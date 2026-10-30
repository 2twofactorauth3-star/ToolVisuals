package ru.he3hauka.toolvisuals.client.optimization.texture;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.LongSupplier;
import ru.he3hauka.toolvisuals.client.diagnostics.error.ClientErrors;

public final class UploadQueue<K> {
    public record Job(long bytes, boolean foreground, Runnable upload, Runnable cancelled) {
        public Job {
            if (bytes < 1 || bytes > 4 * 1024 * 1024) throw new IllegalArgumentException("Invalid texture size");
            Objects.requireNonNull(upload);
            Objects.requireNonNull(cancelled);
        }
    }

    private final Object2IntOpenHashMap<K> indices;
    private final Object[] keys;
    private final Job[] jobs;
    private final int[] previous, next, previousPriority, nextPriority, free;
    private final int[] priorityHead = {-1, -1}, priorityTail = {-1, -1};
    private int head = -1, tail = -1, available;
    private boolean foregroundFirst = true;

    public UploadQueue(int capacity) {
        if (capacity < 1) throw new IllegalArgumentException("Invalid queue capacity");
        indices = new Object2IntOpenHashMap<>(capacity);
        indices.defaultReturnValue(-1);
        keys = new Object[capacity];
        jobs = new Job[capacity];
        previous = new int[capacity];
        next = new int[capacity];
        previousPriority = new int[capacity];
        nextPriority = new int[capacity];
        free = new int[capacity];
        resetSlots();
    }

    public boolean offer(K key, Job job) {
        Objects.requireNonNull(job);
        Job removed = null;
        synchronized (this) {
            int slot = indices.getInt(key);
            if (slot >= 0) {
                removed = jobs[slot];
                if (removed.foreground != job.foreground) {
                    unlinkPriority(slot);
                    jobs[slot] = job;
                    linkPriority(slot, true);
                } else jobs[slot] = job;
            } else {
                if (available == 0) {
                    if (!job.foreground) return false;
                    int victim = priorityHead[0] >= 0 ? priorityHead[0] : head;
                    removed = remove(victim);
                }
                slot = free[--available];
                indices.put(key, slot);
                keys[slot] = key;
                jobs[slot] = job;
                previous[slot] = tail;
                next[slot] = -1;
                if (tail >= 0) next[tail] = slot;
                else head = slot;
                tail = slot;
                linkPriority(slot, false);
            }
        }
        if (removed != null) removed.cancelled.run();
        return true;
    }

    public void cancel(K key) {
        Job removed;
        synchronized (this) {
            int slot = indices.getInt(key);
            removed = slot < 0 ? null : remove(slot);
        }
        if (removed != null) removed.cancelled.run();
    }

    private void linkPriority(int slot, boolean replaced) {
        int priority = jobs[slot].foreground ? 1 : 0;
        int before = priorityTail[priority];
        if (replaced) {
            before = previous[slot];
            while (before >= 0 && jobs[before].foreground != jobs[slot].foreground) before = previous[before];
        }
        int after = before < 0 ? priorityHead[priority] : nextPriority[before];
        previousPriority[slot] = before;
        nextPriority[slot] = after;
        if (before < 0) priorityHead[priority] = slot;
        else nextPriority[before] = slot;
        if (after < 0) priorityTail[priority] = slot;
        else previousPriority[after] = slot;
    }

    private void unlinkPriority(int slot) {
        int priority = jobs[slot].foreground ? 1 : 0;
        int before = previousPriority[slot], after = nextPriority[slot];
        if (before < 0) priorityHead[priority] = after;
        else nextPriority[before] = after;
        if (after < 0) priorityTail[priority] = before;
        else previousPriority[after] = before;
    }

    private Job remove(int slot) {
        var job = jobs[slot];
        unlinkPriority(slot);
        int before = previous[slot], after = next[slot];
        if (before < 0) head = after;
        else next[before] = after;
        if (after < 0) tail = before;
        else previous[after] = before;
        indices.removeInt(keys[slot]);
        keys[slot] = null;
        jobs[slot] = null;
        free[available++] = slot;
        return job;
    }

    private synchronized Job take(boolean foreground, long remainingBytes, boolean first) {
        int slot = priorityHead[foreground ? 1 : 0];
        if (slot < 0) slot = head;
        if (slot < 0 || !first && jobs[slot].bytes > remainingBytes) return null;
        return remove(slot);
    }

    public int drain(int maximum, long bytes, long nanos, LongSupplier clock) {
        int completed = 0;
        long used = 0, started = clock.getAsLong();
        boolean preferred;
        synchronized (this) { preferred = foregroundFirst; }
        while (completed < maximum) {
            if (completed > 0 && clock.getAsLong() - started >= nanos) break;
            var job = take(completed % 2 == 0 ? preferred : !preferred, bytes - used, completed == 0);
            if (job == null) break;
            try { job.upload.run(); }
            catch (RuntimeException error) { ClientErrors.report("Upload queued texture", error); job.cancelled.run(); }
            used += job.bytes;
            completed++;
        }
        if (completed > 0) synchronized (this) { foregroundFirst = !foregroundFirst; }
        return completed;
    }

    public void clear() {
        Job[] pending;
        synchronized (this) {
            pending = new Job[indices.size()];
            int index = 0;
            for (int slot = head; slot >= 0; slot = next[slot]) pending[index++] = jobs[slot];
            indices.clear();
            Arrays.fill(keys, null);
            Arrays.fill(jobs, null);
            resetSlots();
        }
        for (var job : pending) job.cancelled.run();
    }

    private void resetSlots() {
        head = tail = -1;
        Arrays.fill(priorityHead, -1);
        Arrays.fill(priorityTail, -1);
        available = free.length;
        for (int index = 0; index < free.length; index++) free[index] = index;
    }

    public synchronized int size() { return indices.size(); }
}
