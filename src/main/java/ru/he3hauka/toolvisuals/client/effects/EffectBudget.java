package ru.he3hauka.toolvisuals.client.effects;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.module.VisualSetting;
import ru.he3hauka.toolvisuals.client.optimization.Optimizations;
import ru.he3hauka.toolvisuals.client.optimization.frame.FrameMonitor;

import static ru.he3hauka.toolvisuals.client.module.VisualSetting.*;

public final class EffectBudget {
    private EffectBudget() {}
    public static int limit(VisualSetting setting,int fallback){return Optimizations.enabled(OPT_EFFECT_BUDGET)?(int)ToolVisualsClient.CONFIG.value(setting):fallback;}
    public static int quality(){return Optimizations.enabled(OPT_EFFECT_BUDGET)?(int)ToolVisualsClient.CONFIG.value(OPT_EFFECT_QUALITY):0;}
    public static int segments(double distanceSquared,int full){return segments(distanceSquared,full,Math.max(quality(),FrameMonitor.detail()));}
    public static int segments(double distanceSquared,int full,int quality){
        if(distanceSquared<24*24||quality==0)return full;
        return Math.max(16,full/(quality==2?4:2));
    }
}
