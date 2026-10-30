package ru.he3hauka.toolvisuals.client.optimization.scene;

import java.util.*;
import java.util.function.Supplier;

public final class TickSnapshot<T> {
    private Object scope;
    private long tick=Long.MIN_VALUE;
    private List<T> values=List.of();
    public List<T> get(Object scope,long tick,Supplier<List<T>> source){if(this.scope!=scope||this.tick!=tick){var next=List.copyOf(source.get());this.scope=scope;this.tick=tick;values=next;}return values;}
    public void clear(){scope=null;tick=Long.MIN_VALUE;values=List.of();}
}
