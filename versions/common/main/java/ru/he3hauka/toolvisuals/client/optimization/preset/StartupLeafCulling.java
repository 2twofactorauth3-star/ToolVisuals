package ru.he3hauka.toolvisuals.client.optimization.preset;

public final class StartupLeafCulling {
    private StartupLeafCulling(){}
    public static void apply(){
        ca.fxco.moreculling.MoreCulling.CONFIG.leavesCullingMode=ca.fxco.moreculling.config.option.LeavesCullingMode.FAST;
        me.shedaniel.autoconfig.AutoConfig.getConfigHolder(ca.fxco.moreculling.config.MoreCullingConfig.class).save();
    }
}
