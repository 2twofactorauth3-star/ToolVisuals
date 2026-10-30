package ru.he3hauka.toolvisuals.protocols.build;

import java.util.Objects;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;

final class ConstantBranches implements Opcodes {
    private static final BasicValue BEDROCK = new BasicValue(Type.getObjectType("toolvisuals/RemovedBedrock"));

    private static final class NumberValue extends BasicValue {
        final int value;
        NumberValue(int value) { super(Type.INT_TYPE); this.value = value; }
        @Override public boolean equals(Object other) { return other instanceof NumberValue number && value == number.value; }
        @Override public int hashCode() { return value; }
    }

    private static final class Values extends BasicInterpreter {
        Values() { super(ASM9); }

        @Override public BasicValue newOperation(AbstractInsnNode instruction) throws AnalyzerException {
            if (bedrock(instruction)) return BEDROCK;
            int opcode = instruction.getOpcode();
            if (opcode >= ICONST_M1 && opcode <= ICONST_5) return new NumberValue(opcode - ICONST_0);
            if (instruction instanceof IntInsnNode number && (opcode == BIPUSH || opcode == SIPUSH)) return new NumberValue(number.operand);
            if (instruction instanceof LdcInsnNode constant && constant.cst instanceof Integer number) return new NumberValue(number);
            return super.newOperation(instruction);
        }

        @Override public BasicValue merge(BasicValue first, BasicValue second) {
            if (Objects.equals(first, second)) return first;
            if (first instanceof NumberValue && second.getType() == Type.INT_TYPE
                    || second instanceof NumberValue && first.getType() == Type.INT_TYPE) return BasicValue.INT_VALUE;
            return super.merge(first, second);
        }
    }

    private ConstantBranches() {}

    private static boolean bedrock(AbstractInsnNode instruction) {
        return instruction instanceof FieldInsnNode field && field.getOpcode() == GETSTATIC
                && field.owner.equals("net/raphimc/viabedrock/api/BedrockProtocolVersion") && field.name.equals("bedrockLatest");
    }

    static boolean specialize(String owner, MethodNode method) throws AnalyzerException {
        if (method.instructions.size() == 0) return false;
        boolean changed = false;
        var instructions = method.instructions.toArray();
        var frames = new Analyzer<>(new Values()).analyze(owner, method);
        for (int index = 0; index < instructions.length; index++) {
            var instruction = instructions[index];
            var frame = frames[index];
            if (frame == null) continue;
            if (instruction instanceof MethodInsnNode call && call.name.equals("equals") && call.desc.equals("(Ljava/lang/Object;)Z")) {
                int top = frame.getStackSize();
                boolean receiver = frame.getStack(top - 2) == BEDROCK, argument = frame.getStack(top - 1) == BEDROCK;
                if (receiver || argument) {
                    var replacement = new InsnList();
                    replacement.add(new InsnNode(POP2));
                    replacement.add(new InsnNode(receiver && argument ? ICONST_1 : ICONST_0));
                    method.instructions.insertBefore(call, replacement);
                    method.instructions.remove(call);
                    changed = true;
                }
            }
        }
        for (var instruction : method.instructions.toArray()) if (bedrock(instruction)) {
            method.instructions.set(instruction, new InsnNode(ACONST_NULL));
            changed = true;
        }
        if (!changed) return false;
        boolean folded;
        do {
            folded = false;
            instructions = method.instructions.toArray();
            frames = new Analyzer<>(new Values()).analyze(owner, method);
            for (int index = 0; index < instructions.length; index++) {
                if (!(instructions[index] instanceof JumpInsnNode branch) || frames[index] == null) continue;
                var frame = frames[index];
                if (frame.getStackSize() == 0 || !(frame.getStack(frame.getStackSize() - 1) instanceof NumberValue number)) continue;
                Boolean taken = switch (branch.getOpcode()) {
                    case IFEQ -> number.value == 0;
                    case IFNE -> number.value != 0;
                    case IFLT -> number.value < 0;
                    case IFGE -> number.value >= 0;
                    case IFGT -> number.value > 0;
                    case IFLE -> number.value <= 0;
                    default -> null;
                };
                if (taken == null) continue;
                method.instructions.insertBefore(branch, new InsnNode(POP));
                if (taken) method.instructions.set(branch, new JumpInsnNode(GOTO, branch.label));
                else method.instructions.remove(branch);
                folded = true;
            }
        } while (folded);
        prune(owner, method);
        return true;
    }

    private static void prune(String owner, MethodNode method) throws AnalyzerException {
        var instructions = method.instructions.toArray();
        var frames = new Analyzer<>(new BasicInterpreter()).analyze(owner, method);
        var live = new java.util.HashSet<AbstractInsnNode>();
        for (int index = 0; index < instructions.length; index++) if (frames[index] != null) live.add(instructions[index]);
        method.tryCatchBlocks.removeIf(block -> {
            for (AbstractInsnNode instruction = block.start; instruction != null && instruction != block.end; instruction = instruction.getNext())
                if (instruction.getOpcode() >= 0 && live.contains(instruction)) return false;
            return true;
        });
        for (var instruction : instructions)
            if (!live.contains(instruction) && (instruction.getOpcode() >= 0 || instruction instanceof FrameNode)) method.instructions.remove(instruction);
        method.localVariables = null;
        method.visibleLocalVariableAnnotations = null;
        method.invisibleLocalVariableAnnotations = null;
    }
}
