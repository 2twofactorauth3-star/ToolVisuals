package ru.he3hauka.toolvisuals.client.compatibility.gui;

import java.net.URI;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;

public final class ChatEvents {
    private ChatEvents() {}
    public static ClickEvent runCommand(String command){return new ClickEvent(ClickEvent.Action.RUN_COMMAND,command);}
    public static ClickEvent suggestCommand(String command){return new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,command);}
    public static ClickEvent copy(String text){return new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD,text);}
    public static ClickEvent url(URI uri){return new ClickEvent(ClickEvent.Action.OPEN_URL,uri.toString());}
    public static ClickEvent url(String uri){return new ClickEvent(ClickEvent.Action.OPEN_URL,uri);}
    public static HoverEvent hover(Component text){return new HoverEvent(HoverEvent.Action.SHOW_TEXT,text);}
}
