package ru.he3hauka.toolvisuals.client.cosmetic.accessory;

import java.util.Base64;

import ru.he3hauka.toolvisuals.cosmetic.accessory.*;

public final class AccessoryConfigCodec {
    private AccessoryConfigCodec(){}
    public static String encode(AccessoryLook look){return Base64.getEncoder().encodeToString(AccessoryWire.encodeLook(look));}
    public static AccessoryLook decode(String text){
        if(text==null)return AccessoryLook.EMPTY;
        if(text.length()>4*((AccessoryWire.MAX_LOOK+2)/3))throw new IllegalArgumentException("Cosmetic preset exceeds fixed limits");
        return AccessoryWire.decodeLook(Base64.getDecoder().decode(text));
    }
}
