package ru.he3hauka.toolvisuals.client.optimization.diagnostics.capture;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import ru.he3hauka.toolvisuals.client.optimization.diagnostics.Diagnostics;
import ru.he3hauka.toolvisuals.client.optimization.diagnostics.profile.RuntimeProbe;

public final class FreezeMonitor {
    public static final long MINIMUM=50_000_000L,GRACE=1_000_000_000L;
    public record Watch(long id,long threadId,long started,String phase) {}
    public record Summary(long started,long ended,long frames,long total,long maximum,long freezes,long allocated,long cpu) {}
    private final long[] totals=new long[Diagnostics.Section.values().length],maxima=new long[totals.length],starts=new long[32];
    private final int[] calls=new int[totals.length],path=new int[32];
    private final ArrayList<FreezeEvent.StackSample> stacks=new ArrayList<>(8);
    private final long[] activityStarts=new long[32],activityEnds=new long[32];
    private long activityStart;
    private int activityCursor,activityCount;
    private long sequence,current,threadId,started,baseline=16_666_667L,threshold,covered,rootStarted,nextWatch,graceUntil,revision=Long.MIN_VALUE;
    private long summaryStart,summaryEnd,frames,total,maximum,freezes,allocated,cpu;
    private int depth,watchCount;
    private boolean playing,ongoing;
    private DiagnosticContext context=DiagnosticContext.INACTIVE;
    private RuntimeProbe.Point before;

    public synchronized long begin(long now,long owner,DiagnosticContext next,RuntimeProbe.Point point) {
        if(!next.eligible()){suspend(now);return 0;}
        if(!playing||revision!=next.worldRevision()) {
            if(playing)finishActivity(now);
            current=0;
            activityStart=now;
            resetWindow();baseline=Math.max(1,next.frameBudgetNanos()>0?next.frameBudgetNanos():16_666_667L);graceUntil=now+GRACE;
        }
        playing=true;revision=next.worldRevision();context=next;threadId=owner;
        if(current==0)prepare(now,point);
        return current;
    }
    private void prepare(long now,RuntimeProbe.Point point) {
        current=++sequence;started=now;before=point;
        depth=0;covered=0;rootStarted=0;watchCount=0;ongoing=false;stacks.clear();Arrays.fill(totals,0);Arrays.fill(maxima,0);Arrays.fill(calls,0);
        threshold=Math.max(MINIMUM,Math.max(baseline,context.frameBudgetNanos())*3);nextWatch=now+Math.max(100_000_000L,threshold);
    }
    public synchronized void suspend(long now) { if(playing)finishActivity(now);playing=false;current=0;depth=0;stacks.clear();resetWindow(); }
    private void finishActivity(long now) {activityStarts[activityCursor]=activityStart;activityEnds[activityCursor]=now;activityCursor=(activityCursor+1)%activityStarts.length;activityCount=Math.min(activityCount+1,activityStarts.length);}
    public synchronized boolean wasActive(long now) {
        if(playing&&now>=activityStart)return true;
        for(int i=0;i<activityCount;i++)if(now>=activityStarts[i]&&now<=activityEnds[i])return true;
        return false;
    }
    public synchronized boolean playing() { return playing; }
    public synchronized long started() { return current==0?0:started; }
    public synchronized void enter(Diagnostics.Section section,long now) {
        if(current==0||depth==path.length)return;
        if(depth==0)rootStarted=now;
        path[depth]=section.ordinal();starts[depth++]=now;
    }
    public synchronized void exit(Diagnostics.Section section,long now,long began) {
        if(current==0||began<started)return;
        int index=depth-1;
        while(index>=0&&(path[index]!=section.ordinal()||starts[index]!=began))index--;
        if(index<0)return;
        for(int i=depth-1;i>=index;i--)record(path[i],now-starts[i]);
        depth=index;
        if(depth==0)covered+=Math.max(0,now-rootStarted);
    }
    private void record(int phase,long nanos) { nanos=Math.max(0,nanos);totals[phase]+=nanos;maxima[phase]=Math.max(maxima[phase],nanos);calls[phase]++; }
    public synchronized Watch watch(long now) {
        if(current==0||started<graceUntil||now<nextWatch||watchCount>=8)return null;
        watchCount++;nextWatch=started+Math.max(100_000_000L,threshold)*(1L<<Math.min(watchCount,7));
        return new Watch(current,threadId,started,phase());
    }
    public synchronized void sampled(Watch watch,FreezeEvent.StackSample sample) {
        if(current==watch.id&&started==watch.started&&stacks.size()<8)stacks.add(sample);
    }
    private String phase() {
        if(depth==0)return "между замеренными этапами";
        var parts=new StringBuilder();
        for(int i=0;i<depth;i++){if(i>0)parts.append(" > ");parts.append(Diagnostics.Section.values()[path[i]]);}
        return parts.toString();
    }
    public synchronized FreezeEvent ongoing(long now,RuntimeProbe.Point latest) {
        if(current==0||ongoing||started<graceUntil||now-started<Math.max(2_000_000_000L,threshold*2))return null;
        ongoing=true;return event(now,false,latest);
    }
    public synchronized FreezeEvent end(long id,long now,DiagnosticContext after,RuntimeProbe.Point point) {
        if(id==0||current!=id)return null;
        if(!after.eligible()||after.worldRevision()!=revision){suspend(now);return null;}
        long elapsed=Math.max(0,now-started);FreezeEvent result=null;
        if(started>=graceUntil) {
            if(frames==0)summaryStart=started;
            summaryEnd=now;frames++;total+=elapsed;maximum=Math.max(maximum,elapsed);
            long bytes=RuntimeProbe.delta(before.allocated(),point.allocated()),time=RuntimeProbe.delta(before.cpu(),point.cpu());
            if(bytes<0)allocated=-1;else if(allocated>=0)allocated+=bytes;
            if(time<0)cpu=-1;else if(cpu>=0)cpu+=time;
            if(elapsed>=threshold){freezes++;result=event(now,true,point);}
        }
        baseline=Math.max(1,(baseline*15+Math.min(elapsed,baseline*2))/16);
        prepare(now,point);return result;
    }
    private FreezeEvent event(long now,boolean finished,RuntimeProbe.Point after) {
        var phases=new ArrayList<FreezeEvent.Phase>();
        long accounted=covered;
        for(int i=0;i<totals.length;i++) {
            long duration=totals[i],max=maxima[i];int count=calls[i];
            for(int j=0;j<depth;j++)if(path[j]==i){long active=Math.max(0,now-starts[j]);duration+=active;max=Math.max(max,active);count++;}
            if(count>0)phases.add(new FreezeEvent.Phase(Diagnostics.Section.values()[i],duration,max,count));
        }
        if(depth>0)accounted+=Math.max(0,now-rootStarted);
        return new FreezeEvent(current,threadId,started,now,baseline,threshold,accounted,finished,context,before,after,phases,stacks);
    }
    public synchronized Summary summary(long now,boolean force) {
        if(frames==0||!force&&now-summaryStart<30_000_000_000L)return null;
        var result=new Summary(summaryStart,summaryEnd,frames,total,maximum,freezes,allocated,cpu);resetWindow();return result;
    }
    private void resetWindow() { summaryStart=summaryEnd=frames=total=maximum=freezes=allocated=cpu=0; }
}
