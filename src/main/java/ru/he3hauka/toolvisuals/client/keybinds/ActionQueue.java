package ru.he3hauka.toolvisuals.client.keybinds;

import java.util.ArrayDeque;

public final class ActionQueue {
    public static final int LIMIT=8;
    private final ArrayDeque<ActionBind> pending=new ArrayDeque<>();
    private Object connection;
    private long next;
    public void scope(Object current,boolean allowed){if(!allowed||connection!=current){pending.clear();next=0;}connection=allowed?current:null;}
    public boolean offer(ActionBind bind){if(connection==null||pending.size()>=LIMIT)return false;pending.addLast(bind);return true;}
    public ActionBind poll(long now){if(pending.isEmpty()||now<next)return null;next=now+250_000_000L;return pending.removeFirst();}
    public int size(){return pending.size();}
}
