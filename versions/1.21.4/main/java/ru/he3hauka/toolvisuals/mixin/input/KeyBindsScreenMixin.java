package ru.he3hauka.toolvisuals.mixin.input;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.*;
import net.minecraft.client.gui.screens.options.controls.KeyBindsScreen;
import net.minecraft.client.input.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import ru.he3hauka.toolvisuals.client.keybinds.BindingConflicts;

@Mixin(KeyBindsScreen.class)
public abstract class KeyBindsScreenMixin {
    @Inject(method="keyPressed",at=@At("HEAD"),cancellable=true)
    private void toolvisuals$confirmKey(int rawKey,int scan,int modifiers,CallbackInfoReturnable<Boolean> callback){if(rawKey!=InputConstants.KEY_ESCAPE&&toolvisuals$confirm(InputConstants.getKey(rawKey,scan)))callback.setReturnValue(true);}
    @Inject(method="mouseClicked",at=@At("HEAD"),cancellable=true)
    private void toolvisuals$confirmMouse(double x,double y,int button,CallbackInfoReturnable<Boolean> callback){if(toolvisuals$confirm(InputConstants.Type.MOUSE.getOrCreate(button)))callback.setReturnValue(true);}
    @Unique private boolean toolvisuals$confirm(InputConstants.Key key){
        var screen=(KeyBindsScreen)(Object)this;var mapping=screen.selectedKey;if(mapping==null||mapping.saveString().equals(key.getName()))return false;
        var conflicts=BindingConflicts.find(key,mapping,null);if(conflicts.isEmpty())return false;screen.selectedKey=null;
        BindingConflicts.confirm(screen,key,conflicts,()->{mapping.setKey(key);KeyMapping.resetMapping();Minecraft.getInstance().options.save();Minecraft.getInstance().gui.setScreen(screen);});return true;
    }
}
