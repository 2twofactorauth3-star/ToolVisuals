package ru.he3hauka.toolvisuals.mixin.armor;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import ru.he3hauka.toolvisuals.client.armor.ArmorTint;
import ru.he3hauka.toolvisuals.client.friends.FriendRenderState;

@Mixin(AvatarRenderState.class)
public class ArmorPreviewStateMixin implements ArmorTint.Preview, FriendRenderState {
    @Unique private boolean toolvisuals$preview;
    public boolean toolvisuals$armorPreview() { return toolvisuals$preview; }
    public void toolvisuals$armorPreview(boolean value) { toolvisuals$preview = value; }
    @Unique private int toolvisuals$friendColor;
    public int toolvisuals$friendTint() { return toolvisuals$friendColor; }
    public void toolvisuals$friendTint(int value) { toolvisuals$friendColor = value; }
    @Unique private int toolvisuals$friendBodyColor;
    public int toolvisuals$friendBodyTint() { return toolvisuals$friendBodyColor; }
    public void toolvisuals$friendBodyTint(int value) { toolvisuals$friendBodyColor = value; }
    @Unique private boolean toolvisuals$friendFill;
    public boolean toolvisuals$friendArmorFill() { return toolvisuals$friendFill; }
    public void toolvisuals$friendArmorFill(boolean value) { toolvisuals$friendFill=value; }
    @Unique private Component toolvisuals$friendBadge;
    public Component toolvisuals$friendLabel() { return toolvisuals$friendBadge; }
    public void toolvisuals$friendLabel(Component value) { toolvisuals$friendBadge=value; }
}
