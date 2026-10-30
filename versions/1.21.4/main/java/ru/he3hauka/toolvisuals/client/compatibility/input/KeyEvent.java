package ru.he3hauka.toolvisuals.client.compatibility.input;

public record KeyEvent(int key,int scancode,int modifiers) implements InputWithModifiers {
    public boolean isConfirmation(){return key==257||key==335||key==32;}
}
