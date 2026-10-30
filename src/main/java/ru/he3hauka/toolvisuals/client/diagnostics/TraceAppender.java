package ru.he3hauka.toolvisuals.client.diagnostics;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;

final class TraceAppender extends AbstractAppender {
    private final TraceSession session;
    private Logger root;
    private long window=System.nanoTime();
    private int received;

    TraceAppender(TraceSession session){super("ToolVisualsModuleTrace",null,null,true,Property.EMPTY_ARRAY);this.session=session;}
    void attach(){root=LoggerContext.getContext(false).getRootLogger();start();root.addAppender(this);}
    void detach(){if(root!=null)root.removeAppender(this);stop();}

    @Override public synchronized void append(LogEvent event){
        if(!session.active()||event.getLevel()==null||!event.getLevel().isMoreSpecificThan(Level.WARN))return;
        long now=System.nanoTime();if(now-window>=1_000_000_000L){window=now;received=0;}
        if(received++>=40){session.omitted();return;}
        try{
            String message=event.getMessage()==null?"":TraceText.clip(event.getMessage().getFormattedMessage(),1024);
            var error=event.getThrown();
            if(error!=null)message+=" | "+TraceText.failure(error);
            session.event(event.getLoggerName(),event.getLevel().name(),message);
        }catch(RuntimeException error){session.event("LogCapture","format_error",error.getClass().getName());}
    }
}
