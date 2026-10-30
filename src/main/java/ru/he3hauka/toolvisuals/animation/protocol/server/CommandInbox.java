package ru.he3hauka.toolvisuals.animation.protocol.server;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import ru.he3hauka.toolvisuals.animation.protocol.PlaybackWire;

public final class CommandInbox {
    public record Command(PlaybackWire.Request request,boolean startAllowed){}
    private Command hello,start,terminal;
    private boolean scheduled,closed;
    public synchronized boolean offer(PlaybackWire.Request request,boolean startAllowed){
        if(closed)return false;var value=new Command(request,startAllowed);
        if(request instanceof PlaybackWire.Hello){if(hello==null||request.requestId()>hello.request.requestId())hello=value;}
        else if(request instanceof PlaybackWire.Start){if(start==null||request.requestId()>start.request.requestId())start=value;if(terminal!=null&&terminal.request.requestId()<request.requestId())terminal=null;}
        else if(terminal==null||request.requestId()>terminal.request.requestId())terminal=value;
        if(scheduled)return false;scheduled=true;return true;
    }
    public synchronized List<Command> drain(){var values=new ArrayList<Command>();if(hello!=null)values.add(hello);if(start!=null)values.add(start);if(terminal!=null)values.add(terminal);values.sort(Comparator.comparingLong(value->value.request.requestId()));hello=start=terminal=null;scheduled=false;return List.copyOf(values);}
    public synchronized void close(){closed=true;hello=start=terminal=null;}
}
