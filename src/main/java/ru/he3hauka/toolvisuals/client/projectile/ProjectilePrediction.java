package ru.he3hauka.toolvisuals.client.projectile;

import java.util.*;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;
import org.joml.Quaternionf;

import ru.he3hauka.toolvisuals.client.ToolVisualsClient;

import static ru.he3hauka.toolvisuals.client.module.VisualModule.PROJECTILE_PREDICTION;
import static ru.he3hauka.toolvisuals.client.module.VisualSetting.*;

public final class ProjectilePrediction {
    private static List<Ballistics.Path> paths=List.of();
    private static Object level;
    private static long updated;
    private ProjectilePrediction() {}
    public static List<Ballistics.Path> paths(float partial) {
        var client=Minecraft.getInstance();var config=ToolVisualsClient.CONFIG;
        if(!config.active(PROJECTILE_PREDICTION)||client.player==null||client.level==null||client.player.isSpectator())return List.of();
        if(level!=client.level){level=client.level;updated=0;paths=List.of();}
        long now=System.nanoTime();if(now-updated<25_000_000L)return paths;updated=now;
        var player=client.player;ItemStack stack=player.isUsingItem()?player.getUseItem():player.getMainHandItem();
        var launch=launch(client,stack);
        if(launch==null&&!player.isUsingItem()){stack=player.getOffhandItem();launch=launch(client,stack);}
        if(launch==null){paths=List.of();return paths;}
        final var spec=launch;
        var origin=player.getEyePosition(partial).add(0,-.1,0);var output=new ArrayList<Ballistics.Path>();
        for(int shot=0;shot<spec.count;shot++) {
            double yaw=Math.toRadians(player.getYRot()),pitch=Math.toRadians(player.getXRot());
            var direction=new Vec3(-Math.sin(yaw)*Math.cos(pitch),-Math.sin(Math.toRadians(player.getXRot()+spec.pitch)),Math.cos(yaw)*Math.cos(pitch));
            if(shot>0)direction=new Vec3(direction.toVector3f().rotate(new Quaternionf().setAngleAxis(Math.toRadians((shot%2==0?1:-1)*10),player.getUpVector(partial).x,player.getUpVector(partial).y,player.getUpVector(partial).z)));
            var velocity=direction.normalize().scale(spec.speed);
            if(spec.inherit){var motion=player.getKnownMovement();velocity=velocity.add(motion.x,player.onGround()?0:motion.y,motion.z);}
            var environment=new Ballistics.Environment() {
                public boolean loaded(Vec3 p){return p.y>client.level.getMinY()-8&&p.y<client.level.getMaxY()+128&&client.level.isLoaded(BlockPos.containing(p));}
                public boolean gravityBeforeMove(){return spec.throwable;}
                public boolean dragBeforeMove(Vec3 p){return !spec.throwable&&client.level.getFluidState(BlockPos.containing(p)).is(FluidTags.WATER);}
                public double drag(Vec3 p){return client.level.getFluidState(BlockPos.containing(p)).is(FluidTags.WATER)?spec.waterDrag:.99;}
                public HitResult clip(Vec3 from,Vec3 to) {
                    HitResult nearest=client.level.clip(new ClipContext(from,to,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,player));
                    if(!config.flag(PREDICT_ENTITIES))return nearest;
                    double distance=nearest.getType()==HitResult.Type.MISS?from.distanceToSqr(to):from.distanceToSqr(nearest.getLocation());
                    for(var entity:client.level.getEntities(player,new AABB(from,to).inflate(.3),e->e.isPickable()&&!e.isSpectator()&&e.isAlive()&&e.getRootVehicle()!=player.getRootVehicle())) {
                        var box=entity.getBoundingBox().inflate(.125);var crossing=box.clip(from,to);
                        if(box.contains(from)){nearest=new EntityHitResult(entity,from);distance=0;}
                        else if(crossing.isPresent()&&from.distanceToSqr(crossing.get())<distance){distance=from.distanceToSqr(crossing.get());nearest=new EntityHitResult(entity,crossing.get());}
                    }
                    return nearest;
                }
            };
            output.add(Ballistics.trace(origin,velocity,spec.gravity,environment,120));
        }
        paths=List.copyOf(output);return paths;
    }
    private static final class Launch {
        public final double speed,gravity,waterDrag;
        public final float pitch;
        public final int count;
        public final boolean inherit,throwable;
        Launch(double speed,double gravity,double waterDrag,float pitch,int count,boolean inherit){this(speed,gravity,waterDrag,pitch,count,inherit,false);}
        Launch(double speed,double gravity,double waterDrag,float pitch,int count,boolean inherit,boolean throwable){this.speed=speed;this.gravity=gravity;this.waterDrag=waterDrag;this.pitch=pitch;this.count=count;this.inherit=inherit;this.throwable=throwable;}
    }
    private static Launch launch(Minecraft client,ItemStack stack) {
        if(stack.isEmpty())return null;var config=ToolVisualsClient.CONFIG;var player=client.player;
        if(stack.getItem() instanceof BowItem&&config.flag(PREDICT_BOW)&&player.isUsingItem()) {
            float power=BowItem.getPowerForTime(player.getTicksUsingItem());
            return power<.1?null:new Launch(power*3,.05,.6,0,1,true);
        }
        if(stack.getItem() instanceof CrossbowItem&&config.flag(PREDICT_CROSSBOW)) {
            var charged=stack.getOrDefault(DataComponents.CHARGED_PROJECTILES,ChargedProjectiles.EMPTY);
            if(charged.isEmpty()||charged.contains(Items.FIREWORK_ROCKET))return null;
            return new Launch(3.15,.05,.6,0,Math.min(3,charged.items().size()),false);
        }
        if(stack.is(Items.TRIDENT)&&config.flag(PREDICT_TRIDENT)&&player.isUsingItem()&&player.getTicksUsingItem()>=10
                &&EnchantmentHelper.getTridentSpinAttackStrength(stack,player)==0)return new Launch(2.5,.05,.99,0,1,true);
        if(config.flag(PREDICT_THROWABLES)&&(stack.is(Items.ENDER_PEARL)||stack.is(Items.SNOWBALL)||stack.is(Items.EGG)))return new Launch(1.5,.03,.8,0,1,true,true);
        if(config.flag(PREDICT_POTIONS)&&(stack.is(Items.SPLASH_POTION)||stack.is(Items.LINGERING_POTION)))return new Launch(.5,.05,.8,-20,1,true,true);
        if(config.flag(PREDICT_POTIONS)&&stack.is(Items.EXPERIENCE_BOTTLE))return new Launch(.7,.07,.8,-20,1,true,true);
        return null;
    }
}
