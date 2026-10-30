package ru.he3hauka.toolvisuals.client.camera;

import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;

import static ru.he3hauka.toolvisuals.client.camera.CameraLeases.Owner;

public final class ClientCamera {
    private static final CameraLeases<CameraType> LEASES=new CameraLeases<>();
    private ClientCamera(){}
    public static void emote(Minecraft client){
        if(LEASES.base(client.options.getCameraType()).isFirstPerson())acquire(client,Owner.EMOTE);
    }
    public static void acquire(Minecraft client,Owner owner){acquire(client,owner,CameraType.THIRD_PERSON_BACK);}
    public static void acquire(Minecraft client,Owner owner,CameraType desired){apply(client,LEASES.acquire(owner,desired,client.options.getCameraType()));}
    public static void release(Minecraft client,Owner owner){apply(client,LEASES.release(owner,client.options.getCameraType()));}
    public static void update(Minecraft client){apply(client,LEASES.update(client.options.getCameraType()));}
    private static void apply(Minecraft client,CameraType value){if(client.options.getCameraType()!=value)client.options.setCameraType(value);}
}
