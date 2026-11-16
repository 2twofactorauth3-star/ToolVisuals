package ru.he3hauka.toolvisuals.client.optimization.preset;

import ca.fxco.moreculling.MoreCulling;
import ca.fxco.moreculling.config.MoreCullingConfig;
import ca.fxco.moreculling.config.option.LeavesCullingMode;
import me.shedaniel.autoconfig.AutoConfig;


public final class StartupLeafCulling {
    private StartupLeafCulling(){}
    public static void apply(){
        MoreCulling.CONFIG.leavesCullingMode=LeavesCullingMode.FAST;
        AutoConfig.getConfigHolder(MoreCullingConfig.class).save();
    }
}
