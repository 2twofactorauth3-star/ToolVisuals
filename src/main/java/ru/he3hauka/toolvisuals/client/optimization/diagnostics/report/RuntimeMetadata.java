package ru.he3hauka.toolvisuals.client.optimization.diagnostics.report;

import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.Comparator;
import java.util.HexFormat;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModOrigin;
import ru.he3hauka.toolvisuals.client.diagnostics.error.ClientErrors;
import ru.he3hauka.toolvisuals.client.optimization.diagnostics.profile.RuntimeProbe;

public final class RuntimeMetadata {
    private RuntimeMetadata() {}
    public static String header() {
        var runtime=Runtime.getRuntime();
        var text=new StringBuilder("java=").append(System.getProperty("java.version")).append(" vendor=").append(System.getProperty("java.vendor"))
                .append(" os=").append(System.getProperty("os.name")).append(' ').append(System.getProperty("os.version")).append(' ').append(System.getProperty("os.arch"))
                .append(" processors=").append(runtime.availableProcessors()).append(" heap_max_mib=").append(runtime.maxMemory()/1048576).append('\n');
        text.append("collectors=").append(String.join(",",RuntimeProbe.collectors())).append('\n');
        var flags=ManagementFactory.getRuntimeMXBean().getInputArguments().stream().filter(RuntimeMetadata::safeFlag).toList();
        text.append("jvm_memory_flags=").append(String.join(" ",flags)).append('\n');
        if(ManagementFactory.getOperatingSystemMXBean() instanceof com.sun.management.OperatingSystemMXBean os)
            text.append("physical_total_mib=").append(os.getTotalMemorySize()/1048576).append(" physical_free_mib=").append(os.getFreeMemorySize()/1048576).append('\n');
        var loader=FabricLoader.getInstance();
        text.append("toolvisuals_sha256=").append(fingerprint()).append('\n');
        loader.getAllMods().stream().sorted(Comparator.comparing(mod->mod.getMetadata().getId())).limit(256)
                .forEach(mod->text.append("mod=").append(mod.getMetadata().getId()).append(" version=").append(mod.getMetadata().getVersion().getFriendlyString()).append('\n'));
        return text.toString();
    }
    public static boolean safeFlag(String flag) {
        return flag.matches("-Xm[sx][0-9]+[kKmMgG]?")||flag.matches("-XX:[+-](Use[A-Za-z0-9]+GC|ZGenerational|AlwaysPreTouch|DisableExplicitGC)")
                ||flag.matches("-XX:(MaxRAMPercentage|InitialRAMPercentage|SoftMaxHeapSize|ZAllocationSpikeTolerance|ZCollectionInterval|ConcGCThreads|ParallelGCThreads|MaxGCPauseMillis)=[0-9.kKmMgG]+");
    }
    private static String fingerprint() {
        try {
            var container=FabricLoader.getInstance().getModContainer("toolvisuals");
            if(container.isEmpty()||container.get().getOrigin().getKind()!=ModOrigin.Kind.PATH)return "unavailable";
            for(var path:container.get().getOrigin().getPaths())if(Files.isRegularFile(path)&&Files.size(path)<=128*1024*1024) {
                var digest=MessageDigest.getInstance("SHA-256");
                try(var stream=Files.newInputStream(path)){byte[] buffer=new byte[65536];for(int size;(size=stream.read(buffer))>=0;)if(size>0)digest.update(buffer,0,size);}
                return HexFormat.of().formatHex(digest.digest());
            }
            return "development";
        }catch(Exception error){ClientErrors.report("Fingerprint performance diagnostic build",error);return "unavailable";}
    }
}
