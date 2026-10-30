package ru.he3hauka.toolvisuals.client.gui.module;

 
public final class UiMenuLayout {
    public final int x, y, width, height, moduleWidth, paneWidth;
    public final boolean compact;
    public UiMenuLayout(int viewportWidth, int viewportHeight, boolean settings) {
        int available = Math.max(240, viewportWidth - 32);
        int main = Math.min(720, available), pane = 0;
        compact = settings && available < 900;
        if (settings && !compact) { pane = 360; main = Math.min(720, available - pane - 40); }
        moduleWidth = main; width = settings && !compact ? main + pane + 40 : main;
        height = Math.max(240, Math.min(settings ? 568 : 500, viewportHeight - 32));
        paneWidth = compact ? width - 36 : pane;
        x = (viewportWidth - width) / 2; y = (viewportHeight - height) / 2;
    }
}
