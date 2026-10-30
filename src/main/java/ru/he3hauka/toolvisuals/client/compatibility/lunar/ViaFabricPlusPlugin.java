package ru.he3hauka.toolvisuals.client.compatibility.lunar;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.service.MixinService;

public final class ViaFabricPlusPlugin implements IMixinConfigPlugin {
    private final IMixinConfigPlugin original;
    private final BooleanSupplier lunar;
    private final Consumer<String> log;
    private boolean reported;

    public ViaFabricPlusPlugin() {
        this(originalPlugin(),ViaFabricPlusPlugin::lunarChatPatch,
                message->MixinService.getService().getLogger("ToolVisuals/Lunar").info(message));
    }

    ViaFabricPlusPlugin(IMixinConfigPlugin original,BooleanSupplier lunar,Consumer<String> log) {
        this.original=Objects.requireNonNull(original);
        this.lunar=Objects.requireNonNull(lunar);
        this.log=Objects.requireNonNull(log);
    }

    private static IMixinConfigPlugin originalPlugin() {
        try {
            return Class.forName(LunarChatCompatibility.ORIGINAL_PLUGIN,true,ViaFabricPlusPlugin.class.getClassLoader())
                    .asSubclass(IMixinConfigPlugin.class).getDeclaredConstructor().newInstance();
        } catch(ReflectiveOperationException error) {
            throw new IllegalStateException("Cannot initialize the original ViaFabricPlus mixin plugin",error);
        }
    }

    private static boolean lunarChatPatch() {
        if(LunarChatCompatibility.lunarConfig(MixinEnvironment.getCurrentEnvironment().getMixinConfigs()))return true;
        return Mixins.getConfigs().stream().anyMatch(config->LunarChatCompatibility.LUNAR_CONFIG.equals(config.getName()));
    }

    @Override public void onLoad(String mixinPackage) {original.onLoad(mixinPackage);}
    @Override public String getRefMapperConfig() {return original.getRefMapperConfig();}

    @Override public boolean shouldApplyMixin(String targetClassName,String mixinClassName) {
        if(!original.shouldApplyMixin(targetClassName,mixinClassName))return false;
        if(!LunarChatCompatibility.chatCodec(targetClassName,mixinClassName)||!lunar.getAsBoolean())return true;
        if(!reported) {
            log.accept("Lunar chat compatibility active: keeping Lunar's chat codec; skipping only ViaFabricPlus's overlapping chat-codec redirect. Protocol translation remains enabled.");
            reported=true;
        }
        return false;
    }

    @Override public void acceptTargets(Set<String> myTargets,Set<String> otherTargets) {original.acceptTargets(myTargets,otherTargets);}
    @Override public List<String> getMixins() {return original.getMixins();}
    @Override public void preApply(String target,ClassNode node,String mixin,IMixinInfo info) {original.preApply(target,node,mixin,info);}
    @Override public void postApply(String target,ClassNode node,String mixin,IMixinInfo info) {original.postApply(target,node,mixin,info);}
}
