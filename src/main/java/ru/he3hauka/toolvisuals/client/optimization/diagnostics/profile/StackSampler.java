package ru.he3hauka.toolvisuals.client.optimization.diagnostics.profile;

import java.util.ArrayList;
import ru.he3hauka.toolvisuals.client.optimization.diagnostics.capture.FreezeEvent;
import ru.he3hauka.toolvisuals.client.optimization.diagnostics.capture.FreezeMonitor;

public final class StackSampler {
    private StackSampler() {}
    public static FreezeEvent.StackSample capture(FreezeMonitor.Watch watch,long now) {
        var info=RuntimeProbe.thread(watch.threadId());
        if(info==null)return new FreezeEvent.StackSample(now-watch.started(),watch.phase(),"TERMINATED",-1,java.util.List.of());
        var stack=new ArrayList<String>();
        for(var frame:info.getStackTrace()) {
            String file=frame.getFileName();
            if(file!=null){int index=Math.max(file.lastIndexOf('/'),file.lastIndexOf('\\'));file=file.substring(index+1);}
            stack.add(frame.getClassName()+'.'+frame.getMethodName()+'('+(frame.isNativeMethod()?"Native":file==null?"Unknown":file+':'+frame.getLineNumber())+')');
        }
        return new FreezeEvent.StackSample(now-watch.started(),watch.phase(),info.getThreadState().name(),info.getLockOwnerId(),stack);
    }
}
