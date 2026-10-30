package com.viaversion.viafabricplus.api.events;

public interface LoadingCycleCallback {

    enum LoadingCycle {

        PRE_SETTINGS_LOAD,

        POST_SETTINGS_LOAD,

        PRE_FILES_LOAD,

        POST_FILES_LOAD,

        PRE_VIAVERSION_LOAD,

        POST_VIAVERSION_LOAD,

        FINAL_LOAD,

        POST_GAME_LOAD

    }

    void onLoadCycle(final LoadingCycle cycle);

}
