package ru.he3hauka.toolvisuals.client.cosmetic.accessory.render;

public interface AccessoryVisualState {
    AccessoryFrame toolvisuals$accessories();
    void toolvisuals$accessories(AccessoryFrame frame);
    static void clear(Object state){if(state instanceof AccessoryVisualState visual)visual.toolvisuals$accessories(null);}
}
