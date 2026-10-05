package net.minecraft.spearcore.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.spearcore.client.animation.SpearAnimations;
import net.minecraft.spearcore.compat.punchy.PunchyCompat;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

public class SpearClientExtensions implements IClientItemExtensions {

    @Override
    /**
     * 应用玩家手持长矛时的第一人称视角变换
     * @param poseStack 姿态栈，用于处理3D变换
     * @param player 本地玩家实例
     * @param arm 应用变换的手臂（左臂或右臂）
     * @param itemInHand 玩家手中的物品
     * @param partialTick 部分游戏刻，用于插值计算
     * @param equipProcess 装备进度，用于装备动画
     * @param swingProcess 挥舞进度，用于挥舞动画
     * @return 如果应用了变换则返回true
     */
    public boolean applyForgeHandTransform(@NotNull PoseStack poseStack, @NotNull LocalPlayer player,
                                           @NotNull HumanoidArm arm, @NotNull ItemStack itemInHand,
                                           float partialTick, float equipProcess, float swingProcess) {
        if (PunchyCompat.isActive()) return false;

        // 根据手臂方向确定偏移方向（右臂为正，左臂为负）
        int dir = arm == HumanoidArm.RIGHT ? 1 : -1;

        // 处理使用物品的情况（如持矛准备攻击）
        if (player.isUsingItem() && player.getUseItem() == itemInHand) {
            applyItemArmTransform(poseStack, arm, 0.0F);
            // 计算使用物品的持续时间
            float useTicks = itemInHand.getUseDuration() - (player.getUseItemRemainingTicks() - partialTick + 1.0F);
            if (useTicks < 0) useTicks = 0;
            // 执行第一人称使用动画
            SpearAnimations.firstPersonUse(SpearAnimations.spearHitTicks + partialTick, poseStack, useTicks, arm, itemInHand);
            return true;
        } else if (swingProcess > 0.0F) {
            applyItemArmTransform(poseStack, arm, 0.0F);
            SpearAnimations.firstPersonAttack(swingProcess, poseStack, dir, arm);
            return true;
        }
        applyItemArmTransform(poseStack, arm, equipProcess);
        return true;
    }

    private static void applyItemArmTransform(PoseStack poseStack, HumanoidArm arm, float equipProcess) {
        int dir = arm == HumanoidArm.RIGHT ? 1 : -1;
        poseStack.translate(dir * 0.56F, -0.52F + equipProcess * -0.6F, -0.72F);
    }
}
