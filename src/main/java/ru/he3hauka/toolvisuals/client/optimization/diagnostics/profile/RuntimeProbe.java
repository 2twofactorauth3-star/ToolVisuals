package ru.he3hauka.toolvisuals.client.optimization.diagnostics.profile;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.util.List;

public final class RuntimeProbe {
    public record Point(long cpu,long allocated,long heap,long committed,long maximum) {}
    public record Gc(long count,long milliseconds) {}
    private static final java.lang.management.ThreadMXBean THREADS=ManagementFactory.getThreadMXBean();
    private static final com.sun.management.ThreadMXBean ALLOCATION=THREADS instanceof com.sun.management.ThreadMXBean bean?bean:null;
    private RuntimeProbe() {}
    public static Point current() {
        var runtime=Runtime.getRuntime();
        long cpu=THREADS.isCurrentThreadCpuTimeSupported()&&THREADS.isThreadCpuTimeEnabled()?THREADS.getCurrentThreadCpuTime():-1;
        long allocated=ALLOCATION!=null&&ALLOCATION.isThreadAllocatedMemorySupported()&&ALLOCATION.isThreadAllocatedMemoryEnabled()?ALLOCATION.getCurrentThreadAllocatedBytes():-1;
        return new Point(cpu,allocated,runtime.totalMemory()-runtime.freeMemory(),runtime.totalMemory(),runtime.maxMemory());
    }
    public static ThreadInfo thread(long id) { return THREADS.getThreadInfo(id,40); }
    public static Point threadPoint(long id) {
        var runtime=Runtime.getRuntime();
        long cpu=THREADS.isThreadCpuTimeSupported()&&THREADS.isThreadCpuTimeEnabled()?THREADS.getThreadCpuTime(id):-1;
        long allocated=ALLOCATION!=null&&ALLOCATION.isThreadAllocatedMemorySupported()&&ALLOCATION.isThreadAllocatedMemoryEnabled()?ALLOCATION.getThreadAllocatedBytes(id):-1;
        return new Point(cpu,allocated,runtime.totalMemory()-runtime.freeMemory(),runtime.totalMemory(),runtime.maxMemory());
    }
    public static Gc gc() {
        long count=0,time=0;
        for(var bean:ManagementFactory.getGarbageCollectorMXBeans()){count+=Math.max(0,bean.getCollectionCount());time+=Math.max(0,bean.getCollectionTime());}
        return new Gc(count,time);
    }
    public static List<String> collectors() { return ManagementFactory.getGarbageCollectorMXBeans().stream().map(bean->bean.getName()).toList(); }
    public static long delta(long before,long after) { return before<0||after<before?-1:after-before; }
}
