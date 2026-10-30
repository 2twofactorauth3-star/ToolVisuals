package ru.he3hauka.toolvisuals.client.auction;

import java.util.*;
import java.util.random.RandomGenerator;

public final class AuctionRunner {
    public interface Environment {
        long now();
        boolean idle();
        MenuView menu();
        void command(String command);
        void click(int container, int slot, int button);
        void close(int container);
        void notice(String key, Object... args);
    }
    public static final class MenuView {
        public final int id;
        public final AuctionPlan.MenuKey key;
        public final Map<Integer,AuctionPlan.ButtonKey> buttons;
        public final boolean emptyCursor;
        public MenuView(int id, AuctionPlan.MenuKey key, Map<Integer,AuctionPlan.ButtonKey> buttons, boolean emptyCursor) {
            this.id=id; this.key=key; this.buttons=Map.copyOf(buttons); this.emptyCursor=emptyCursor;
        }
    }
    private enum Phase { IDLE, COMMAND_SEND, COMMAND_WAIT, MANUAL_SEND, MANUAL_READY, MANUAL_ACK, MANUAL_FINISH }
    private enum Route { COMMANDS, MANUAL }
    private static final long RESPONSE_TIMEOUT = 10000;
    private static final String SUCCESS = "relisted|successfully relist|перевыставлены|перевыставлено|успешно перевыстав";
    private static final String FAILURE = "unknown command|unknown or incomplete command|неизвестная команда|неизвестная или неполная команда|нет такой команды|no permission|нет прав|недостаточно прав";
    private final Environment env;
    private final RandomGenerator random;
    private final Map<AuctionPlan,Route> successfulRoutes = new WeakHashMap<>();
    private final Set<Integer> ownedMenus = new HashSet<>();
    private AuctionPlan plan;
    private Object connection;
    private Phase phase = Phase.IDLE;
    private Route route;
    private boolean triedCommands, triedManual, confirmed, clicked, acknowledged, rejected;
    private int index, waitingContainer;
    private long nextAt, deadline, earliest;
    private String status = "off";
    public AuctionRunner(Environment env, RandomGenerator random) { this.env=env; this.random=random; }
    public String status() { return status; }
    public boolean running() { return phase != Phase.IDLE; }
    public long remainingSeconds() { return Math.max(0,(nextAt-env.now()+999)/1000); }
    public void tick(AuctionPlan selected, boolean enabled, Object connection, boolean recording) {
        long now=env.now();
        if (!enabled || selected == null || connection == null || !selected.ready() || recording) {
            clear(); plan=null; this.connection=connection;
            status=recording?"recording":!enabled?"off":selected==null?"no_profile":"incomplete"; return;
        }
        if (plan != selected || this.connection != connection) {
            clear(); plan=selected; this.connection=connection; nextAt=now+plan.delayMillis(random); status="waiting";
        }
        if (phase == Phase.IDLE) {
            if (now < nextAt || !env.idle()) return;
            triedCommands=triedManual=confirmed=clicked=false; ownedMenus.clear();
            var preferred=successfulRoutes.get(plan);
            start(plan.mode==AuctionPlan.Mode.MANUAL || (plan.mode==AuctionPlan.Mode.BOTH && !plan.manual.isEmpty()
                    && (plan.commands.isEmpty() || preferred==Route.MANUAL)) ? Route.MANUAL : Route.COMMANDS);
        }
        if (rejected) { fail("rejected",true); return; }
        switch (phase) {
            case COMMAND_SEND -> {
                if(now<earliest)return;
                if(!env.idle()){interrupt();return;}
                acknowledged=false; phase=Phase.COMMAND_WAIT; deadline=now+RESPONSE_TIMEOUT;
                env.command(plan.commands.get(index));
            }
            case COMMAND_WAIT -> {
                if (acknowledged) {
                    confirmed=true;
                    if(++index==plan.commands.size()) succeed();
                    else { earliest=now+1000; phase=Phase.COMMAND_SEND; }
                } else if(now>=deadline) fail("unconfirmed",false);
            }
            case MANUAL_SEND -> {
                if(!env.idle()){interrupt();return;}
                phase=Phase.MANUAL_READY; index=1; earliest=now+plan.manual.get(index).waitMillis; deadline=earliest+RESPONSE_TIMEOUT;
                env.command(plan.manual.getFirst().command);
            }
            case MANUAL_READY -> {
                if(now<earliest)return;
                var menu=env.menu(); var step=plan.manual.get(index);
                if(menu!=null&&!menu.emptyCursor){fail("cursor",false);return;}
                if(menu!=null&&step.menu.matches(menu.key)&&step.item.matches(menu.buttons.get(step.slot))) {
                    waitingContainer=menu.id; ownedMenus.add(menu.id); acknowledged=false; clicked=true;
                    phase=Phase.MANUAL_ACK; earliest=now+250; deadline=now+RESPONSE_TIMEOUT;
                    env.click(menu.id,step.slot,step.button);
                } else if(now>=deadline) fail("menu_mismatch",false);
            }
            case MANUAL_ACK -> {
                if(acknowledged&&now>=earliest) {
                    if(++index==plan.manual.size()){phase=Phase.MANUAL_FINISH;earliest=now+750;}
                    else {phase=Phase.MANUAL_READY;earliest=now+plan.manual.get(index).waitMillis;deadline=earliest+RESPONSE_TIMEOUT;}
                } else if(now>=deadline) fail("unconfirmed",false);
            }
            case MANUAL_FINISH -> {if(now>=earliest)succeed();}
            default -> {}
        }
    }
    public boolean requestNow() {
        if(plan==null || phase!=Phase.IDLE)return false;
        nextAt=env.now(); return true;
    }
    public void message(String text) {
        if(!running())return;
        if(matches(text,FAILURE)||matches(text,plan.failureText)) {rejected=true;return;}
        if(phase==Phase.COMMAND_WAIT && matches(text,plan.successText.isBlank()?SUCCESS:plan.successText)) acknowledged=true;
    }
    public void serverMenuResponse(int container, boolean opened) {
        if(phase==Phase.MANUAL_ACK && (opened || container==waitingContainer)) {
            acknowledged=true; if(opened)ownedMenus.add(container);
        }
    }
    public void interrupt() {
        if(!running())return;
        phase=Phase.IDLE; ownedMenus.clear(); rejected=false;
        if(plan!=null)nextAt=env.now()+plan.delayMillis(random);
        status="interrupted"; env.notice("interrupted");
    }
    private void start(Route route) {
        this.route=route; rejected=acknowledged=false; index=0; earliest=env.now();
        if(route==Route.COMMANDS) {triedCommands=true;phase=Phase.COMMAND_SEND;status="commands";}
        else {triedManual=true;phase=Phase.MANUAL_SEND;status="manual";}
    }
    private void fail(String reason, boolean explicitRejection) {
        if(explicitRejection && !confirmed && !clicked && plan.mode==AuctionPlan.Mode.BOTH) {
            if(!triedManual&&!plan.manual.isEmpty()) {env.notice("fallback_manual");start(Route.MANUAL);return;}
            if(!triedCommands&&!plan.commands.isEmpty()) {env.notice("fallback_commands");start(Route.COMMANDS);return;}
        }
        phase=Phase.IDLE; ownedMenus.clear(); rejected=false; nextAt=env.now()+plan.delayMillis(random);
        status=reason; env.notice(reason);
    }
    private void succeed() {
        successfulRoutes.put(plan,route);
        var menu=env.menu(); if(menu!=null&&ownedMenus.contains(menu.id)&&menu.emptyCursor)env.close(menu.id);
        phase=Phase.IDLE; ownedMenus.clear(); nextAt=env.now()+plan.delayMillis(random); status="done";
        env.notice("done",plan.id);
    }
    private void clear() { phase=Phase.IDLE; ownedMenus.clear(); nextAt=0; rejected=false; }
    private static boolean matches(String text, String patterns) {
        if(text==null||patterns==null||patterns.isBlank())return false;
        String normalized=text.toLowerCase(Locale.ROOT);
        for(String pattern:patterns.split("\\|"))if(!pattern.isBlank()&&normalized.contains(pattern.strip().toLowerCase(Locale.ROOT)))return true;
        return false;
    }
}
