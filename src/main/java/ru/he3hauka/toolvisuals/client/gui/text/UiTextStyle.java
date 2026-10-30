package ru.he3hauka.toolvisuals.client.gui.text;

public enum UiTextStyle {
    REGULAR(1f), MENU(1f), BRAND(2f);

    public final float weight;

    UiTextStyle(float weight) {
        this.weight=weight;
    }
}
