package ru.he3hauka.toolvisuals.animation.social;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import ru.he3hauka.toolvisuals.animation.PairEmote;
import ru.he3hauka.toolvisuals.animation.network.RelayLimits;

import static ru.he3hauka.toolvisuals.animation.social.SocialProtocol.*;

public final class SocialCoordinator {
    public static final long INVITATION_NANOS = 10_000_000_000L;
    private static final long COOLDOWN_NANOS = 5_000_000_000L;
    public record Position(UUID world, double x, double y, double z, boolean available, long observed) {
        public Position {
            if (world == null || !Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)) throw new IllegalArgumentException("Invalid position");
        }
        public double distanceSquared(Position other) {
            if (other == null || !world.equals(other.world)) return Double.POSITIVE_INFINITY;
            double dx = x - other.x, dy = y - other.y, dz = z - other.z;
            return dx * dx + dy * dy + dz * dz;
        }
    }
    public record Delivery(UUID recipient, UUID source, State state) {}
    public record Pair(UUID id, UUID first, UUID second, PairEmote emote, long started, Position originFirst, Position originSecond) {
        public Started state(long now) { return new Started(id, first, second, emote, (int) Math.clamp((now - started) / 50_000_000L, 0, emote.animation.duration - 1)); }
    }
    private record Pending(UUID id, UUID sender, UUID recipient, PairEmote emote, long created) {}
    private static final class Participant {
        Position position;
        boolean badge;
        long lastInvite = Long.MIN_VALUE, lastReceived = Long.MIN_VALUE;
        Participant(Position position, boolean badge) { this.position = position; this.badge = badge; }
    }
    private final Map<UUID, Participant> players = new HashMap<>();
    private final Map<UUID, Pending> pending = new HashMap<>();
    private final Map<UUID, UUID> invitations = new HashMap<>();
    private final Map<UUID, Pair> pairs = new HashMap<>();

    public synchronized List<Delivery> register(UUID id, Position position, boolean badge) {
        var participant = players.get(id);
        boolean added = participant == null;
        if (added) {
            if (players.size() >= RelayLimits.MAX_PLAYERS) return List.of();
            participant = new Participant(position, badge); players.put(id, participant);
        }
        boolean changed = added || participant.badge != badge;
        participant.position = position; participant.badge = badge;
        var result = new ArrayList<Delivery>();
        if (changed) result.add(new Delivery(null, id, new Badge(id, badge)));
        if (added) for (var entry : players.entrySet()) {
            if (entry.getValue().badge && position.distanceSquared(entry.getValue().position) <= 128 * 128)
                result.add(new Delivery(id, entry.getKey(), new Badge(entry.getKey(), true)));
        }
        return result;
    }
    public synchronized boolean registered(UUID id) { return players.containsKey(id); }
    public synchronized Badge badge(UUID id) { var player = players.get(id); return player == null ? null : new Badge(id, player.badge); }
    public synchronized Pair pair(UUID id) { return pairs.get(id); }
    public synchronized List<Delivery> update(UUID id, Position position) {
        var player = players.get(id);
        if (player == null) return List.of();
        player.position = position;
        var pair = pairs.get(id);
        if (pair == null) return List.of();
        var origin = pair.first.equals(id) ? pair.originFirst : pair.originSecond;
        return !position.available || position.distanceSquared(origin) > .012 ? cancel(id, Outcome.CANCELLED) : List.of();
    }
    public synchronized List<Delivery> invite(UUID sender, UUID recipient, PairEmote emote, long now) {
        return invite(sender,recipient,emote,true,now);
    }
    public synchronized List<Delivery> invite(UUID sender,UUID recipient,PairEmote emote,boolean visible,long now) {
        var first = players.get(sender); var second = players.get(recipient);
        if (!visible || first == null || second == null || sender.equals(recipient) || !near(first, second, now))
            return List.of(new Delivery(sender, sender, new Result(new UUID(0, 0), Outcome.UNAVAILABLE)));
        if (pairs.containsKey(sender) || pairs.containsKey(recipient) || invitations.containsKey(sender) || invitations.containsKey(recipient)
                || recent(first.lastInvite, now) || recent(second.lastReceived, now))
            return List.of(new Delivery(sender, sender, new Result(new UUID(0, 0), Outcome.BUSY)));
        first.lastInvite = now; second.lastReceived = now;
        var invitation = new Pending(UUID.randomUUID(), sender, recipient, emote, now);
        pending.put(invitation.id, invitation); invitations.put(sender, invitation.id); invitations.put(recipient, invitation.id);
        return List.of(new Delivery(recipient, sender, new Invitation(invitation.id, sender, emote)), new Delivery(sender, sender, new Result(invitation.id, Outcome.SENT)));
    }
    public synchronized List<Delivery> answer(UUID recipient, UUID nonce, boolean accept, long now) {
        return answer(recipient, nonce, accept, true, now);
    }
    public synchronized List<Delivery> answer(UUID recipient, UUID nonce, boolean accept, boolean allowed, long now) {
        return answer(recipient,nonce,accept,allowed,true,now);
    }
    public synchronized UUID invitationSender(UUID recipient,UUID nonce) {
        var invitation=pending.get(nonce);
        return invitation!=null&&invitation.recipient.equals(recipient)?invitation.sender:null;
    }
    public synchronized boolean involves(UUID actor,UUID other) {
        var pair=pairs.get(actor);
        if(pair!=null)return pair.first.equals(other)||pair.second.equals(other);
        var nonce=invitations.get(actor);var invitation=nonce==null?null:pending.get(nonce);
        return invitation!=null&&(invitation.sender.equals(other)||invitation.recipient.equals(other));
    }
    public synchronized List<Delivery> answer(UUID recipient,UUID nonce,boolean accept,boolean allowed,boolean visible,long now) {
        var invitation = pending.get(nonce);
        if (invitation == null || !invitation.recipient.equals(recipient)) return List.of();
        remove(invitation);
        if (now - invitation.created >= INVITATION_NANOS) return result(invitation, Outcome.EXPIRED);
        if (!accept) return result(invitation, Outcome.DECLINED);
        if (!allowed || !visible) return result(invitation, Outcome.UNAVAILABLE);
        var first = players.get(invitation.sender); var second = players.get(recipient);
        if (first == null || second == null || !near(first, second, now) || pairs.containsKey(invitation.sender) || pairs.containsKey(recipient))
            return result(invitation, Outcome.UNAVAILABLE);
        var pair = new Pair(nonce, invitation.sender, recipient, invitation.emote, now, first.position, second.position);
        pairs.put(pair.first, pair); pairs.put(pair.second, pair);
        return broadcast(pair, pair.state(now));
    }
    public synchronized List<Delivery> cancel(UUID id, Outcome outcome) {
        var result = new ArrayList<Delivery>();
        var nonce = invitations.get(id);
        if (nonce != null) { var invitation = pending.get(nonce); remove(invitation); result.addAll(result(invitation, outcome)); }
        var pair = pairs.remove(id);
        if (pair != null) { pairs.remove(pair.first); pairs.remove(pair.second); result.addAll(broadcast(pair, new Stopped(pair.id))); }
        return result;
    }
    public synchronized List<Delivery> remove(UUID id) {
        var result = new ArrayList<>(cancel(id, Outcome.CANCELLED));
        if (players.remove(id) != null) result.add(new Delivery(null, id, new Badge(id, false)));
        return result;
    }
    public synchronized List<Delivery> tick(long now) {
        var result = new ArrayList<Delivery>();
        for (var invitation : List.copyOf(pending.values())) if (now - invitation.created >= INVITATION_NANOS) {
            remove(invitation); result.addAll(result(invitation, Outcome.EXPIRED));
        }
        for (var entry : List.copyOf(pairs.entrySet())) {
            var pair = entry.getValue();
            if (!entry.getKey().equals(pair.first)) continue;
            if (now - pair.started >= pair.emote.animation.duration * 50_000_000L || !near(players.get(pair.first), players.get(pair.second), now))
                result.addAll(cancel(pair.first, Outcome.CANCELLED));
        }
        return result;
    }
    private void remove(Pending invitation) { pending.remove(invitation.id); invitations.remove(invitation.sender); invitations.remove(invitation.recipient); }
    private static boolean recent(long previous, long now) { return previous != Long.MIN_VALUE && now - previous < COOLDOWN_NANOS; }
    private static boolean near(Participant first, Participant second, long now) {
        return first != null && second != null && first.position.available && second.position.available
                && now >= first.position.observed && now - first.position.observed <= 2_000_000_000L
                && now >= second.position.observed && now - second.position.observed <= 2_000_000_000L
                && first.position.distanceSquared(second.position) <= 16;
    }
    private static List<Delivery> result(Pending invitation, Outcome outcome) {
        var state = new Result(invitation.id, outcome);
        return List.of(new Delivery(invitation.sender, invitation.sender, state), new Delivery(invitation.recipient, invitation.sender, state));
    }
    private static List<Delivery> broadcast(Pair pair, State state) { return List.of(new Delivery(null, pair.first, state), new Delivery(null, pair.second, state)); }
}
