package ru.he3hauka.toolvisuals.client.cape.animation;

import java.util.Base64;
import java.util.ArrayList;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import ru.he3hauka.toolvisuals.cosmetic.cape.CapeDesign;

public final class CapeAnimationCodec {
    private CapeAnimationCodec(){}
    public static JsonObject encode(CapeDesign design){
        var value=new JsonObject();value.addProperty("fps",design.fps);var frames=new JsonArray();
        for(int i=0;i<design.frameCount;i++)frames.add(Base64.getEncoder().encodeToString(design.frame(i).bytes()));value.add("frames",frames);return value;
    }
    public static CapeDesign decode(JsonElement encoded){
        var value=encoded.getAsJsonObject();var rows=value.getAsJsonArray("frames");int fps=value.get("fps").getAsInt();
        if(rows.isEmpty()||rows.size()>CapeDesign.MAX_FRAMES||fps<1||fps>CapeDesign.MAX_FPS)throw new IllegalArgumentException("Cape animation exceeds its limits");
        var frames=new ArrayList<CapeDesign>();for(var row:rows){String pixels=row.getAsString();if(pixels.length()>4*((CapeDesign.BYTES+2)/3))throw new IllegalArgumentException("Cape frame exceeds its limits");frames.add(CapeDesign.decode(Base64.getDecoder().decode(pixels)));}
        return new CapeDesign(frames,fps);
    }
}
