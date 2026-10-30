package ru.he3hauka.toolvisuals.client.hand;

import ru.he3hauka.toolvisuals.client.config.ClientConfig;

public final class HandProfiles {
    private ClientConfig source;
    private long revision=-1;
    private HandPose right,left;

    public HandPose pose(ClientConfig config,HandSide side) {
        if(source!=config||revision!=config.revision()) {
            right=new HandPose(config::value,HandSide.RIGHT);left=new HandPose(config::value,HandSide.LEFT);
            source=config;revision=config.revision();
        }
        return side==HandSide.RIGHT?right:left;
    }
}
