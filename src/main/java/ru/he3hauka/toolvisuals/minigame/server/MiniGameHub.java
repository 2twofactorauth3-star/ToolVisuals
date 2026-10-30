package ru.he3hauka.toolvisuals.minigame.server;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import ru.he3hauka.toolvisuals.minigame.GameKind;
import ru.he3hauka.toolvisuals.minigame.GameRound;
import ru.he3hauka.toolvisuals.minigame.network.MiniGameWire;

import static ru.he3hauka.toolvisuals.minigame.network.MiniGameWire.*;

public final class MiniGameHub {
    public static final int MAX_PLAYERS = 1024, MAX_ROOMS = 512;
    public static final long INVITATION_TIME = 30_000_000_000L, GAME_IDLE = 180_000_000_000L, PEER_IDLE = 90_000_000_000L;
    public record Delivery(UUID recipient, Message message) {}
    private static final class Peer {
        final UUID id;
        volatile UUID session;
        volatile boolean enabled;
        volatile long seen;
        long invited = Long.MIN_VALUE, received = Long.MIN_VALUE;
        Command last;
        Message reply;
        Peer(UUID id, UUID session, long now) { this.id = id; this.session = session; this.seen = now; }
    }
    private static final class Room {
        final UUID id = UUID.randomUUID(), first, second;
        final long created, requestId;
        final GameKind kind;
        GameRound round;
        volatile long activity;
        boolean started, closed;
        Room(UUID first, UUID second, GameKind kind, long seed, long requestId, long now) {
            this.first = first; this.second = second; this.kind = kind; this.round = new GameRound(kind, seed); this.requestId = requestId; created = activity = now;
        }
        UUID other(UUID actor) { return actor.equals(first) ? second : first; }
    }

    private final Object directory = new Object();
    private final Map<UUID, Peer> peers = new ConcurrentHashMap<>();
    private final Map<UUID, Room> rooms = new ConcurrentHashMap<>(), membership = new ConcurrentHashMap<>();

    public UUID partner(UUID actor, Command command) {
        if (command.action() == Action.INVITE) return command.reference();
        return partner(actor);
    }
    public UUID partner(UUID actor) {
        var room = membership.get(actor); return room == null ? NONE : room.other(actor);
    }

    public List<Delivery> handle(UUID actor, Command command, boolean visible, long now) {
        Peer peer = peers.get(actor);
        if (peer == null && command.action() == Action.HELLO) synchronized (directory) {
            peer = peers.get(actor);
            if (peer == null && peers.size() < MAX_PLAYERS) { peer = new Peer(actor, command.session(), now); peers.put(actor, peer); }
        }
        if (peer == null) return List.of(new Delivery(actor, empty(command, Code.UNAVAILABLE)));
        synchronized (peer) {
            if (peers.get(actor) != peer) return List.of();
            if (!peer.session.equals(command.session())) return List.of(new Delivery(actor, empty(command, Code.STALE)));
            peer.seen = now;
            if (peer.last != null && command.id() <= peer.last.id()) {
                if (command.equals(peer.last)) {
                    var reply = replay(peer, command, now); var output = new ArrayList<Delivery>(); output.add(new Delivery(actor, reply));
                    if (reply.code() == Code.WAITING) {
                        var room = membership.get(actor); var target = peers.get(reply.second());
                        if (room != null && target != null) synchronized (room) {
                            if (!room.closed && !room.started) output.add(new Delivery(target.id, snapshot(target, 0, room, now, Code.INVITED)));
                        }
                    }
                    return output;
                }
                return List.of(new Delivery(actor, empty(command, Code.STALE)));
            }
            var result = process(peer, command, visible, now);
            peer.last = command;
            peer.reply = result.stream().filter(value -> value.recipient().equals(actor) && value.message().reply() == command.id()).map(Delivery::message).findFirst().orElse(empty(command, Code.INVALID));
            return result;
        }
    }

    private Message replay(Peer peer, Command command, long now) {
        var room = membership.get(peer.id);
        if (peer.reply.code() == Code.MATCH || peer.reply.code() == Code.WAITING) {
            if (room == null || !room.id.equals(peer.reply.room())) return empty(command, Code.CLOSED);
            synchronized (room) { return snapshot(peer, command.id(), room, now, room.started ? Code.MATCH : Code.WAITING); }
        }
        return peer.reply;
    }

    private List<Delivery> process(Peer peer, Command command, boolean visible, long now) {
        if (command.action() == Action.HELLO) {
            synchronized (directory) { peer.enabled = command.first() == 1; }
            var output = peer.enabled ? new ArrayList<Delivery>() : new ArrayList<>(cancel(peer.id, Code.CLOSED));
            output.add(new Delivery(peer.id, empty(command, peer.enabled ? Code.READY : Code.OFF))); return output;
        }
        if (!peer.enabled) return List.of(new Delivery(peer.id, empty(command, Code.OFF)));
        if (command.action() == Action.INVITE) {
            var target = peers.get(command.reference());
            if (!visible || target == null || !target.enabled || target == peer || now - target.seen >= PEER_IDLE)
                return List.of(new Delivery(peer.id, empty(command, Code.UNAVAILABLE)));
            Room room;
            synchronized (directory) {
                if (!target.enabled || peers.get(target.id) != target) return List.of(new Delivery(peer.id, empty(command, Code.UNAVAILABLE)));
                if (peer.invited != Long.MIN_VALUE && now - peer.invited < 5_000_000_000L || target.received != Long.MIN_VALUE && now - target.received < 5_000_000_000L)
                    return List.of(new Delivery(peer.id, empty(command, Code.BUSY)));
                if (membership.containsKey(peer.id) || membership.containsKey(target.id)) return List.of(new Delivery(peer.id, empty(command, Code.BUSY)));
                if (rooms.size() >= MAX_ROOMS) return List.of(new Delivery(peer.id, empty(command, Code.CAPACITY)));
                room = new Room(peer.id, target.id, command.kind(), UUID.randomUUID().getLeastSignificantBits(), command.id(), now);
                peer.invited = target.received = now;
                rooms.put(room.id, room); membership.put(peer.id, room); membership.put(target.id, room);
            }
            return List.of(new Delivery(peer.id, snapshot(peer, command.id(), room, now, Code.WAITING)),
                    new Delivery(target.id, snapshot(target, 0, room, now, Code.INVITED)));
        }
        var room = membership.get(peer.id);
        if (room == null || !command.reference().equals(NONE) && !command.reference().equals(room.id))
            return List.of(new Delivery(peer.id, empty(command, Code.CLOSED)));
        synchronized (room) {
            if (room.closed) return List.of(new Delivery(peer.id, empty(command, Code.CLOSED)));
            if (command.action() == Action.LEAVE) return close(room, peer, command.id(), Code.CLOSED);
            if (!visible) return close(room, peer, command.id(), Code.UNAVAILABLE);
            if (now - (room.started ? room.activity : room.created) >= (room.started ? GAME_IDLE : INVITATION_TIME))
                return close(room, peer, command.id(), Code.EXPIRED);
            if (command.action() == Action.ANSWER) {
                if (!peer.id.equals(room.second) || room.started) return List.of(new Delivery(peer.id, empty(command, Code.INVALID)));
                if (command.first() == 0) return close(room, peer, command.id(), Code.DECLINED);
                room.started = true; room.activity = now;
                return publish(room, peer, command.id(), now);
            }
            if (command.action() == Action.POLL) return List.of(new Delivery(peer.id, snapshot(peer, command.id(), room, now, room.started ? Code.MATCH : peer.id.equals(room.first) ? Code.WAITING : Code.INVITED)));
            if (command.action() != Action.MOVE || !room.started) return List.of(new Delivery(peer.id, empty(command, Code.INVALID)));
            if (command.revision() != room.round.revision) return List.of(new Delivery(peer.id, snapshot(peer, command.id(), room, now, Code.MATCH)));
            if (room.round.winner != 0 || room.round.turn != (peer.id.equals(room.first) ? 1 : 2))
                return List.of(new Delivery(peer.id, empty(command, Code.INVALID)));
            try { room.round = room.round.play(command.first(), command.second()); }
            catch (IllegalArgumentException invalid) { return List.of(new Delivery(peer.id, empty(command, Code.INVALID))); }
            room.activity = now; return publish(room, peer, command.id(), now);
        }
    }

    private List<Delivery> publish(Room room, Peer actor, long reply, long now) {
        var output = new ArrayList<Delivery>(2);
        for (UUID id : List.of(room.first, room.second)) {
            var peer = peers.get(id); if (peer != null) output.add(new Delivery(id, snapshot(peer, peer == actor ? reply : 0, room, now, Code.MATCH)));
        }
        return output;
    }

    private Message snapshot(Peer peer, long reply, Room room, long now, Code code) {
        int seconds = code == Code.WAITING || code == Code.INVITED ? (int)Math.max(0, Math.min(30, (INVITATION_TIME - (now - room.created) + 999_999_999L) / 1_000_000_000L)) : 0;
        return new Message(peer.session, reply, code, room.id, room.first, room.second, room.kind, room.round.seed, room.requestId, code == Code.MATCH ? room.round.history() : new int[0], seconds);
    }

    private List<Delivery> close(Room room, Peer actor, long reply, Code reason) {
        room.closed = true; rooms.remove(room.id, room); membership.remove(room.first, room); membership.remove(room.second, room);
        var output = new ArrayList<Delivery>(2);
        for (UUID id : List.of(room.first, room.second)) {
            var peer = peers.get(id);
            if (peer != null) output.add(new Delivery(id, new Message(peer.session, peer == actor ? reply : 0, reason, room.id, room.first, room.second, room.kind, room.round.seed, room.requestId, new int[0], 0)));
        }
        return output;
    }

    public List<Delivery> cancel(UUID actor, Code reason) {
        var room = membership.get(actor); if (room == null) return List.of();
        synchronized (room) { return room.closed ? List.of() : close(room, null, 0, reason); }
    }

    public List<Delivery> remove(UUID actor) {
        Peer peer; synchronized (directory) { peer = peers.remove(actor); }
        return peer == null ? List.of() : cancel(actor, Code.CLOSED);
    }

    public List<Delivery> expire(long now) {
        var output = new ArrayList<Delivery>();
        for (var room : rooms.values()) synchronized (room) {
            if (!room.closed && now - (room.started ? room.activity : room.created) >= (room.started ? GAME_IDLE : INVITATION_TIME)) output.addAll(close(room, null, 0, Code.EXPIRED));
        }
        for (var peer : peers.values()) if (now - peer.seen >= PEER_IDLE) {
            boolean removed; synchronized (directory) { removed = now - peer.seen >= PEER_IDLE && peers.remove(peer.id, peer); }
            if (removed) output.addAll(cancel(peer.id, Code.EXPIRED));
        }
        return output;
    }

    public void clear() { rooms.clear(); membership.clear(); peers.clear(); }
    public int rooms() { return rooms.size(); }
    private static Message empty(Command command, Code code) {
        return new Message(command.session(), command.id(), code, NONE, NONE, NONE, command.kind(), 0, 0, new int[0], 0);
    }
}
