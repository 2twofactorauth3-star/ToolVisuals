package com.viaversion.viafabricplus.protocoltranslator.impl.platform;

import com.viaversion.viafabricplus.ViaFabricPlusImpl;
import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import com.viaversion.viafabricplus.protocoltranslator.impl.viaversion.ViaFabricPlusVLViaConfig;
import com.viaversion.vialoader.impl.platform.ViaVersionPlatformImpl;
import com.viaversion.viaversion.configuration.AbstractViaConfig;
import com.viaversion.viaversion.libs.gson.JsonArray;
import com.viaversion.viaversion.libs.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;

public final class ViaFabricPlusViaVersionPlatformImpl extends ViaVersionPlatformImpl {

    public ViaFabricPlusViaVersionPlatformImpl(File rootFolder) {
        super(rootFolder);
    }

    @Override
    public String getPlatformName() {
        return "ViaFabricPlus";
    }

    @Override
    public String getPlatformVersion() {
        return ViaFabricPlusImpl.INSTANCE.getVersion();
    }

    @Override
    protected AbstractViaConfig createConfig() {

        return new ViaFabricPlusVLViaConfig(new File(getDataFolder(), "viaversion.yml"), this.getLogger());
    }

    @Override
    public JsonObject getDump() {
        final JsonObject platformDump = new JsonObject();
        platformDump.addProperty("impl_version", ViaFabricPlusImpl.INSTANCE.getImplVersion());
        platformDump.addProperty("native_version", ProtocolTranslator.NATIVE_VERSION.toString());
        platformDump.addProperty("target_version", ProtocolTranslator.getTargetVersion().toString());

        final JsonArray mods = new JsonArray();
        FabricLoader.getInstance().getAllMods().stream().map(mod -> {
            final JsonObject jsonMod = new JsonObject();
            jsonMod.addProperty("id", mod.getMetadata().getId());
            jsonMod.addProperty("name", mod.getMetadata().getName());
            jsonMod.addProperty("version", mod.getMetadata().getVersion().getFriendlyString());
            final JsonArray authors = new JsonArray();
            mod.getMetadata().getAuthors().stream().map(it -> {
                final JsonObject info = new JsonObject();
                final JsonObject contact = new JsonObject();
                it.getContact().asMap().forEach(contact::addProperty);

                if (!contact.isEmpty()) {
                    info.add("contact", contact);
                }
                info.addProperty("name", it.getName());
                return info;
            }).forEach(authors::add);
            jsonMod.add("authors", authors);

            return jsonMod;
        }).forEach(mods::add);

        platformDump.add("mods", mods);

        return platformDump;
    }

    @Override
    public File getDataFolder() {

        return ViaFabricPlusImpl.INSTANCE.rootPath().toFile();
    }

}
