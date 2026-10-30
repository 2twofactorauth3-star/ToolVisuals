package ru.he3hauka.toolvisuals.client.gui.text;

import ru.he3hauka.toolvisuals.client.diagnostics.error.ClientErrors;

import java.util.concurrent.atomic.AtomicBoolean;

import ru.he3hauka.toolvisuals.client.gui.render.UiTextRasterizer;

public final class FontWarmup {
    private static final AtomicBoolean STARTED=new AtomicBoolean();
    private FontWarmup() {}
    public static void start(){
        if(!STARTED.compareAndSet(false,true))return;
        var worker=new Thread(()->{
            try{
                String sample="ToolVisuals 0123456789 FPS Ping HUD Поиск Настройки Игрок Мир Привет ёйруф";
                for(int pixels:new int[]{11,12,13,14,15,16,18,20,22,24,28,30,36,45,48,60,64})UiTextRasterizer.rasterize(sample,pixels);
            }catch(RuntimeException error){ClientErrors.report("Warm up UI font",error);}
        },"ToolVisuals font warmup");
        worker.setDaemon(true);worker.start();
    }
}
