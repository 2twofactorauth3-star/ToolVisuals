package ru.he3hauka.toolvisuals.protocols.build;

import java.io.IOException;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

final class Instructions implements Opcodes {
    private Instructions() {}

    static AbstractInsnNode previous(AbstractInsnNode instruction) {
        do { instruction = instruction.getPrevious(); } while (instruction != null && instruction.getOpcode() < 0);
        return instruction;
    }

    static AbstractInsnNode next(AbstractInsnNode instruction) {
        do { instruction = instruction.getNext(); } while (instruction != null && instruction.getOpcode() < 0);
        return instruction;
    }

    static int integer(AbstractInsnNode instruction) throws IOException {
        int opcode = instruction.getOpcode();
        if (opcode >= ICONST_M1 && opcode <= ICONST_5) return opcode - ICONST_0;
        if (instruction instanceof IntInsnNode value) return value.operand;
        if (instruction instanceof LdcInsnNode value && value.cst instanceof Integer number) return number;
        throw new IOException("Expected an integer array operand");
    }

    static AbstractInsnNode integer(int value) {
        return value >= -1 && value <= 5 ? new InsnNode(ICONST_0 + value) : new LdcInsnNode(value);
    }

    static void removeArrayElement(MethodNode method, AbstractInsnNode producer) throws IOException {
        removeArrayElement(method, producer, producer);
    }

    static void removeArrayElement(MethodNode method, AbstractInsnNode first, AbstractInsnNode last) throws IOException {
        var index = previous(first);
        var duplicate = previous(index);
        var store = next(last);
        if (duplicate == null || duplicate.getOpcode() != DUP || store == null || store.getOpcode() != AASTORE)
            throw new IOException("Unrecognized optional platform array in " + method.name);
        int removed = integer(index);
        var array = duplicate;
        while (array != null && array.getOpcode() != ANEWARRAY) array = previous(array);
        if (array == null) throw new IOException("Optional platform array has no allocation");
        var size = previous(array);
        int length = integer(size);
        method.instructions.set(size, integer(length - 1));
        var cursor = next(store);
        while (cursor != null && cursor.getOpcode() == DUP) {
            var elementIndex = next(cursor);
            int value = integer(elementIndex);
            if (value > removed) method.instructions.set(elementIndex, integer(value - 1));
            while (cursor != null && cursor.getOpcode() != AASTORE) cursor = next(cursor);
            cursor = cursor == null ? null : next(cursor);
        }
        method.instructions.remove(duplicate);
        method.instructions.remove(index);
        var cursorToRemove = first;
        while (cursorToRemove != null) {
            var following = cursorToRemove.getNext();
            method.instructions.remove(cursorToRemove);
            if (cursorToRemove == last) break;
            cursorToRemove = following;
        }
        method.instructions.remove(store);
    }

    static void body(MethodNode method, InsnList code, int stack) {
        method.instructions = code;
        method.tryCatchBlocks.clear();
        method.localVariables = null;
        method.visibleLocalVariableAnnotations = null;
        method.invisibleLocalVariableAnnotations = null;
        method.maxStack = stack;
    }
}
