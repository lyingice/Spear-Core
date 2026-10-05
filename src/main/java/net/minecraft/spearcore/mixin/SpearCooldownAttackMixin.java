package net.minecraft.spearcore.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.spearcore.item.SpearItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class SpearCooldownAttackMixin {

    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void spearcore$cancelAttackOnCooldown(CallbackInfoReturnable<Boolean> cir) {
        Minecraft self = (Minecraft)(Object)this;
        LocalPlayer player = self.player;
        if (player == null) return;

        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof SpearItem)) return;

        if (player.getAttackStrengthScale(0.0F) < 0.95F) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }
}
