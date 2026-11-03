package ru.he3hauka.toolvisuals.client.cape;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.Identifier;

import ru.he3hauka.toolvisuals.client.optimization.texture.TextureUploads;
import ru.he3hauka.toolvisuals.cosmetic.cape.CapeDesign;

public final class CapeTexture implements AutoCloseable {
    private static long sequence;
    public final Identifier id=Identifier.fromNamespaceAndPath("toolvisuals","capes/"+sequence++);
    public final ClientAsset.Texture asset=new ClientAsset.ResourceTexture(id,id);
    private final boolean foreground;
    private DynamicTexture texture;
    private String hash;
    private CapeDesign desired;
    private Object pending;
    private long retryAfter;
    private long lastUpload=Long.MIN_VALUE;
    private net.minecraft.world.entity.player.PlayerSkin original,replacement;
    public CapeTexture(){this(false);}
    public CapeTexture(boolean foreground){this.foreground=foreground;}
    public boolean ready(){return texture!=null&&hash!=null;}
    public ClientAsset.Texture update(CapeDesign design){
        if(design.frameCount>1)design=ru.he3hauka.toolvisuals.cosmetic.cape.CapeAnimationPolicy.frame(design,System.nanoTime());
        desired=design;
        long now=System.nanoTime();
        if(!design.hash.equals(hash)&&pending==null&&now>=retryAfter&&ru.he3hauka.toolvisuals.cosmetic.cape.CapeAnimationPolicy.uploadDue(lastUpload,now)){
            Object ticket=new Object();pending=ticket;
            boolean accepted=TextureUploads.offer(this,CapeAtlas.WIDTH*CapeAtlas.HEIGHT*4L,foreground,()->{
                if(pending!=ticket)return;
                try{
                    var next=desired;
                    if(texture==null){
                        var pixels=new NativeImage(CapeAtlas.WIDTH,CapeAtlas.HEIGHT,false);CapeAtlas.paint(next,pixels::setPixel);
                        texture=new DynamicTexture(()->id.toString(),pixels);Minecraft.getInstance().getTextureManager().register(id,texture);
                    }else{CapeAtlas.paint(next,texture.getPixels()::setPixel);texture.upload();}
                    hash=next.hash;
                    lastUpload=System.nanoTime();
                }finally{if(pending==ticket)pending=null;}
            },()->{if(pending==ticket)pending=null;retryAfter=System.nanoTime()+1_000_000_000L;});
            if(!accepted){pending=null;retryAfter=System.nanoTime()+100_000_000L;}
        }
        return ready()?asset:null;
    }
    public void apply(net.minecraft.client.renderer.entity.state.AvatarRenderState state,CapeDesign design){
        var cape=update(design);if(cape==null||state.skin==null)return;
        if(state.skin!=original&&state.skin!=replacement){original=state.skin;CapeClient.skin(state,cape);replacement=state.skin;}
        else{state.skin=replacement;state.showCape=true;}
    }
    @Override public void close(){TextureUploads.cancel(this);pending=null;desired=null;original=replacement=null;retryAfter=0;lastUpload=Long.MIN_VALUE;if(texture!=null){Minecraft.getInstance().getTextureManager().release(id);texture=null;}hash=null;}
}
