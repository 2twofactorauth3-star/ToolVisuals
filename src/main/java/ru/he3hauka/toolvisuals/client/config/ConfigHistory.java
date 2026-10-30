package ru.he3hauka.toolvisuals.client.config;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Supplier;

public final class ConfigHistory {
    private static final class State {
        private Supplier<String> source;
        private String encoded;
        State(Supplier<String> source){this.source=source;}
        String json(){if(encoded==null){encoded=source.get();source=null;}return encoded;}
    }
    private final Deque<State> undo=new ArrayDeque<>(),redo=new ArrayDeque<>();
    private State current;
    private String lastGroup;
    private long lastChange=Long.MIN_VALUE;
    public void reset(String state){current=new State(()->state);undo.clear();redo.clear();lastChange=Long.MIN_VALUE;lastGroup=null;}
    public void record(String state,long now){record(state,now,"snapshot");}
    public void record(String state,long now,String group){if(current!=null&&state.equals(current.json()))return;recordDeferred(()->state,now,group);}
    public void recordDeferred(Supplier<String> source,long now,String group){
        var next=new State(source);
        boolean combine=!undo.isEmpty()&&group!=null&&group.equals(lastGroup)&&lastChange!=Long.MIN_VALUE&&now>=lastChange&&now-lastChange<=400_000_000L;
        if(!combine&&current!=null){
            if(next.json().equals(current.json()))return;
            undo.addLast(current);if(undo.size()>50)undo.removeFirst();
        }
        current=next;redo.clear();lastChange=now;lastGroup=group;
    }
    public boolean canUndo(){return !undo.isEmpty();}
    public boolean canRedo(){return !redo.isEmpty();}
    public String undo(){if(!canUndo())return null;redo.addLast(current);current=undo.removeLast();lastChange=Long.MIN_VALUE;return current.json();}
    public String redo(){if(!canRedo())return null;undo.addLast(current);current=redo.removeLast();lastChange=Long.MIN_VALUE;return current.json();}
}
