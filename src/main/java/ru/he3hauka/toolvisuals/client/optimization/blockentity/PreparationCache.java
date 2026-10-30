package ru.he3hauka.toolvisuals.client.optimization.blockentity;

import java.util.function.Supplier;

public final class PreparationCache<T> {
    private Object scope,key;
    private long tick=Long.MIN_VALUE,revision=Long.MIN_VALUE;
    private boolean variant;
    private T value;
    private final Object[] derived=new Object[2];

    public boolean matches(Object scope,Object key,long tick,long revision,boolean variant){
        return value!=null&&this.scope==scope&&this.key==key&&this.tick==tick&&this.revision==revision&&this.variant==variant;
    }
    public T get(Object scope,Object key,long tick,long revision,boolean variant,Supplier<T> source){
        if(matches(scope,key,tick,revision,variant))return value;
        T next=source.get();
        this.scope=scope;this.key=key;this.tick=tick;this.revision=revision;this.variant=variant;
        value=next;java.util.Arrays.fill(derived,null);
        return next;
    }
    public Object derive(T source,int index,Supplier<Object> factory){
        if(value!=source)return factory.get();
        if(derived[index]==null)derived[index]=factory.get();
        return derived[index];
    }
}
