package ru.he3hauka.toolvisuals.protocols.build;

import java.util.Set;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

final class ProtocolPatches implements Opcodes {
    static final String VERSION = "com/viaversion/viaversion/api/protocol/version/ProtocolVersion";
    static final String POLICY = "ru/he3hauka/toolvisuals/client/connection/ProtocolPolicy";
    private static final String UI = "ru/he3hauka/toolvisuals/client/connection/ui/ProtocolUi";
    private final String translator;
    int guards, catalogs, routes;

    ProtocolPatches(String minecraft) {
        translator = "com/viaversion/viafabricplus/protocoltranslator/" + (minecraft.equals("26.3") ? "ProtocolTranslationImpl" : "ProtocolTranslator");
    }

    private FieldInsnNode nativeVersion() { return new FieldInsnNode(GETSTATIC, translator, "NATIVE_VERSION", "L" + VERSION + ";"); }

    boolean patch(ClassNode type) {
        int before = guards + catalogs + routes;
        for (var method : type.methods) {
            if (type.name.equals(translator) && method.name.equals("setTargetVersion") && method.desc.equals("(L" + VERSION + ";Z)V")) {
                int parameter = (method.access & ACC_STATIC) != 0 ? 0 : 1;
                var code = new InsnList();
                code.add(new VarInsnNode(ALOAD, parameter));
                code.add(nativeVersion());
                code.add(new MethodInsnNode(INVOKESTATIC, POLICY, "normalize", "(L" + VERSION + ";L" + VERSION + ";)L" + VERSION + ";", false));
                code.add(new VarInsnNode(ASTORE, parameter));
                method.instructions.insert(code);
                method.maxStack += 2;
                guards++;
            }
            if (type.name.endsWith("/SettingsSave") && method.name.equals("protocolVersionByName")) {
                var code = new InsnList();
                code.add(new VarInsnNode(ALOAD, 0));
                code.add(nativeVersion());
                code.add(new MethodInsnNode(INVOKESTATIC, POLICY, "byName", "(Ljava/lang/String;L" + VERSION + ";)L" + VERSION + ";", false));
                code.add(new InsnNode(ARETURN));
                Instructions.body(method, code, 2);
                guards++;
            }
            if (type.name.endsWith("/ScreensImpl") && method.name.equals("openViaFabricPlusScreen")) {
                var code = new InsnList();
                code.add(new VarInsnNode(ALOAD, 1));
                code.add(new MethodInsnNode(INVOKESTATIC, UI, "open", "(Ljava/lang/Object;)V", false));
                code.add(new InsnNode(RETURN));
                Instructions.body(method, code, 1);
                routes++;
            }
            for (var instruction : method.instructions.toArray()) {
                if (instruction instanceof MethodInsnNode call && call.getOpcode() == INVOKESTATIC && call.owner.equals(VERSION)
                        && Set.of("getProtocols", "getReversedProtocols").contains(call.name) && call.desc.equals("()Ljava/util/List;")) {
                    var code = new InsnList();
                    code.add(nativeVersion());
                    code.add(new MethodInsnNode(INVOKESTATIC, POLICY, "catalog", "(Ljava/util/List;L" + VERSION + ";)Ljava/util/List;", false));
                    method.instructions.insert(call, code);
                    method.maxStack += 1;
                    catalogs++;
                }
                if (instruction instanceof FieldInsnNode field && field.getOpcode() == GETSTATIC && selection(field.owner) && field.name.equals("INSTANCE"))
                    method.instructions.set(field, new InsnNode(ACONST_NULL));
                if (instruction instanceof MethodInsnNode call && selection(call.owner) && call.name.equals("open")
                        && Type.getArgumentTypes(call.desc).length == 1) {
                    var code = new InsnList();
                    code.add(new InsnNode(SWAP));
                    code.add(new InsnNode(POP));
                    code.add(new MethodInsnNode(INVOKESTATIC, UI, "open", "(Ljava/lang/Object;)V", false));
                    method.instructions.insertBefore(call, code);
                    method.instructions.remove(call);
                    routes++;
                }
                if (instruction instanceof FieldInsnNode field && field.getOpcode() == PUTFIELD && field.desc.equals("L" + VERSION + ";")
                        && (field.name.toLowerCase(java.util.Locale.ROOT).contains("targetversion") || field.name.toLowerCase(java.util.Locale.ROOT).contains("forcedversion"))) {
                    var code = new InsnList();
                    code.add(nativeVersion());
                    code.add(new MethodInsnNode(INVOKESTATIC, POLICY, "normalizeNullable", "(L" + VERSION + ";L" + VERSION + ";)L" + VERSION + ";", false));
                    method.instructions.insertBefore(field, code);
                    method.maxStack += 1;
                    guards++;
                }
            }
        }
        return guards + catalogs + routes != before;
    }

    static boolean selection(String owner) { return owner.equals("com/viaversion/viafabricplus/screen/impl/ProtocolSelectionScreen"); }
}
