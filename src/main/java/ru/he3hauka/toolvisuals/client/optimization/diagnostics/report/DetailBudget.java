package ru.he3hauka.toolvisuals.client.optimization.diagnostics.report;

import java.util.LinkedHashMap;
import ru.he3hauka.toolvisuals.client.optimization.diagnostics.capture.FreezeEvent;

public final class DetailBudget {
    private final LinkedHashMap<Long,Boolean> decisions=new LinkedHashMap<>();
    private long started,lastSevere,suppressed,maximum;
    private int detailed;
    public boolean allow(FreezeEvent event,long now) {
        Boolean decision=decisions.get(event.id());if(decision!=null)return decision;
        if(now-started>=30_000_000_000L){started=now;detailed=0;}
        boolean severe=event.duration()>=1_000_000_000L&&now-lastSevere>=3_000_000_000L;
        boolean accepted=detailed<6||severe;
        if(accepted){detailed++;if(severe)lastSevere=now;}else {suppressed++;maximum=Math.max(maximum,event.duration());}
        decisions.put(event.id(),accepted);if(decisions.size()>64)decisions.remove(decisions.keySet().iterator().next());
        return accepted;
    }
    public String suppressed() {
        if(suppressed==0)return "";
        String text="Подробности повторяющихся фризов объединены: "+suppressed+" событий; максимум="+String.format(java.util.Locale.ROOT,"%.2f",maximum/1e6)+" ms\n";
        suppressed=maximum=0;return text;
    }
}
