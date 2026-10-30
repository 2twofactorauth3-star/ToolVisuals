package ru.he3hauka.toolvisuals.client.optimization.hud;

import java.util.function.Supplier;

public final class RevisionValue<T> {
    private Object scope;
    private long revision=Long.MIN_VALUE;
    private T value;
    public T get(Object scope,long revision,Supplier<T> source){if(this.scope!=scope||this.revision!=revision||value==null){T next=source.get();this.scope=scope;this.revision=revision;value=next;}return value;}
    public void clear(){scope=null;revision=Long.MIN_VALUE;value=null;}
}
