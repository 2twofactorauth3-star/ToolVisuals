package ru.he3hauka.toolvisuals.client.items.highlight.render;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import ru.he3hauka.toolvisuals.client.compatibility.render.LegacyRenderState;

public final class LegacyItemTargets extends LegacyRenderState {
    public static LegacyItemTargets bind(RenderTarget target) { var state = new LegacyItemTargets(); target.bindWrite(true); return state; }
    public static TextureTarget create(int width, int height, boolean depth) {
        try (var state = new LegacyItemTargets()) { return new TextureTarget(width, height, depth); }
    }
    public static void destroy(RenderTarget target) {
        try (var state = new LegacyItemTargets()) { target.destroyBuffers(); }
    }
}
