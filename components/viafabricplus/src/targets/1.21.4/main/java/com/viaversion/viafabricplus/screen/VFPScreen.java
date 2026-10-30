package com.viaversion.viafabricplus.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.NoticeScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.PressableTextWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import com.viaversion.viafabricplus.ViaFabricPlusImpl;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

public class VFPScreen extends Screen {

    private static final String MOD_URL = "https://github.com/ViaVersion/ViaFabricPlus";

    private final boolean backButton;
    public Screen prevScreen;

    private Text subtitle;
    private ButtonWidget.PressAction subtitlePressAction;

    private PressableTextWidget subtitleWidget;

    public VFPScreen(final String title, final boolean backButton) {
        this(Text.of(title), backButton);
    }

    public VFPScreen(final Text title, final boolean backButton) {
        super(title);
        this.backButton = backButton;
    }

    public void setupDefaultSubtitle() {
        this.setupUrlSubtitle(MOD_URL);
    }

    public void setupUrlSubtitle(final String subtitle) {
        this.setupSubtitle(Text.of(subtitle), ConfirmLinkScreen.opening(this, subtitle));
    }

    public void setupSubtitle(@Nullable final Text subtitle) {
        this.setupSubtitle(subtitle, null);
    }

    public void setupSubtitle(@Nullable final Text subtitle, @Nullable final ButtonWidget.PressAction subtitlePressAction) {
        this.subtitle = subtitle;
        this.subtitlePressAction = subtitlePressAction;

        if (subtitleWidget != null) {
            remove(subtitleWidget);
            subtitleWidget = null;
        }
        if (subtitlePressAction != null) {
            final int subtitleWidth = textRenderer.getWidth(subtitle);
            this.addDrawableChild(subtitleWidget = new PressableTextWidget(width / 2 - (subtitleWidth / 2), (textRenderer.fontHeight + 2) * 2 + 3, subtitleWidth, textRenderer.fontHeight + 2, subtitle, subtitlePressAction, textRenderer));
        }
    }

    public void open(final Screen prevScreen) {
        this.prevScreen = prevScreen;
        setScreen(this);
    }

    public static void setScreen(final Screen screen) {
        final MinecraftClient client = MinecraftClient.getInstance();

        client.execute(() -> client.setScreen(screen));
    }

    @Override
    protected void init() {
        if (backButton) {
            this.addDrawableChild(ButtonWidget.builder(Text.of("<-"), button -> this.close()).position(5, 5).size(20, 20).build());
        }
    }

    public void addRefreshButton(final Runnable click) {
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("base.viafabricplus.refresh"), button -> {
            click.run();
            client.setScreen(this);
        }).position(width - 60 - 5, 5).size(60, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        this.renderTitle(context);
    }

    @Override
    public void close() {
        if (prevScreen instanceof VFPScreen vfpScreen) {
            vfpScreen.open(vfpScreen.prevScreen);
        } else {
            MinecraftClient.getInstance().setScreen(prevScreen);
        }
    }

    public void renderTitle(final DrawContext context) {
        final MatrixStack matrices = context.getMatrices();

        matrices.push();
        matrices.scale(2F, 2F, 2F);
        context.drawCenteredTextWithShadow(textRenderer, "ViaFabricPlus", width / 4, 3, Color.ORANGE.getRGB());
        matrices.pop();

        renderSubtitle(context);
    }

    public void renderSubtitle(final DrawContext context) {
        if (subtitle != null && subtitlePressAction == null) {
            final int startY = (textRenderer.fontHeight + 2) * 2 + 3;
            context.drawCenteredTextWithShadow(textRenderer, subtitle, width / 2, subtitleCentered() ? this.height / 2 - startY : startY, -1);
        }
    }

    protected boolean subtitleCentered() {

        return false;
    }

    public void renderScreenTitle(final DrawContext context) {
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 70, 16777215);
    }

    public static void showErrorScreen(final Text title, final Throwable throwable, final Screen next) {
        ViaFabricPlusImpl.INSTANCE.logger().error("Something went wrong!", throwable);

        final MinecraftClient client = MinecraftClient.getInstance();
        client.execute(() -> client.setScreen(new NoticeScreen(() -> client.setScreen(next), title, Text.translatable("base.viafabricplus.something_went_wrong").append("\n" + throwable.getMessage()), Text.translatable("base.viafabricplus.cancel"), false)));
    }

}
