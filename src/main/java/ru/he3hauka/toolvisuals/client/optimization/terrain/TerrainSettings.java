package ru.he3hauka.toolvisuals.client.optimization.terrain;

import net.minecraft.client.Minecraft;

import ru.he3hauka.toolvisuals.client.optimization.Optimizations;

import static ru.he3hauka.toolvisuals.client.module.VisualSetting.OPT_HIDDEN_FACES;

public final class TerrainSettings {
    private static Object world;
    private static boolean hiddenFaces;
    private TerrainSettings() {}
    public static void tick(Minecraft client){
        boolean enabled=Optimizations.enabled(OPT_HIDDEN_FACES);
        if(world==client.level&&hiddenFaces!=enabled&&client.level!=null)client.levelExtractor.allChanged();
        world=client.level;hiddenFaces=enabled;
    }
}
