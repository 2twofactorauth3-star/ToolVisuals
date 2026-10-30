package ru.he3hauka.toolvisuals.client.armor.render;

public final class ArmorRenderContext {
    private static final ThreadLocal<Object> CURRENT=new ThreadLocal<>();
    private ArmorRenderContext() {}
    public static Object current(){return CURRENT.get();}
    public static Object enter(Object state){var previous=CURRENT.get();CURRENT.set(state);return previous;}
    public static void restore(Object previous){if(previous==null)CURRENT.remove();else CURRENT.set(previous);}
}
