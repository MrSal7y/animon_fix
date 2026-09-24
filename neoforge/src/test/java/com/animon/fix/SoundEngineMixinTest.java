package com.animon.fix;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.*;
import java.io.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class SoundEngineMixinTest {
    @Test
    void hookTargetsPlaybackAfterNeoForgeReplacement() throws IOException {
        ClassNode engine = read("net/minecraft/client/sounds/SoundEngine");
        ClassNode mixin = read("com/animon/fix/mixin/SoundEngineMixin");
        int checked = 0;
        for (MethodNode method : mixin.methods) {
            if (method.visibleAnnotations == null) continue;
            for (AnnotationNode ann : method.visibleAnnotations) {
                if (!ann.desc.endsWith("/Inject;")) continue;
                String selector = (String) ((List<?>) value(ann, "method")).getFirst();
                MethodNode target = engine.methods.stream().filter(m -> (m.name + m.desc).equals(selector)).findFirst().orElseThrow();
                AnnotationNode at = (AnnotationNode) ((List<?>) value(ann, "at")).getFirst();
                String invocation = (String) value(at, "target");
                int hooks = -1, resolve = -1, selection = -1;
                for (int i = 0; i < target.instructions.size(); i++) {
                    if (target.instructions.get(i) instanceof MethodInsnNode call) {
                        if (call.owner.equals("net/neoforged/neoforge/client/ClientHooks") && call.name.equals("playSound")) hooks = i;
                        if (call.name.equals("resolve")) selection = i;
                        if (("L" + call.owner + ";" + call.name + call.desc).equals(invocation)) resolve = i;
                    }
                }
                assertTrue(hooks >= 0 && resolve > hooks && resolve > selection && selection >= 0, "Filter must run after NeoForge sound replacement, at an existing invocation");
                checked++;
            }
        }
        assertEquals(1, checked, "One playback check: do not mark cries at enqueue time or twice");
        assertTrue(engine.fields.stream().anyMatch(f -> f.name.equals("instanceToChannel") && f.desc.equals("Ljava/util/Map;")));
        assertTrue(engine.methods.stream().anyMatch(m -> m.name.equals("stop") && m.desc.equals("(Lnet/minecraft/client/resources/sounds/SoundInstance;)V")));
    }

    @Test
    void delayedSoundsReachTheHookWithoutGoingThroughSoundManager() throws IOException {
        ClassNode engine = read("net/minecraft/client/sounds/SoundEngine");
        assertTrue(engine.methods.stream().filter(m -> m.name.equals("tickNonPaused")).anyMatch(m -> {
            for (AbstractInsnNode insn : m.instructions) {
                if (insn instanceof MethodInsnNode call && call.owner.equals(engine.name) && call.name.equals("play")) return true;
            }
            return false;
        }));
    }

    @Test
    void selectedAnimationHooksMatchCobblemonCalls() throws IOException {
        ClassNode target = read("com/cobblemon/mod/common/client/render/models/blockbench/PosableState");
        ClassNode mixin = read("com/animon/fix/mixin/PosableStateCryAnimationMixin");
        int checked = 0;
        for (MethodNode method : mixin.methods) {
            if (method.visibleAnnotations == null) continue;
            for (AnnotationNode ann : method.visibleAnnotations) {
                if (!ann.desc.endsWith("/ModifyArg;")) continue;
                String selector = (String) ((List<?>) value(ann, "method")).getFirst();
                MethodNode targetMethod = target.methods.stream().filter(m -> (m.name + m.desc).equals(selector)).findFirst().orElseThrow();
                AnnotationNode at = (AnnotationNode) value(ann, "at");
                String invocation = (String) value(at, "target");
                int matches = 0;
                for (AbstractInsnNode insn : targetMethod.instructions) {
                    if (insn instanceof MethodInsnNode call && ("L" + call.owner + ";" + call.name + call.desc).equals(invocation)) {
                        var args = org.objectweb.asm.Type.getArgumentTypes(call.desc);
                        int index = (Integer) value(ann, "index");
                        assertEquals(org.objectweb.asm.Type.getArgumentTypes(method.desc)[0], args[index]);
                        matches++;
                    }
                }
                assertEquals(1, matches, invocation);
                checked++;
            }
        }
        assertEquals(2, checked);
    }

    private static Object value(AnnotationNode ann, String name) {
        for (int i = 0; i < ann.values.size(); i += 2) if (ann.values.get(i).equals(name)) return ann.values.get(i + 1);
        throw new AssertionError("Missing " + name);
    }

    private static ClassNode read(String name) throws IOException {
        try (InputStream in = SoundEngineMixinTest.class.getClassLoader().getResourceAsStream(name + ".class")) {
            assertNotNull(in, name);
            ClassNode node = new ClassNode();
            new ClassReader(in).accept(node, 0);
            return node;
        }
    }
}
