package net.minecraft.spearcore.client.animation;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.spearcore.item.SpearItem;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.InteractionHand;

public class SpearAnimations {

    public static float progress(float f, float f2, float f3) {
        return Mth.clamp(Mth.inverseLerp(f, f2, f3), 0.0f, 1.0f);
    }

    // ==================== 第一人称攻击 ====================

    public static void firstPersonAttack(float f, PoseStack poseStack, int n, HumanoidArm humanoidArm) {
        float f2 = easeInOutSine(progress(f, 0.0f, 0.05f));
        float f3 = easeOutBack(progress(f, 0.05f, 0.3f));
        float f4 = easeInOutExpo(progress(f, 0.4f, 1.2f));
        poseStack.translate((float)n * 0.1f * (f2 - f3), -0.075f * (f2 - f4), 0.65f * (f2 - f3));
        poseStack.mulPose(Axis.XP.rotationDegrees(-70.0f * (f2 - f4)));
        poseStack.translate(0.0, 0.0, -0.25 * (double)(f4 - f3));
    }

    // ==================== 第一人称举矛 ====================

    public static void firstPersonUse(float f, PoseStack poseStack, float f2, HumanoidArm humanoidArm, ItemStack itemStack) {
        if (!(itemStack.getItem() instanceof SpearItem spear)) return;
        UseParams useParams = UseParams.compute(spear, f2);
        int n = humanoidArm == HumanoidArm.RIGHT ? 1 : -1;
        poseStack.translate(
                (float)n * (useParams.raiseProgress() * 0.15f + useParams.raiseProgressEnd() * -0.05f
                        + useParams.swayProgress() * -0.1f + useParams.swayScaleSlow() * 0.005f),
                useParams.raiseProgress() * -0.075f + useParams.raiseProgressMiddle() * 0.075f
                        + useParams.swayScaleFast() * 0.01f,
                useParams.raiseProgressStart() * 0.05 + useParams.raiseProgressEnd() * -0.05
                        + useParams.swayScaleSlow() * 0.005f
        );
        poseStack.rotateAround(Axis.XP.rotationDegrees(
                -65.0f * easeInOutBack(useParams.raiseProgress())
                        - 35.0f * useParams.lowerProgress()
                        + 100.0f * useParams.raiseBackProgress()
                        - 0.5f * useParams.swayScaleFast()
        ), 0.0f, 0.1f, 0.0f);
        poseStack.rotateAround(Axis.YN.rotationDegrees(
                (float)n * (-90.0f * progress(useParams.raiseProgress(), 0.5f, 0.55f)
                        + 90.0f * useParams.swayProgress()
                        + 2.0f * useParams.swayScaleSlow())
        ), (float)n * 0.15f, 0.0f, 0.0f);
        poseStack.translate(0.0f, -hitFeedbackAmount(f), 0.0f);
    }

    // ==================== 第三人称攻击物品 ====================

    public static void thirdPersonAttackItem(float attackTime, PoseStack poseStack, LivingEntity livingEntity) {
        if (attackTime <= 0.0f) return;
        float f = 0.0f;
        if (livingEntity.getMainHandItem().getItem() instanceof SpearItem spear) {
            f = spear.getForwardMovement();
        }
        float f4 = easeInQuad(progress(attackTime, 0.05f, 0.2f));
        float f5 = easeInOutExpo(progress(attackTime, 0.4f, 1.0f));
        poseStack.rotateAround(Axis.XN.rotationDegrees(70.0f * (f4 - f5)), 0.0f, -0.125f, 0.125f);
        poseStack.translate(0.0f, f * (f4 - f5), 0.0f);
    }

    // ==================== 第三人称使用物品 ====================

    public static void thirdPersonUseItem(float fs2, PoseStack poseStack, float timeHeld,
                                          HumanoidArm humanoidArm, ItemStack itemStack,
                                          LivingEntity livingEntity, float partialTick) {
        if (!(itemStack.getItem() instanceof SpearItem spear)) return;
        if (timeHeld == 0.0f) return;

        float f2 = easeInQuad(progress(livingEntity.getAttackAnim(partialTick), 0.05f, 0.2f));
        float f3 = easeInOutExpo(progress(livingEntity.getAttackAnim(partialTick), 0.4f, 1.0f));
        UseParams useParams = UseParams.compute(spear, timeHeld);
        int n = humanoidArm == HumanoidArm.RIGHT ? 1 : -1;
        float f4 = 1.0f - easeOutBack(1.0f - useParams.raiseProgress());
        float f6 = hitFeedbackAmount(spearHitTicks + partialTick);
        poseStack.translate(0.0, -f6 * 0.4, -spear.getForwardMovement() * (f4 - useParams.raiseBackProgress()) + f6);
        poseStack.rotateAround(Axis.XN.rotationDegrees(
                        70.0f * (useParams.raiseProgress() - useParams.raiseBackProgress()) - 40.0f * (f2 - f3)),
                0.0f, -0.03125f, 0.125f);
        poseStack.rotateAround(Axis.YP.rotationDegrees(
                        (float)(n * 90) * (useParams.raiseProgress() - useParams.swayProgress() + 3.0f * f3 + f2)),
                0.0f, 0.0f, 0.125f);
    }
    // ==================== 第三人称手臂攻击 ====================

    public static <T extends HumanoidModel<?>> void thirdPersonAttackHand(
            HumanoidModel<?> humanoidModel, ModelPart modelPart, LivingEntity livingEntity) {
        float f = livingEntity.getAttackAnim(Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false));
        HumanoidArm humanoidArm = livingEntity.swingingArm == InteractionHand.MAIN_HAND
                ? livingEntity.getMainArm() : livingEntity.getMainArm().getOpposite();
        humanoidModel.rightArm.yRot -= humanoidModel.body.yRot;
        humanoidModel.leftArm.yRot -= humanoidModel.body.yRot;
        humanoidModel.leftArm.xRot -= humanoidModel.body.yRot;
        float f2 = easeInOutSine(progress(f, 0.0f, 0.05f));
        float f3 = easeInQuad(progress(f, 0.05f, 0.2f));
        float f4 = easeInOutExpo(progress(f, 0.4f, 1.0f));
        modelPart.xRot += (90.0f * f2 - 120.0f * f3 + 30.0f * f4) * ((float) Math.PI / 180);
    }

// ==================== 第三人称手臂蓄力 ====================

    public static <T extends HumanoidModel<?>> void thirdPersonHandUse(
            ModelPart modelPart,
            ModelPart headPart,
            boolean isMainArm,
            ItemStack itemStack,
            T model,
            LivingEntity livingEntity
    ) {
        int n = isMainArm ? 1 : -1;

        // 根据头部旋转调整手臂
        float headYRotDeg = (180f / (float) Math.PI) * headPart.yRot;
        headYRotDeg %= 360f;
        if (headYRotDeg > 180f) headYRotDeg -= 360f;
        else if (headYRotDeg < -180f) headYRotDeg += 360f;

        float headYRotRad = headYRotDeg * ((float) Math.PI / 180f);
        modelPart.yRot = -0.1f * (float) n + headYRotRad;
        modelPart.xRot = (-(float) Math.PI / 2f) + headPart.xRot + 0.8f;

        if (livingEntity.isFallFlying() || model.swimAmount > 0.0f) {
            modelPart.xRot -= 0.9599311f;
        }

        // 限制角度范围
        modelPart.yRot = ((float) Math.PI / 180f) * Mth.clamp(
                (180f / (float) Math.PI) * modelPart.yRot, -60f, 60f);
        modelPart.xRot = ((float) Math.PI / 180f) * Mth.clamp(
                (180f / (float) Math.PI) * modelPart.xRot, -120f, 30f);

        // 蓄力摆动
        if (livingEntity.getTicksUsingItem() > 0
                && (!livingEntity.isUsingItem() || livingEntity.getUsedItemHand() == (isMainArm ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND))) {
            if (itemStack.getItem() instanceof SpearItem spear) {
                UseParams useParams = UseParams.compute(spear, livingEntity.getTicksUsingItem());
                modelPart.yRot += (float) (-n) * useParams.swayScaleFast() * ((float) Math.PI / 180f) * useParams.swayIntensity() * 1.0f;
                modelPart.zRot += (float) (-n) * useParams.swayScaleSlow() * ((float) Math.PI / 180f) * useParams.swayIntensity() * 0.5f;
                modelPart.xRot += ((float) Math.PI / 180f) * (
                        -40f * useParams.raiseProgressStart()
                                + 30f * useParams.raiseProgressMiddle()
                                - 20f * useParams.raiseProgressEnd()
                                + 20f * useParams.lowerProgress()
                                + 10f * useParams.raiseBackProgress()
                                + 0.6f * useParams.swayScaleSlow() * useParams.swayIntensity()
                );
            }
        }
    }

    // ==================== UseParams ====================

    public record UseParams(float raiseProgress, float raiseProgressStart, float raiseProgressMiddle,
                            float raiseProgressEnd, float swayProgress, float lowerProgress,
                            float raiseBackProgress, float swayIntensity, float swayScaleSlow, float swayScaleFast) {

        public static UseParams compute(SpearItem spear, float usedTicks) {
            int delayTicks = spear.getDelayTicks();
            int dismountEnd = spear.getDismountEndTick();
            int knockbackEnd = spear.getKnockbackEndTick();
            int damageEnd = spear.getDamageEndTick();

            int n = delayTicks;
            int n2 = dismountEnd;
            int n3 = n2 - 20;
            int n4 = knockbackEnd;
            int n5 = n4 - 40;
            int n6 = damageEnd;

            float f2 = progress(usedTicks, 0.0f, n > 0 ? n : 1);
            float f3 = progress(f2, 0.0f, 0.5f);
            float f4 = progress(f2, 0.5f, 0.8f);
            float f5 = progress(f2, 0.8f, 1.0f);
            float f6 = progress(usedTicks, n3, n5);
            float f7 = easeOutCubic(easeInOutElastic(progress(usedTicks - 20.0f, n5, n4)));
            float f8 = progress(usedTicks, n6 - 5, n6);
            float f9 = 2.0f * easeOutCirc(f6) - 2.0f * easeInCirc(f8);
            float f10 = Mth.sin(usedTicks * 19.0f * ((float)Math.PI / 180)) * f9;
            float f11 = Mth.sin(usedTicks * 30.0f * ((float)Math.PI / 180)) * f9;
            return new UseParams(f2, f3, f4, f5, f6, f7, f8, f9, f10, f11);
        }
    }

    // ==================== 缓动函数 ====================

    private static float easeInOutSine(float t) {
        return -(Mth.cos((float) Math.PI * t) - 1.0f) / 2.0f;
    }

    private static float easeOutBack(float t) {
        float c1 = 1.70158f;
        float c3 = c1 + 1;
        return (float) (1 + c3 * Math.pow(t - 1, 3) + c1 * Math.pow(t - 1, 2));
    }

    private static float easeInOutExpo(float t) {
        return t == 0.0f ? 0.0f : t == 1.0f ? 1.0f : t < 0.5f ?
                (float) Math.pow(2, 20 * t - 10) / 2.0f :
                (2.0f - (float) Math.pow(2, -20 * t + 10)) / 2.0f;
    }

    private static float easeInOutBack(float t) {
        float c1 = 1.70158f;
        float c2 = c1 * 1.525f;
        return t < 0.5f ?
                (float) (Math.pow(2 * t, 2) * ((c2 + 1) * 2 * t - c2)) / 2 :
                (float) ((Math.pow(2 * t - 2, 2) * ((c2 + 1) * (t * 2 - 2) + c2) + 2) / 2);
    }

    private static float easeOutQuart(float t) {
        return (float) (1 - Math.pow(1 - t, 4));
    }

    private static float easeOutCubic(float t) {
        return 1.0f - (float) Math.pow(1 - t, 3);
    }

    private static float easeInOutElastic(float t) {
        if (t == 0 || t == 1) return t;
        float c5 = (float) (2 * Math.PI) / 4.5f;
        return t < 0.5f
                ? -(float) (Math.pow(2, 20 * t - 10) * Math.sin((20 * t - 11.125) * c5)) / 2
                : (float) (Math.pow(2, -20 * t + 10) * Math.sin((20 * t - 11.125) * c5)) / 2 + 1;
    }

    private static float easeOutCirc(float t) {
        return (float) Math.sqrt(1 - Math.pow(t - 1, 2));
    }

    private static float easeInCirc(float t) {
        return 1 - (float) Math.sqrt(1 - Math.pow(t, 2));
    }

    private static float easeInQuad(float t) {
        return t * t;
    }

    private static float hitFeedbackAmount(float f) {
        return 0.4f * (easeOutQuart(progress(f, 1.0f, 3.0f)) - easeInOutSine(progress(f, 3.0f, 10.0f)));
    }

    // ==================== 外部调用 ====================

    public static int spearHitTicks = 0;

    public static void triggerHitFeedback() {
        spearHitTicks = 10;
    }
}