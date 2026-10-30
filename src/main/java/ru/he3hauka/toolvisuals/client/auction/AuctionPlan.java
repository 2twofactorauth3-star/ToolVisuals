package ru.he3hauka.toolvisuals.client.auction;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.random.RandomGenerator;

public final class AuctionPlan {
    public enum Mode { COMMANDS, MANUAL, BOTH }
    public static final int MAX_COMMANDS = 16, MAX_STEPS = 32;
    public final String id, server, successText, failureText;
    public final Mode mode;
    public final int minSeconds, maxSeconds;
    public final List<String> commands;
    public final List<Step> manual;

    public AuctionPlan(String id, String server, Mode mode, int minSeconds, int maxSeconds,
                       List<String> commands, List<Step> manual, String successText, String failureText) {
        this.id = text(id, 128); this.server = server(server); this.mode = Objects.requireNonNull(mode);
        if (minSeconds < 1 || maxSeconds > 86400 || minSeconds > maxSeconds) throw new IllegalArgumentException("Invalid interval");
        this.minSeconds = minSeconds; this.maxSeconds = maxSeconds;
        if (commands == null || commands.size() > MAX_COMMANDS || manual == null || manual.size() > MAX_STEPS) throw new IllegalArgumentException("Too many actions");
        this.commands = commands.stream().map(AuctionPlan::command).toList();
        this.manual = List.copyOf(manual);
        if (!manual.isEmpty() && (manual.size() < 2 || manual.getFirst().command.isEmpty()
                || manual.stream().skip(1).anyMatch(s -> !s.command.isEmpty()))) throw new IllegalArgumentException("Expected an opening command followed by clicks");
        this.successText = optionalText(successText, 512); this.failureText = optionalText(failureText, 512);
    }
    public static AuctionPlan empty(String id, String server) { return new AuctionPlan(id, server, Mode.COMMANDS, 300, 300, List.of(), List.of(), "", ""); }
    public boolean ready() { return switch (mode) { case COMMANDS -> !commands.isEmpty(); case MANUAL -> !manual.isEmpty(); case BOTH -> !commands.isEmpty() || !manual.isEmpty(); }; }
    public long delayMillis(RandomGenerator random) { return random.nextLong(minSeconds * 1000L, maxSeconds * 1000L + 1); }
    public AuctionPlan withRecording(String id, List<Step> steps) { return new AuctionPlan(id, server, mode == Mode.COMMANDS && commands.isEmpty() ? Mode.MANUAL : mode,
            minSeconds, maxSeconds, commands, steps, successText, failureText); }
    public static String command(String value) {
        String result = text(value, 256); if (result.startsWith("/")) result = result.substring(1).strip();
        if (result.isEmpty() || result.length()>255 || result.startsWith("/") || result.startsWith(".toolvisuals")) throw new IllegalArgumentException("Invalid command");
        return result;
    }
    public static String server(String address) {
        String result = text(address, 255).toLowerCase(Locale.ROOT);
        if (result.chars().anyMatch(Character::isWhitespace) || result.contains("/") || result.contains("\\")) throw new IllegalArgumentException("Invalid server address");
        if (result.endsWith(":25565")) result = result.substring(0, result.length() - 6);
        if (result.endsWith(".")) result = result.substring(0, result.length() - 1);
        return text(result,255);
    }
    private static String optionalText(String value, int max) { return value == null || value.isBlank() ? "" : text(value,max); }
    public static String text(String value, int max) {
        if (value == null || value.isBlank() || value.length() > max || value.codePoints().anyMatch(Character::isISOControl)) throw new IllegalArgumentException("Invalid text");
        return value.strip();
    }
    public static final class MenuKey {
        public final String type, title;
        public final int slots;
        public MenuKey(String type, String title, int slots) {
            this.type = text(type,128); this.title = optionalText(title,512);
            if (slots < 1 || slots > 256) throw new IllegalArgumentException("Invalid menu size"); this.slots = slots;
        }
        public boolean matches(MenuKey other) { return other != null && type.equals(other.type) && title.equals(other.title) && slots == other.slots; }
    }
    public static final class ButtonKey {
        public final String item, name;
        public ButtonKey(String item, String name) { this.item = text(item,128); this.name = optionalText(name,512); }
        public boolean matches(ButtonKey other) { return other != null && item.equals(other.item) && name.equals(other.name); }
    }
    public static final class Step {
        public final String command;
        public final MenuKey menu;
        public final ButtonKey item;
        public final int slot, button, waitMillis;
        private Step(String command, MenuKey menu, ButtonKey item, int slot, int button, int waitMillis) {
            this.command = command; this.menu = menu; this.item = item; this.slot = slot; this.button = button; this.waitMillis = waitMillis;
        }
        public static Step command(String command) { return new Step(AuctionPlan.command(command),null,null,-1,0,0); }
        public static Step click(MenuKey menu, ButtonKey item, int slot, int button, int waitMillis) {
            Objects.requireNonNull(menu); Objects.requireNonNull(item);
            if (slot < 0 || slot >= menu.slots || button < 0 || button > 1 || waitMillis < 250 || waitMillis > 10000) throw new IllegalArgumentException("Invalid click");
            return new Step("",menu,item,slot,button,waitMillis);
        }
    }
}
