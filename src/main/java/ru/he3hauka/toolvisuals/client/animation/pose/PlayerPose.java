package ru.he3hauka.toolvisuals.client.animation.pose;

public record PlayerPose(Limb head,Limb body,Limb rightArm,Limb leftArm,Limb rightLeg,Limb leftLeg) {
    public record Limb(float x,float y,float z,float weight) {
        public Limb {if(!Float.isFinite(x)||!Float.isFinite(y)||!Float.isFinite(z)||!Float.isFinite(weight)||weight<0||weight>1)throw new IllegalArgumentException("Invalid visual pose");}
    }
    public static final PlayerPose EMPTY=new PlayerPose(null,null,null,null,null,null);
    public static Limb limb(double x,double y,double z){return new Limb((float)x,(float)y,(float)z,1);}
    public static PlayerPose mix(PlayerPose from,PlayerPose to,double fraction){
        float t=(float)Math.clamp(fraction,0,1);if(from==null)from=EMPTY;if(to==null)to=EMPTY;
        return new PlayerPose(mix(from.head,to.head,t),mix(from.body,to.body,t),mix(from.rightArm,to.rightArm,t),mix(from.leftArm,to.leftArm,t),mix(from.rightLeg,to.rightLeg,t),mix(from.leftLeg,to.leftLeg,t));
    }
    private static Limb mix(Limb a,Limb b,float t){
        if(a==null&&b==null)return null;float aw=a==null?0:a.weight*(1-t),bw=b==null?0:b.weight*t,w=aw+bw;
        if(w<.00001)return null;
        return new Limb(((a==null?0:a.x*aw)+(b==null?0:b.x*bw))/w,((a==null?0:a.y*aw)+(b==null?0:b.y*bw))/w,((a==null?0:a.z*aw)+(b==null?0:b.z*bw))/w,Math.clamp(w,0,1));
    }
    public PlayerPose strength(float amount){return amount>=1?this:mix(EMPTY,this,Math.clamp(amount,0,1));}
}
