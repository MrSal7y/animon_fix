package com.animon.fix;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SoundManagerMixinTest {
    @Test
    void everySoundInjectionTargetsAnExistingMinecraftMethod() throws IOException {
        ClassNode target = readClass("net/minecraft/client/sounds/SoundManager");
        Set<String> methods = new HashSet<>();
        target.methods.forEach(method -> methods.add(method.name + method.desc));

        ClassNode mixin = readClass("com/animon/fix/mixin/SoundManagerMixin");
        int checked = 0;
        for (var method : mixin.methods) {
            if (method.visibleAnnotations == null) {
                continue;
            }
            for (AnnotationNode annotation : method.visibleAnnotations) {
                if (!annotation.desc.equals("Lorg/spongepowered/asm/mixin/injection/Inject;")) {
                    continue;
                }
                for (int i = 0; i < annotation.values.size(); i += 2) {
                    if (!annotation.values.get(i).equals("method")) {
                        continue;
                    }
                    for (Object selector : (List<?>) annotation.values.get(i + 1)) {
                        assertTrue(methods.contains(selector),
                                () -> "SoundManager has no method " + selector);
                        checked++;
                    }
                }
            }
        }
        assertEquals(2, checked, "Check both immediate and delayed sound hooks");
    }

    private static ClassNode readClass(String name) throws IOException {
        try (InputStream stream = SoundManagerMixinTest.class.getClassLoader()
                .getResourceAsStream(name + ".class")) {
            if (stream == null) {
                throw new IOException("Missing class: " + name);
            }
            ClassNode node = new ClassNode();
            new ClassReader(stream).accept(node, ClassReader.SKIP_CODE);
            return node;
        }
    }
}
