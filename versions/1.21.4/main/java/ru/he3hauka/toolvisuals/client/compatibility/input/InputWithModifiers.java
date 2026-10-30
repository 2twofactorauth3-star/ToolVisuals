package ru.he3hauka.toolvisuals.client.compatibility.input;

public interface InputWithModifiers {
    int modifiers();
    default boolean hasControlDown(){return (modifiers()&2)!=0;}
    default boolean hasShiftDown(){return (modifiers()&1)!=0;}
    default boolean hasAltDown(){return (modifiers()&4)!=0;}
}
