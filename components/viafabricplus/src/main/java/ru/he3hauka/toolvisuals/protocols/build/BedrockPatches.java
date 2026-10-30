package ru.he3hauka.toolvisuals.protocols.build;

import java.io.IOException;
import java.util.Arrays;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

final class BedrockPatches implements Opcodes {
    int arrays, providers, platforms, accounts;

    static boolean removed(String name) {
        int nested = name.indexOf('$');
        if (nested >= 0) name = name.substring(0, nested);
        return name.contains("/bedrock/") || name.contains("/viabedrock/")
                || name.endsWith("/ViaBedrockPlatformImpl")
                || name.endsWith("/BedrockSettings") || name.endsWith("/MixinViaBedrockConfig")
                || name.endsWith("/MixinChunkTracker") || name.endsWith("/MixinRakSessionCodec")
                || name.endsWith("/RakNetPingEncapsulationCodec") || name.endsWith("/MixinDebugHud")
                || name.startsWith("com/viaversion/viafabricplus/screen/impl/ProtocolSelectionScreen")
                || name.startsWith("com/viaversion/viafabricplus/screen/impl/ServerListScreen")
                || name.startsWith("com/viaversion/viafabricplus/screen/impl/realms/BedrockRealmsScreen");
    }

    boolean patch(ClassNode type) throws Exception {
        boolean changed = false;
        if (type.name.endsWith("/AccountsSave")) {
            preserveAccounts(type);
            accounts++;
            return true;
        }
        changed |= type.methods.removeIf(method -> method.name.equals("changeBedrockProtocolName")
                || type.name.endsWith("/ConnectionUtil") && (method.name.toLowerCase(java.util.Locale.ROOT).contains("bedrock")
                || method.desc.contains("dev/kastle/netty/channel/nethernet/")));
        for (var method : type.methods) {
            if (type.name.endsWith("/VFPDebugHudEntry") && debug(method)) {
                changed = true;
                continue;
            }
            for (var instruction : method.instructions.toArray()) {
                if (instruction.getPrevious() == null && method.instructions.getFirst() != instruction) continue;
                if (instruction instanceof FieldInsnNode field && field.getOpcode() == GETSTATIC
                        && (field.owner.endsWith("/BedrockSettings") || field.owner.endsWith("/AuthenticationSettings"))
                        && field.name.equals("INSTANCE") && type.name.endsWith("/SettingsManager")) {
                    Instructions.removeArrayElement(method, field);
                    arrays++;
                    changed = true;
                }
                if (instruction instanceof InvokeDynamicInsnNode dynamic && Arrays.stream(dynamic.bsmArgs)
                        .anyMatch(argument -> argument instanceof Handle handle && handle.getOwner().endsWith("/ViaBedrockPlatformImpl"))) {
                    Instructions.removeArrayElement(method, dynamic);
                    platforms++;
                    changed = true;
                }
                if (instruction instanceof MethodInsnNode call && call.name.equals("changeBedrockProtocolName")) {
                    method.instructions.remove(call);
                    changed = true;
                }
                if (instruction instanceof TypeInsnNode allocation && allocation.getOpcode() == NEW && allocation.desc.endsWith("/ViaBedrockPlatformImpl")) {
                    var duplicate = Instructions.next(allocation);
                    var constructor = Instructions.next(duplicate);
                    var discard = Instructions.next(constructor);
                    if (duplicate.getOpcode() != DUP || !(constructor instanceof MethodInsnNode init) || !init.name.equals("<init>") || discard.getOpcode() != POP)
                        throw new IOException("Unrecognized Bedrock platform constructor");
                    method.instructions.remove(allocation);
                    method.instructions.remove(duplicate);
                    method.instructions.remove(constructor);
                    method.instructions.remove(discard);
                    platforms++;
                    changed = true;
                }
                if (instruction instanceof LdcInsnNode literal && literal.cst instanceof Type value
                        && value.getSort() == Type.OBJECT && value.getInternalName().startsWith("net/raphimc/viabedrock/")
                        && (type.name.endsWith("/ViaFabricPlusPlatformLoader") || type.name.endsWith("/ViaFabricPlusVLLoader"))) {
                    var first = Instructions.previous(literal);
                    AbstractInsnNode last = literal;
                    while (last != null && !(last instanceof MethodInsnNode call && call.name.equals("use"))) last = Instructions.next(last);
                    if (first == null || first.getOpcode() != ALOAD || last == null) throw new IOException("Unrecognized Bedrock provider installation");
                    for (var cursor = first; cursor != null;) {
                        var next = cursor.getNext();
                        method.instructions.remove(cursor);
                        if (cursor == last) break;
                        cursor = next;
                    }
                    providers++;
                    changed = true;
                }
                if (instruction instanceof TypeInsnNode allocation && allocation.getOpcode() == NEW && allocation.desc.endsWith("/AccountsSave")
                        && type.name.endsWith("/SaveManager")) {
                    var first = Instructions.previous(allocation);
                    AbstractInsnNode last = allocation;
                    while (last != null && !(last instanceof FieldInsnNode field && field.getOpcode() == PUTFIELD)) last = Instructions.next(last);
                    if (first.getOpcode() != ALOAD || last == null) throw new IOException("Unrecognized legacy account store registration");
                    Instructions.removeArrayElement(method, first, last);
                    arrays++;
                    changed = true;
                }
                if (instruction instanceof MethodInsnNode call && call.owner.endsWith("/BedrockSettings") && call.name.equals("replaceDefaultPort")) {
                    method.instructions.set(call, new InsnNode(POP));
                    changed = true;
                }
            }
            changed |= ConstantBranches.specialize(type.name, method);
        }
        return changed;
    }

    private boolean debug(MethodNode method) {
        if (method.instructions.size() == 0 || !method.desc.endsWith(")V")) return false;
        boolean bedrock = Arrays.stream(method.instructions.toArray()).anyMatch(instruction -> instruction instanceof LdcInsnNode constant
                && constant.cst instanceof Type value && value.getSort() == Type.OBJECT && value.getInternalName().startsWith("net/raphimc/viabedrock/"));
        if (!bedrock) return false;
        var line = Arrays.stream(method.instructions.toArray()).filter(instruction -> instruction instanceof MethodInsnNode call
                && call.desc.equals("(Ljava/lang/String;)V")).map(instruction -> (MethodInsnNode) instruction).findFirst().orElseThrow();
        var code = new InsnList();
        code.add(new VarInsnNode(ALOAD, 1));
        code.add(new LdcInsnNode("ToolVisuals Java protocols (ViaFabricPlus)"));
        code.add(new MethodInsnNode(line.getOpcode(), line.owner, line.name, line.desc, line.itf));
        code.add(new InsnNode(RETURN));
        Instructions.body(method, code, 2);
        return true;
    }

    private void preserveAccounts(ClassNode type) {
        type.fields.removeIf(field -> field.name.toLowerCase(java.util.Locale.ROOT).contains("bedrock"));
        type.fields.add(new FieldNode(ACC_PRIVATE, "toolvisuals$preserved", "Lcom/google/gson/JsonObject;", null, null));
        type.methods.removeIf(method -> !method.name.equals("<init>") && !method.name.equals("read") && !method.name.equals("write")
                && !method.name.equals("getClassicubeAccount") && !method.name.equals("setClassicubeAccount"));
        for (var method : type.methods) {
            if (method.name.equals("read")) {
                var code = new InsnList();
                code.add(new VarInsnNode(ALOAD, 0));
                code.add(new VarInsnNode(ALOAD, 1));
                code.add(new MethodInsnNode(INVOKEVIRTUAL, "com/google/gson/JsonObject", "deepCopy", "()Lcom/google/gson/JsonObject;", false));
                code.add(new FieldInsnNode(PUTFIELD, type.name, "toolvisuals$preserved", "Lcom/google/gson/JsonObject;"));
                code.add(new InsnNode(RETURN));
                Instructions.body(method, code, 2);
            } else if (method.name.equals("write")) {
                var end = new LabelNode();
                var code = new InsnList();
                code.add(new VarInsnNode(ALOAD, 0));
                code.add(new FieldInsnNode(GETFIELD, type.name, "toolvisuals$preserved", "Lcom/google/gson/JsonObject;"));
                code.add(new JumpInsnNode(IFNULL, end));
                code.add(new VarInsnNode(ALOAD, 1));
                code.add(new MethodInsnNode(INVOKEVIRTUAL, "com/google/gson/JsonObject", "asMap", "()Ljava/util/Map;", false));
                code.add(new VarInsnNode(ALOAD, 0));
                code.add(new FieldInsnNode(GETFIELD, type.name, "toolvisuals$preserved", "Lcom/google/gson/JsonObject;"));
                code.add(new MethodInsnNode(INVOKEVIRTUAL, "com/google/gson/JsonObject", "deepCopy", "()Lcom/google/gson/JsonObject;", false));
                code.add(new MethodInsnNode(INVOKEVIRTUAL, "com/google/gson/JsonObject", "asMap", "()Ljava/util/Map;", false));
                code.add(new MethodInsnNode(INVOKEINTERFACE, "java/util/Map", "putAll", "(Ljava/util/Map;)V", true));
                code.add(end);
                code.add(new FrameNode(F_SAME, 0, null, 0, null));
                code.add(new InsnNode(RETURN));
                Instructions.body(method, code, 2);
            }
        }
    }
}
