package ru.he3hauka.toolvisuals.client.cape.ui;

import java.nio.file.Path;
import java.util.function.Consumer;
import ru.he3hauka.toolvisuals.client.gui.dialog.FileDialogs;
import ru.he3hauka.toolvisuals.client.cape.image.CapeImages;

final class CapeFileDialog {
    private CapeFileDialog() {}
    static void choose(boolean save, String title, Consumer<Path> chosen, Runnable failed) {
        choose(save,false,title,chosen,failed);
    }
    static void choose(boolean save,boolean animated,String title,Consumer<Path> chosen,Runnable failed) {
        FileDialogs.choose(save,title,"Images",save?"png;gif":CapeImages.EXTENSIONS,animated?"ToolVisuals-Cape.gif":"ToolVisuals-Cape.png",
                path -> chosen.accept(CapeFilePaths.selected(path.toString(),save,animated)),failed);
    }
}
