package ru.he3hauka.toolvisuals.client.optimization.diagnostics.report;

import java.time.Instant;
import java.util.Comparator;
import java.util.Locale;
import ru.he3hauka.toolvisuals.client.optimization.diagnostics.capture.FreezeEvent;
import ru.he3hauka.toolvisuals.client.optimization.diagnostics.capture.FreezeMonitor;
import ru.he3hauka.toolvisuals.client.optimization.diagnostics.profile.ProfileEvidence;
import ru.he3hauka.toolvisuals.client.optimization.diagnostics.profile.RuntimeProbe;

public final class FreezeReport {
    private FreezeReport() {}
    private static String ms(long nanos) { return String.format(Locale.ROOT,"%.2f",nanos/1e6); }
    private static String mib(long bytes) { return bytes<0?"недоступно":String.format(Locale.ROOT,"%.2f MiB",bytes/1048576d); }
    public static String event(FreezeEvent event,ProfileEvidence profile,String profiler,Instant wall) {
        var text=new StringBuilder(4096);
        text.append("\n=== ФРИЗ #").append(event.id()).append(event.finished()?" ЗАВЕРШЁН":" ПРОДОЛЖАЕТСЯ").append(" | ").append(wall).append(" ===\n");
        text.append("Полный кадр: ").append(ms(event.duration())).append(" ms; обычный кадр ~").append(ms(event.baseline())).append(" ms; порог ").append(ms(event.threshold())).append(" ms\n");
        text.append("Контекст: screen=").append(event.context().screen()).append(" throttle=").append(event.context().throttle()).append(' ').append(event.context().details()).append('\n');
        text.append("Модули: ").append(event.context().modules()).append('\n');
        text.append("Поток #").append(event.threadId()).append(": CPU=").append(cpuTime(RuntimeProbe.delta(event.before().cpu(),event.after().cpu())))
                .append("; выделено на потоке ~").append(mib(RuntimeProbe.delta(event.before().allocated(),event.after().allocated()))).append('\n');
        text.append("Java heap: ").append(mib(event.before().heap())).append(" -> ").append(mib(event.after().heap()))
                .append("; committed=").append(mib(event.after().committed())).append("; max=").append(mib(event.after().maximum())).append('\n');
        text.append("Вне замеренных этапов: ").append(ms(Math.max(0,event.duration()-event.covered()))).append(" ms\n");
        text.append("Этапы (вложены друг в друга; время нельзя складывать):\n");
        event.phases().stream().sorted(Comparator.comparingLong(FreezeEvent.Phase::total).reversed()).limit(10).forEach(phase->text.append("  ").append(phase.section())
                .append(" total=").append(ms(phase.total())).append(" ms max=").append(ms(phase.maximum())).append(" ms calls=").append(phase.calls()).append('\n'));
        if(event.stacks().isEmpty())text.append("Стек во время паузы не получен: короткий фриз, остановка всей JVM или занятый наблюдатель.\n");
        for(var stack:event.stacks()) {
            text.append("Стек +").append(ms(stack.offset())).append(" ms: ").append(stack.state()).append("; этап=").append(stack.phase());
            if(stack.owner()>0)text.append("; владелец блокировки thread#").append(stack.owner());
            text.append('\n');for(String frame:stack.frames())text.append("    at ").append(frame).append('\n');
        }
        text.append("JFR: ").append(profiler).append("; полученные GC-паузы в кадре=").append(ms(profile.paused(event.started(),event.ended()))).append(" ms (0 не исключает недоставленные сэмплы)\n");
        long from=event.started()-2_000_000_000L;
        text.append("Выделения памяти: сэмплы за 2 s до кадра и сам кадр; вес является оценкой, а не размером утечки.\n");
        hotspots(text,profile,ProfileEvidence.Kind.ALLOCATION,from,event.ended(),0);
        text.append("CPU-сэмплы потока кадра:\n");hotspots(text,profile,ProfileEvidence.Kind.CPU,event.started(),event.ended(),event.threadId());
        text.append("Долгие ожидания и I/O потока кадра:\n");hotspots(text,profile,ProfileEvidence.Kind.BLOCK,event.started(),event.ended(),event.threadId());
        text.append("Стек показывает место работы/ожидания; наличие класса мода в стеке само по себе не доказывает вину.\n");
        return text.toString();
    }
    private static String cpuTime(long value) { return value<0?"недоступно":ms(value)+" ms"; }
    private static void hotspots(StringBuilder text,ProfileEvidence profile,ProfileEvidence.Kind kind,long from,long to,long owner) {
        var hot=profile.top(kind,from,to,owner,4);
        if(hot.isEmpty())text.append("  нет доступных сэмплов\n");
        for(var item:hot){text.append("  ").append(item.subject()).append(" samples=").append(item.samples()).append(" weight=").append(kind==ProfileEvidence.Kind.ALLOCATION?mib(item.weight()):kind==ProfileEvidence.Kind.CPU?item.weight()+" samples":ms(item.weight())+" ms").append('\n');for(String frame:item.stack())text.append("    at ").append(frame).append('\n');}
    }
    public static String summary(FreezeMonitor.Summary sample,Instant time,RuntimeProbe.Point point,RuntimeProbe.Gc previous,RuntimeProbe.Gc current) {
        double seconds=Math.max(.001,(sample.ended()-sample.started())/1e9);
        String allocated=sample.allocated()<0?"недоступно":String.format(Locale.ROOT,"%.1f MiB/s",sample.allocated()/1048576d/seconds);
        String cpu=sample.cpu()<0?"недоступно":String.format(Locale.ROOT,"%.1f%%",100d*sample.cpu()/Math.max(1,sample.total()));
        return String.format(Locale.ROOT,"%s АКТИВНАЯ ИГРА %.1f s: frames=%d fps=%.1f avg=%.2f ms max=%.2f ms freezes=%d main_allocated=%s main_cpu=%s heap=%.1f/%d MiB GC_cycles_delta=%d GC_accounting_delta_ms=%d (не длительность фриза)\n",
                time,seconds,sample.frames(),sample.frames()/seconds,sample.total()/1e6/sample.frames(),sample.maximum()/1e6,sample.freezes(),allocated,cpu,
                point.heap()/1048576d,point.maximum()/1048576,Math.max(0,current.count()-previous.count()),Math.max(0,current.milliseconds()-previous.milliseconds()));
    }
}
