package ru.he3hauka.toolvisuals.client.diagnostics.error;

import org.slf4j.LoggerFactory;

public final class ClientErrors {
    private static final org.slf4j.Logger LOGGER=LoggerFactory.getLogger("ToolVisuals/Error");
    private static final ErrorRepeats REPEATS=new ErrorRepeats(128,60_000_000_000L);
    private static Thread.UncaughtExceptionHandler installed,previous;
    private ClientErrors(){}

    public static synchronized void initialize(){
        if(installed!=null)return;
        previous=Thread.getDefaultUncaughtExceptionHandler();installed=handler(previous);Thread.setDefaultUncaughtExceptionHandler(installed);
        LOGGER.info("Client error reporting enabled: logs/latest.log; operation, thread, cause and full stack traces");
    }

    public static void report(String operation,Throwable error){
        if(error==null)return;
        for(var notice:REPEATS.record(operation,error,System.nanoTime()))write(notice);
    }

    private static void write(ErrorRepeats.Notice notice){
        if(notice.repeats()==0)LOGGER.error("Operation failed: {}",notice.operation(),notice.error());
        else LOGGER.error("Operation failed again: {} ({} additional occurrences)",notice.operation(),notice.repeats(),notice.error());
    }

    public static Runnable task(String operation,Runnable action){
        return ()->{try{action.run();}catch(RuntimeException|Error error){report(operation,error);throw error;}};
    }

    static Thread.UncaughtExceptionHandler handler(Thread.UncaughtExceptionHandler delegate){
        return (thread,error)->{
            try{report("Uncaught exception on thread "+thread.getName(),error);}
            finally{if(delegate!=null)delegate.uncaughtException(thread,error);}
        };
    }

    public static synchronized void close(){
        for(var notice:REPEATS.drain())write(notice);
        if(installed!=null&&Thread.getDefaultUncaughtExceptionHandler()==installed)Thread.setDefaultUncaughtExceptionHandler(previous);
        installed=previous=null;
    }
}
