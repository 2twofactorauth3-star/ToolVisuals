package ru.he3hauka.toolvisuals.client.compatibility.input;

public record MouseButtonEvent(double x,double y,MouseButtonInfo buttonInfo) implements InputWithModifiers {
    public int button(){return buttonInfo.button();}
    @Override public int modifiers(){return buttonInfo.modifiers();}
}
