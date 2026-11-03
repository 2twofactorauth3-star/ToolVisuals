package ru.he3hauka.toolvisuals.client.friends;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public final class FriendDisplay {
    public enum Field { HEALTH,ARMOR,INVENTORY }
    public enum InventoryMode { DEFAULT,GRID,HOVER }
    public static final FriendDisplay DEFAULT=new FriendDisplay(true,true,true);
    public final boolean health,armor,inventory;
    public final InventoryMode mode;
    public FriendDisplay(boolean health,boolean armor,boolean inventory){this(health,armor,inventory,InventoryMode.DEFAULT);}
    public FriendDisplay(boolean health,boolean armor,boolean inventory,InventoryMode mode){this.health=health;this.armor=armor;this.inventory=inventory;this.mode=java.util.Objects.requireNonNull(mode);}
    public FriendDisplay toggle(Field field){return new FriendDisplay(field==Field.HEALTH?!health:health,field==Field.ARMOR?!armor:armor,field==Field.INVENTORY?!inventory:inventory,mode);}
    public FriendDisplay nextMode(){return new FriendDisplay(health,armor,inventory,InventoryMode.values()[(mode.ordinal()+1)%InventoryMode.values().length]);}
    public int mode(int fallback){return mode==InventoryMode.DEFAULT?fallback:mode==InventoryMode.GRID?0:1;}
    public static FriendDisplay decode(JsonElement value){
        if(value==null||value.isJsonNull())return DEFAULT;
        var object=value.getAsJsonObject();var mode=object.get("mode");return new FriendDisplay(flag(object,"health"),flag(object,"armor"),flag(object,"inventory"),mode==null||mode.isJsonNull()?InventoryMode.DEFAULT:InventoryMode.valueOf(mode.getAsString()));
    }
    private static boolean flag(JsonObject object,String key){
        var value=object.get(key);if(value==null||value.isJsonNull())return true;
        if(!value.isJsonPrimitive()||!value.getAsJsonPrimitive().isBoolean())throw new IllegalArgumentException("Invalid friend display preference");
        return value.getAsBoolean();
    }
}
