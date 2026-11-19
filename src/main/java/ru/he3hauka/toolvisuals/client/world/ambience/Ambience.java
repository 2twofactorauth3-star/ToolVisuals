package ru.he3hauka.toolvisuals.client.world.ambience;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import ru.he3hauka.toolvisuals.client.ToolVisualsClient;
import ru.he3hauka.toolvisuals.client.module.VisualModule;
import ru.he3hauka.toolvisuals.client.module.VisualSetting;
import ru.he3hauka.toolvisuals.client.render.filter.NoRender;
import ru.he3hauka.toolvisuals.client.render.filter.RenderOption;

public final class Ambience {
    private static final Season[] SEASONS=Season.values();
    private record Palette(Season season,float strength) {}
    private static volatile Palette palette;
    private static Palette requested;
    private static int stableTicks;
    private static ClientLevel level;
    private static int ticks;
    private Ambience() {}
    public static void tick(Minecraft client) {
        var config=ToolVisualsClient.CONFIG;
        boolean enabled=client.level!=null&&client.player!=null&&client.level.dimension().equals(Level.OVERWORLD)&&config.active(VisualModule.AMBIENCE);
        Season season=SEASONS[(int)config.value(VisualSetting.AMBIENCE_SEASON)];
        float strength=config.number(VisualSetting.AMBIENCE_STRENGTH);
        Palette current=requested;
        Palette next=!enabled?null:current!=null&&current.season==season&&current.strength==strength?current:new Palette(season,strength);
        if(next!=requested){requested=next;stableTicks=0;}else if(stableTicks<6)stableTicks++;
        boolean changed=palette==null?next!=null:!palette.equals(next);
        boolean sameWorld=level==client.level;
        level=client.level;
        if(changed&&(next==null||!sameWorld||stableTicks>=6)){
            palette=next;
            if(sameWorld&&level!=null){level.clearTintCaches();client.levelExtractor.allChanged();}
        }
        if(!enabled||client.isPaused()||!config.flag(VisualSetting.AMBIENCE_PARTICLES)||NoRender.hides(RenderOption.ALL_PARTICLES)||++ticks%2!=0)return;
        var random=level.getRandom();var player=client.player;
        int radius=Math.min(12,Math.max(4,client.options.getEffectiveRenderDistance()*2));
        float density=config.number(VisualSetting.AMBIENCE_DENSITY)*Season.intensity(next.strength);
        int limit=next.season==Season.WINTER?2:1,spawned=0;
        if(next.season==Season.SUMMER&&ticks%6!=0)return;
        for(int attempt=0;attempt<4&&spawned<limit;attempt++) {
            double x=player.getX()+(random.nextDouble()*2-1)*radius,z=player.getZ()+(random.nextDouble()*2-1)*radius,y=player.getY()+2+random.nextDouble()*5;
            BlockPos pos=BlockPos.containing(x,y,z);
            if(!level.hasChunkAt(pos)||!level.canSeeSky(pos)||!level.getBlockState(pos).isAir())continue;
            if(random.nextFloat()>=density)continue;
            var particle=switch(next.season){case WINTER->ParticleTypes.SNOWFLAKE;case SPRING->ParticleTypes.CHERRY_LEAVES;case AUTUMN->ParticleTypes.PALE_OAK_LEAVES;case SUMMER->ParticleTypes.SPORE_BLOSSOM_AIR;};
            level.addParticle(particle,x,y,z,.015,-.015,.008);
            spawned++;
        }
    }
    public static int vegetation(int color,boolean leaves) {
        Palette current=palette;
        return current==null?color:Season.blend(color,leaves?current.season.leaves:current.season.grass,Season.intensity(current.strength)*(leaves?1:.88f));
    }
    public static int sky(int color) {
        Palette current=palette;
        if(current==null)return color;
        float peak=Math.max(color>>16&255,Math.max(color>>8&255,color&255))/255f;
        return Season.blend(color,current.season.sky,Season.atmosphere(current.strength,peak,.58f));
    }
    public static Vector3fc sky(Vector3fc color) {
        Palette current=palette;
        if(current==null)return color;
        float amount=Season.atmosphere(current.strength,Math.max(color.x(),Math.max(color.y(),color.z())),.58f);
        int tint=current.season.sky;
        return new Vector3f(color.x()+((tint>>16&255)/255f-color.x())*amount,color.y()+((tint>>8&255)/255f-color.y())*amount,color.z()+((tint&255)/255f-color.z())*amount);
    }
    public static void fog(Vector4f color) {
        Palette current=palette;
        if(current==null)return;
        float amount=Season.atmosphere(current.strength,Math.max(color.x,Math.max(color.y,color.z)),.44f);
        int tint=current.season.fog;
        color.x+=((tint>>16&255)/255f-color.x)*amount;
        color.y+=((tint>>8&255)/255f-color.y)*amount;
        color.z+=((tint&255)/255f-color.z)*amount;
    }
}
