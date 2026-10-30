package ru.he3hauka.toolvisuals.client.diagnostics.error;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

final class ErrorRepeats {
    record Notice(String operation,Throwable error,long repeats){}
    private record Key(String operation,String type,String message,StackTraceElement origin){}
    private static final class Entry {
        final String operation;
        final Throwable error;
        long time,repeats;
        Entry(String operation,Throwable error,long time){this.operation=operation;this.error=error;this.time=time;}
        Notice notice(){var notice=new Notice(operation,error,repeats);repeats=0;return notice;}
    }
    private final int capacity;
    private final long interval;
    private final LinkedHashMap<Key,Entry> entries=new LinkedHashMap<>(16,.75f,true);

    ErrorRepeats(int capacity,long interval){if(capacity<1||interval<1)throw new IllegalArgumentException("Invalid error log bounds");this.capacity=capacity;this.interval=interval;}

    synchronized List<Notice> record(String operation,Throwable error,long now){
        var cause=error;
        for(int depth=0;depth<8&&cause.getCause()!=null&&cause.getCause()!=cause;depth++)cause=cause.getCause();
        String message=cause.getMessage();if(message==null)message="";if(message.length()>512)message=message.substring(0,512);
        var stack=cause.getStackTrace();var key=new Key(operation,cause.getClass().getName(),message,stack.length==0?null:stack[0]);
        var entry=entries.get(key);
        if(entry!=null){
            entry.repeats++;
            if(now-entry.time<interval)return List.of();
            entry.time=now;return List.of(entry.notice());
        }
        var notices=new ArrayList<Notice>(2);
        if(entries.size()>=capacity){var oldest=entries.pollFirstEntry().getValue();if(oldest.repeats>0)notices.add(oldest.notice());}
        entries.put(key,new Entry(operation,error,now));notices.add(new Notice(operation,error,0));return notices;
    }

    synchronized List<Notice> drain(){
        var notices=new ArrayList<Notice>();
        for(var entry:entries.values())if(entry.repeats>0)notices.add(entry.notice());
        entries.clear();return notices;
    }
}
