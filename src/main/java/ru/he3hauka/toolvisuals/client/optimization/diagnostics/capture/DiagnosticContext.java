package ru.he3hauka.toolvisuals.client.optimization.diagnostics.capture;

public record DiagnosticContext(boolean world,boolean focused,boolean iconified,boolean paused,boolean loading,
                                String screen,String throttle,long worldRevision,long frameBudgetNanos,String details,String modules) {
    public static final DiagnosticContext INACTIVE=new DiagnosticContext(false,false,false,false,false,"none","NONE",0,0,"","");
    public boolean eligible() { return world&&focused&&!iconified&&!paused&&!loading&&throttle.equals("NONE"); }
}
