package ru.he3hauka.toolvisuals.protocols.build;

import java.util.HashSet;
import java.util.Set;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;

final class ReferencedTypes {
    private ReferencedTypes() {}

    static Set<String> read(byte[] bytes) {
        var names = new HashSet<String>();
        var collector = new Remapper() {
            @Override public String map(String name) { names.add(name); return name; }
        };
        new ClassReader(bytes).accept(new ClassRemapper(new ClassVisitor(Opcodes.ASM9) {}, collector), ClassReader.SKIP_DEBUG);
        return names;
    }
}
