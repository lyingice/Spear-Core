package net.minecraft.spearcore.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.spearcore.client.animation.SpearAnimations;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

public class SpearClientExtensions implements IClientItemExtensions {

    @Override
    public boolean applyForgeHandTransform(@NotNull PoseStack poseStack, @NotNull LocalPlayer player,
                                           @NotNull HumanoidArm arm, @NotNull ItemStack itemInHand,
                                           float partialTick, float equipProcess, float swingProcess) {
        int dir = arm == HumanoidArm.RIGHT ? 1 : -1;

        if (player.isUsingItem() && player.getUseItem() == itemInHand) {
            poseStack.translate(dir * 0.56F, -0.52F, -0.72F);
            float useTicks = itemInHand.getUseDuration(player) - (player.getUseItemRemainingTicks() - partialTick + 1.0F);
            if (useTicks < 0) useTicks = 0;
            SpearAnimations.firstPersonUse(SpearAnimations.spearHitTicks + partialTick, poseStack, useTicks, arm, itemInHand);
            return true;
        } else if (swingProcess > 0.0F) {
            poseStack.translate(dir * 0.56F, -0.52F, -0.72F);
            SpearAnimations.firstPersonAttack(swingProcess, poseStack, dir, arm);
            return true;
        }
        // 闲置：固定位置，不走 equipProcess 渐变
        poseStack.translate(dir * 0.56F, -0.52F, -0.72F);
        return true;
    }
}