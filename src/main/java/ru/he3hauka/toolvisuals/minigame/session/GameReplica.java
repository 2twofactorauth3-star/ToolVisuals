package ru.he3hauka.toolvisuals.minigame.session;

import java.util.UUID;

import ru.he3hauka.toolvisuals.minigame.GameKind;
import ru.he3hauka.toolvisuals.minigame.GameRound;

import static ru.he3hauka.toolvisuals.minigame.network.MiniGameWire.*;

public final class GameReplica {
    public static final long RETRY = 1_000_000_000L, TIMEOUT = 8_000_000_000L;
    public final UUID session, self;
    private Command pending;
    private Message room, invitation;
    private GameRound round;
    private UUID consent = NONE;
    private long sequence, started, sent, invitationUntil;
    private boolean enabled, ready;

    public GameReplica(UUID session, UUID self) { this.session = session; this.self = self; }
    public boolean ready() { return enabled && ready; }
    public boolean busy() { return pending != null; }
    public boolean enabled() { return enabled; }
    public Message room() { return room; }
    public Message invitation() { return invitation; }
    public GameRound round() { return round; }
    public Command pending() { return pending; }

    public void enable(boolean value, long now) {
        enabled = value; ready = false;
        if (!value) clearRoom();
        submit(Action.HELLO, NONE, GameKind.TIC_TAC_TOE, 0, value ? 1 : 0, 0, now);
    }

    public boolean invite(UUID target, GameKind kind, long now) {
        if (!ready() || busy() || room != null || invitation != null || target.equals(self) || target.equals(NONE)) return false;
        submit(Action.INVITE, target, kind, 0, 0, 0, now); return true;
    }

    public boolean answer(UUID id, boolean accept, long now) {
        if (!ready() || criticalRequest() || invitation == null || !invitation.room().equals(id) || now >= invitationUntil) return false;
        consent = accept ? id : NONE;
        submit(Action.ANSWER, id, invitation.kind(), 0, accept ? 1 : 0, 0, now);
        invitation = null;
        return true;
    }

    public boolean move(int first, int second, long now) {
        if (!ready() || busy() || room == null || round == null || round.winner != 0 || round.turn != ownTurn()) return false;
        round.play(first, second);
        submit(Action.MOVE, room.room(), room.kind(), round.revision, first, second, now); return true;
    }

    public void leave(long now) {
        clearRoom();
        if (enabled) submit(Action.LEAVE, NONE, GameKind.TIC_TAC_TOE, 0, 0, 0, now);
    }

    public void heartbeat(long now) {
        if (!enabled || busy()) return;
        if (room != null) submit(Action.POLL, room.room(), room.kind(), 0, 0, 0, now);
        else submit(Action.HELLO, NONE, GameKind.TIC_TAC_TOE, 0, 1, 0, now);
    }

    private void submit(Action action, UUID reference, GameKind kind, int revision, int first, int second, long now) {
        pending = new Command(session, ++sequence, action, reference, kind, revision, first, second); started = now; sent = Long.MIN_VALUE;
    }
    private boolean criticalRequest() { return pending != null && pending.action() != Action.HELLO && pending.action() != Action.POLL; }

    public Command due(long now) {
        if (pending == null || now - started >= TIMEOUT) return null;
        if (sent != Long.MIN_VALUE && now - sent < RETRY) return null;
        sent = now; return pending;
    }

    public boolean timedOut(long now) {
        if (pending == null || now - started < TIMEOUT) return false;
        pending = null; ready = false; clearRoom(); return true;
    }

    public void expireInvitation(long now) { if (invitation != null && now >= invitationUntil) { invitation = null; if (round == null) consent = NONE; } }
    public int ownTurn() { return room != null && room.first().equals(self) ? 1 : 2; }

    public boolean receive(Message message, long now, boolean acceptInvitations) {
        if (!session.equals(message.session()) || !enabled && message.code() != Code.OFF) return false;
        Command acknowledged = pending != null && message.reply() == pending.id() ? pending : null;
        boolean changed = false;
        switch (message.code()) {
            case READY, OFF -> {
                if (acknowledged == null || acknowledged.action() != Action.HELLO) return false;
                ready = message.code() == Code.READY && enabled;
            }
            case INVITED -> {
                if (!message.second().equals(self) || room != null || !enabled) return false;
                ready = true;
                if (invitation == null || !invitation.room().equals(message.room())) {
                    invitation = message; invitationUntil = now + message.seconds() * 1_000_000_000L; changed = acceptInvitations;
                }
            }
            case WAITING -> {
                if (!message.first().equals(self) || acknowledged == null || acknowledged.action() != Action.INVITE && acknowledged.action() != Action.POLL) return false;
                if (acknowledged.action() == Action.INVITE && acknowledged.id() != message.invitationRequest()) return false;
                if (acknowledged.action() == Action.INVITE && (!acknowledged.reference().equals(message.second()) || acknowledged.kind() != message.kind())) return false;
                if (room == null || !room.room().equals(message.room())) changed = true;
                if (round == null) { room = message; consent = message.room(); }
            }
            case MATCH -> {
                if (!message.first().equals(self) && !message.second().equals(self)) return false;
                boolean initiating = pending != null && pending.action() == Action.INVITE && message.first().equals(self)
                        && pending.id() == message.invitationRequest() && pending.reference().equals(message.second()) && pending.kind() == message.kind();
                if (!message.room().equals(consent) && (room == null || !room.room().equals(message.room())) && !initiating) return false;
                if (room != null && room.room().equals(message.room()) && (room.kind() != message.kind() || room.seed() != message.seed()
                        || !room.first().equals(message.first()) || !room.second().equals(message.second()))) return false;
                int revision = message.history().length / 2;
                if (round == null || revision > round.revision) {
                    round = new GameRound(message.kind(), message.seed(), message.history()); room = message;
                    invitation = null; consent = message.room(); changed = true;
                }
            }
            default -> {
                boolean relevant = acknowledged != null || !message.room().equals(NONE)
                        && (room != null && room.room().equals(message.room()) || invitation != null && invitation.room().equals(message.room()) || consent.equals(message.room()));
                if (!relevant) return false;
                if (message.code() != Code.INVALID && message.code() != Code.STALE) {
                    if (acknowledged != null && acknowledged.action() == Action.INVITE && room == null) consent = NONE;
                    else clearRoom();
                    changed = true;
                }
            }
        }
        if (acknowledged != null) {
            if (acknowledged.action() == Action.ANSWER && message.code() != Code.MATCH) consent = NONE;
            if (pending == acknowledged) pending = null;
        }
        return changed;
    }

    private void clearRoom() { room = invitation = null; round = null; consent = NONE; }
}
