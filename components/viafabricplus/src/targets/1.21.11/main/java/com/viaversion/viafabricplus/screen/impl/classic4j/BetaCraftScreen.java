package com.viaversion.viafabricplus.screen.impl.classic4j;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.network.chat.Component;

import com.viaversion.viafabricplus.screen.VFPList;
import com.viaversion.viafabricplus.screen.VFPListEntry;
import com.viaversion.viafabricplus.screen.VFPScreen;
import com.viaversion.viafabricplus.screen.impl.settings.TitleEntry;
import com.viaversion.viafabricplus.util.ConnectionUtil;
import de.florianreuth.classic4j.BetaCraftHandler;
import de.florianreuth.classic4j.model.betacraft.BCServerInfo;
import de.florianreuth.classic4j.model.betacraft.BCServerList;
import de.florianreuth.classic4j.model.betacraft.BCVersionCategory;
import java.util.List;

import static com.viaversion.viafabricplus.screen.VFPListEntry.SLOT_MARGIN;

public final class BetaCraftScreen extends VFPScreen {

    public static final BetaCraftScreen INSTANCE = new BetaCraftScreen();

    public static BCServerList SERVER_LIST;
    private static final String BETA_CRAFT_SERVER_LIST_URL = "https://betacraft.uk/serverlist/";

    private BetaCraftScreen() {
        super("BetaCraft", true);
    }

    @Override
    protected void init() {
        super.init();
        if (SERVER_LIST != null) {
            createView();
            return;
        }
        setupSubtitle(Component.translatable("betacraft.viafabricplus.loading"));
        BetaCraftHandler.requestServerList(serverList -> {
            BetaCraftScreen.SERVER_LIST = serverList;
            createView();
        }, throwable -> showErrorScreen(BetaCraftScreen.INSTANCE.getTitle(), throwable, this));
    }

    private void createView() {
        this.setupSubtitle(Component.nullToEmpty(BETA_CRAFT_SERVER_LIST_URL), ConfirmLinkScreen.confirmLink(this, BETA_CRAFT_SERVER_LIST_URL));

        final int entryHeight = (font.lineHeight + 2) * 3;
        this.addRenderableWidget(new SlotList(this.minecraft, width, height, 2 * SLOT_MARGIN + entryHeight, -5, entryHeight));

        this.addRefreshButton(() -> SERVER_LIST = null);
    }

    @Override
    protected boolean subtitleCentered() {
        return SERVER_LIST == null;
    }

    public static class SlotList extends VFPList {
        private static double scrollAmount;

        public SlotList(Minecraft minecraftClient, int width, int height, int top, int bottom, int entryHeight) {
            super(minecraftClient, width, height, top, bottom, entryHeight);
            if (SERVER_LIST == null) {
                return;
            }

            for (BCVersionCategory value : BCVersionCategory.values()) {
                final List<BCServerInfo> servers = SERVER_LIST.serversOfVersionCategory(value);
                if (servers.isEmpty()) {
                    continue;
                }
                addEntry(new TitleEntry(Component.nullToEmpty(value.name())));
                for (BCServerInfo server : servers) {
                    addEntry(new ServerSlot(server));
                }
            }

            initScrollY(scrollAmount);
        }

        @Override
        public int getRowWidth() {
            return super.getRowWidth() + 140;
        }

        @Override
        protected void updateSlotAmount(double amount) {
            scrollAmount = amount;
        }
    }

    public static class ServerSlot extends VFPListEntry {
        private final BCServerInfo server;

        public ServerSlot(BCServerInfo server) {
            this.server = server;
        }

        @Override
        public Component getNarration() {
            return Component.nullToEmpty(server.name());
        }

        @Override
        public void mappedMouseClicked(double mouseX, double mouseY, int button) {
            ConnectionUtil.connect(server.name(), server.socket());
            super.mappedMouseClicked(mouseX, mouseY, button);
        }

        @Override
        public void mappedRender(GuiGraphics context, int x, int y, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            final Font textRenderer = Minecraft.getInstance().font;
            context.drawCenteredString(textRenderer, server.name() + ChatFormatting.DARK_GRAY + " [" + server.gameVersion() + "]", entryWidth / 2, entryHeight / 2 - textRenderer.lineHeight / 2, -1);

            if (server.onlineMode()) {
                context.drawString(textRenderer, Component.translatable("base.viafabricplus.online_mode").withStyle(ChatFormatting.GREEN), 1, 1, -1);
            }
            final String serverIP = server.socket();
            final String playerText = server.playerCount() + "/" + server.playerLimit();

            context.drawString(textRenderer, Component.literal(serverIP).withStyle(ChatFormatting.DARK_GRAY), entryWidth - textRenderer.width(serverIP) - 1, entryHeight - textRenderer.lineHeight - 1, -1);
            context.drawString(textRenderer, playerText, entryWidth - textRenderer.width(playerText) - 1, 1, -1);
        }
    }

}
