package ru.he3hauka.toolvisuals.client.optimization.diagnostics.capture;

import java.util.List;
import ru.he3hauka.toolvisuals.client.optimization.diagnostics.Diagnostics;
import ru.he3hauka.toolvisuals.client.optimization.diagnostics.profile.RuntimeProbe;

public record FreezeEvent(long id,long threadId,long started,long ended,long baseline,long threshold,long covered,
                          boolean finished,DiagnosticContext context,RuntimeProbe.Point before,RuntimeProbe.Point after,
                          List<Phase> phases,List<StackSample> stacks) {
    public record Phase(Diagnostics.Section section,long total,long maximum,int calls) {}
    public record StackSample(long offset,String phase,String state,long owner,List<String> frames) {
        public StackSample { frames=List.copyOf(frames); }
    }
    public FreezeEvent { phases=List.copyOf(phases);stacks=List.copyOf(stacks); }
    public long duration() { return Math.max(0,ended-started); }
}
