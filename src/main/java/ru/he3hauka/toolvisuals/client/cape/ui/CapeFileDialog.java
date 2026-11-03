package ru.he3hauka.toolvisuals.client.cape.ui;

import java.nio.file.Path;
import java.util.function.Consumer;
import ru.he3hauka.toolvisuals.client.gui.dialog.FileDialogs;

final class CapeFileDialog {
    private CapeFileDialog() {}
    static void choose(boolean save, String title, Consumer<Path> chosen, Runnable failed) {
        FileDialogs.choose(save, title, "PNG images", "png", "ToolVisuals-Cape.png",
                path -> chosen.accept(CapeFilePaths.selected(path.toString(), save)), failed);
    }
}
