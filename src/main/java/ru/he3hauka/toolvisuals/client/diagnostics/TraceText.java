package ru.he3hauka.toolvisuals.client.diagnostics;

import java.util.*;
import java.util.regex.Pattern;

final class TraceText {
    static final int LIMIT=4096;
    private static final Pattern JWT=Pattern.compile("[A-Za-z0-9_-]{8,}\\.[A-Za-z0-9_-]{8,}\\.[A-Za-z0-9_-]{8,}");
    private static final Pattern SECRET=Pattern.compile("(?i)(bearer\\s+|(?:access[_-]?token|refresh[_-]?token|password|secret|authorization|session[_-]?id)\\s*[:=]\\s*)[^\\s,;}\\\"]+");
    private final List<String> privateValues;

    TraceText(Collection<String> privateValues){
        this.privateValues=privateValues.stream().filter(Objects::nonNull).filter(value->value.length()>=3)
                .filter(value->!value.equalsIgnoreCase("ToolVisuals")).distinct().sorted(Comparator.comparingInt(String::length).reversed()).toList();
    }

    String safe(String value){
        String text=clip(value,LIMIT);
        for(String hidden:privateValues)text=text.replace(hidden,"<private>");
        text=JWT.matcher(text).replaceAll("<token>");
        text=SECRET.matcher(text).replaceAll("$1<redacted>");
        var result=new StringBuilder(text.length());
        for(int index=0;index<text.length();index++){
            char ch=text.charAt(index);
            if(ch=='\n')result.append("\\n");
            else if(ch=='\r')result.append("\\r");
            else if(ch=='\t')result.append(' ');
            else if(!Character.isISOControl(ch)&&Character.getType(ch)!=Character.FORMAT)result.append(ch);
        }
        return clip(result.toString(),LIMIT);
    }

    static String clip(String text,int limit){return text==null?"":text.length()<=limit?text:text.substring(0,limit-1)+"…";}

    static String failure(Throwable error){
        try{return describe(error);}catch(RuntimeException failure){return error==null?"":error.getClass().getName()+" (details unavailable)";}
    }

    private static String describe(Throwable error){
        var result=new StringBuilder();
        var seen=Collections.newSetFromMap(new IdentityHashMap<Throwable,Boolean>());
        for(int cause=0;error!=null&&cause<4&&seen.add(error);cause++,error=error.getCause()){
            if(cause>0)result.append(" caused_by=");
            result.append(error.getClass().getName()).append(": ").append(clip(error.getMessage(),512));
            var frames=error.getStackTrace();
            for(int index=0;index<Math.min(12,frames.length);index++)result.append(" | at ").append(frames[index]);
        }
        return clip(result.toString(),LIMIT);
    }
}
