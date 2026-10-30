package ru.he3hauka.toolvisuals.client.hud.info;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class InfoHudValues {
    private static final DateTimeFormatter MINUTES=DateTimeFormatter.ofPattern("HH:mm",Locale.ROOT),SECONDS=DateTimeFormatter.ofPattern("HH:mm:ss",Locale.ROOT);
    private InfoHudValues(){}
    public static String clock(Instant instant,ZoneId zone,boolean seconds){return (seconds?SECONDS:MINUTES).format(instant.atZone(zone));}
    public static String ping(int milliseconds){return milliseconds<0?"— ms":milliseconds+" ms";}
    public static String speed(double deltaX,double deltaZ){
        double blocksPerSecond=Math.hypot(deltaX,deltaZ)*20;
        return String.format(Locale.ROOT,"%.1f BPS",Double.isFinite(blocksPerSecond)?blocksPerSecond:0);
    }
}
