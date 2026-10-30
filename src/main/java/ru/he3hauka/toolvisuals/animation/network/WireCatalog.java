package ru.he3hauka.toolvisuals.animation.network;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.ToIntFunction;

public final class WireCatalog {
    private WireCatalog(){}
    public static <T> Map<Integer,T> index(T[] values,ToIntFunction<T> id){
        var result=new LinkedHashMap<Integer,T>();
        for(var value:values){int key=id.applyAsInt(value);if(key<0||result.putIfAbsent(key,value)!=null)throw new IllegalArgumentException("Duplicate or negative wire ID");}
        return Map.copyOf(result);
    }
}
