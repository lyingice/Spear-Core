package net.minecraft.spearcore.mixin;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.spearcore.item.SpearItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public class LocalPlayerInputMixin {

    @Shadow
    public net.minecraft.client.player.Input input;

    @Inject(method = "aiStep", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/player/Input;tick(ZF)V", shift = At.Shift.AFTER))
    public void afterInputTick(CallbackInfo ci) {
        LocalPlayer self = (LocalPlayer) (Object) this;
        if (self.isUsingItem() && !self.getUseItem().isEmpty()
                && self.getUseItem().getItem() instanceof SpearItem) {
            // 原版在 input.tick() 之后会 if(isUsingItem) { leftImpulse *= 0.2; ... }
            // 在这之后立刻恢复
            input.leftImpulse *= 5.0F;
            input.forwardImpulse *= 5.0F;
        }
    }
}