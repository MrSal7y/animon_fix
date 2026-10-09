package com.animon.fix;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.io.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/** Compile success alone cannot prove that mandatory Mixin injection points still exist. */
class FabricMixinCompatibilityTest {
    @Test void allConfiguredMixinTargetsAndShadowsExist() throws IOException {
        var config = JsonParser.parseReader(new InputStreamReader(resource("animon_fix.mixins.json"))).getAsJsonObject();
        int injections = 0;
        for (var entry : config.getAsJsonArray("client")) {
            ClassNode mixin = read("com/animon/fix/mixin/" + entry.getAsString());
            AnnotationNode annotation = annotations(mixin.visibleAnnotations, mixin.invisibleAnnotations).stream()
                    .filter(a -> a.desc.equals("Lorg/spongepowered/asm/mixin/Mixin;")).findFirst().orElseThrow();
            String targetName;
            Object classes = value(annotation, "value");
            if (classes != null) targetName = ((Type)((List<?>)classes).getFirst()).getInternalName();
            else targetName = ((String)((List<?>)value(annotation, "targets")).getFirst()).replace('.', '/');
            ClassNode target = read(targetName);
            for (FieldNode field : mixin.fields) {
                if (hasShadow(field.visibleAnnotations, field.invisibleAnnotations)) {
                    assertTrue(target.fields.stream().anyMatch(f -> f.name.equals(field.name) && f.desc.equals(field.desc)), field.name);
                }
            }
            for (MethodNode method : mixin.methods) {
                for (AnnotationNode ann : annotations(method.visibleAnnotations, method.invisibleAnnotations)) {
                    if (ann.desc.endsWith("/Shadow;")) {
                        assertNotNull(findMethod(target, method.name + method.desc), method.name);
                    }
                    if (!(ann.desc.endsWith("/Inject;") || ann.desc.endsWith("/ModifyArg;") || ann.desc.endsWith("/Redirect;"))) continue;
                    for (Object selector : (List<?>)value(ann, "method")) {
                        MethodNode targetMethod = findMethod(target, (String)selector);
                        assertNotNull(targetMethod, target.name + ": " + selector);
                        Object atValue = value(ann, "at");
                        List<?> ats = atValue instanceof List<?> list ? list : List.of(atValue);
                        for (Object atObject : ats) {
                            AnnotationNode at = (AnnotationNode)atObject;
                            String kind = (String)value(at, "value");
                            String invocation = (String)value(at, "target");
                            if ("INVOKE".equals(kind) || "FIELD".equals(kind)) {
                                int matches = 0;
                                for (AbstractInsnNode insn : targetMethod.instructions) {
                                    if (insn instanceof MethodInsnNode call && ("L" + call.owner + ";" + call.name + call.desc).equals(invocation)) {
                                        matches++;
                                        if (ann.desc.endsWith("/ModifyArg;")) {
                                            int index = (Integer)value(ann, "index");
                                            assertEquals(Type.getArgumentTypes(method.desc)[0], Type.getArgumentTypes(call.desc)[index]);
                                        }
                                    }
                                    if (insn instanceof FieldInsnNode field && ("L" + field.owner + ";" + field.name + ":" + field.desc).equals(invocation)
                                            && (value(at, "opcode") == null || field.getOpcode() == (Integer)value(at, "opcode"))) matches++;
                                }
                                assertEquals(1, matches, mixin.name + ": " + selector + " -> " + invocation);
                            } else {
                                assertTrue(Set.of("HEAD", "TAIL", "RETURN").contains(kind), kind);
                            }
                        }
                        injections++;
                    }
                }
            }
        }
        assertEquals(12, injections, "Check every configured mandatory hook");
    }

    @Test void delayedPlaybackIsFilteredAfterSoundFileSelection() throws IOException {
        ClassNode system = read("net/minecraft/client/sounds/SoundEngine");
        MethodNode play = findMethod(system, "play(Lnet/minecraft/client/resources/sounds/SoundInstance;)V");
        int selection = -1, filter = -1;
        for (int i = 0; i < play.instructions.size(); i++) {
            if (play.instructions.get(i) instanceof MethodInsnNode call) {
                if (call.name.equals("resolve")) selection = i;
                if (call.name.equals("getLocation")) filter = i;
            }
        }
        assertTrue(selection >= 0 && filter > selection);
        MethodNode delayed = findMethod(system, "playDelayed(Lnet/minecraft/client/resources/sounds/SoundInstance;I)V");
        assertFalse(calls(delayed, system.name, "play"), "Enqueueing must not mark a cry as already played");
        assertTrue(system.methods.stream().filter(m -> m.name.equals("tickNonPaused")).anyMatch(m -> calls(m, system.name, "play")));
    }

    @Test void alphaCriesUseTheSameSoundSystemVolumePath() throws IOException {
        ClassNode keyframe = read("com/cobblemon/mod/common/client/render/models/blockbench/bedrock/animation/BedrockSoundKeyframe");
        assertTrue(keyframe.methods.stream().anyMatch(m -> calls(m, "com/cobblemon/mod/common/client/sound/instances/AlphaCrySoundInstance", "<init>")));
        ClassNode alpha = read("com/cobblemon/mod/common/client/sound/instances/AlphaCrySoundInstance");
        assertEquals("net/minecraft/client/resources/sounds/EntityBoundSoundInstance", alpha.superName);
    }

    private static boolean calls(MethodNode method, String owner, String name) {
        for (AbstractInsnNode insn : method.instructions) if (insn instanceof MethodInsnNode call && call.owner.equals(owner) && call.name.equals(name)) return true;
        return false;
    }
    private static boolean hasShadow(List<AnnotationNode> visible, List<AnnotationNode> invisible) {
        return annotations(visible, invisible).stream().anyMatch(a -> a.desc.endsWith("/Shadow;"));
    }
    private static List<AnnotationNode> annotations(List<AnnotationNode> visible, List<AnnotationNode> invisible) {
        var all = new ArrayList<AnnotationNode>();
        if (visible != null) all.addAll(visible);
        if (invisible != null) all.addAll(invisible);
        return all;
    }
    private static MethodNode findMethod(ClassNode target, String selector) throws IOException {
        for (MethodNode method : target.methods) if ((method.name + method.desc).equals(selector)) return method;
        return target.superName == null ? null : findMethod(read(target.superName), selector);
    }
    private static Object value(AnnotationNode ann, String name) {
        if (ann.values != null) for (int i = 0; i < ann.values.size(); i += 2) if (ann.values.get(i).equals(name)) return ann.values.get(i + 1);
        return null;
    }
    private static InputStream resource(String path) {
        InputStream in = FabricMixinCompatibilityTest.class.getClassLoader().getResourceAsStream(path);
        assertNotNull(in, path);
        return in;
    }
    private static ClassNode read(String name) throws IOException {
        try (InputStream in = resource(name + ".class")) {
            ClassNode node = new ClassNode();
            new ClassReader(in).accept(node, 0);
            return node;
        }
    }
}
