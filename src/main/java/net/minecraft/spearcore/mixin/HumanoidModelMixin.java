package net.minecraft.spearcore.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.spearcore.client.animation.SpearAnimations;
import net.minecraft.spearcore.item.SpearItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
public abstract class HumanoidModelMixin<T extends LivingEntity> {

    @Shadow @Final public ModelPart rightArm;
    @Shadow @Final public ModelPart leftArm;
    @Shadow @Final public ModelPart head;

    /**
     * 在 setupAnim 之后注入，修改手臂旋转以适配长矛动画
     * 对照原版 SpearAnimations.thirdPersonHandUse
     */
    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At("TAIL"))
    public void onSetupAnim(T entity, float limbSwing, float limbSwingAmount,
                            float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        ItemStack mainHand = entity.getMainHandItem();
        ItemStack offHand = entity.getOffhandItem();

        boolean mainIsSpear = mainHand.getItem() instanceof SpearItem;
        boolean offIsSpear = offHand.getItem() instanceof SpearItem;

        if (!mainIsSpear && !offIsSpear) return;

        HumanoidArm mainArm = entity.getMainArm();

        // 攻击动画 — 手臂
        float attackAnim = entity.attackAnim;
        if (attackAnim > 0.0F) {
            HumanoidArm attackArm = entity.swingingArm == InteractionHand.MAIN_HAND
                    ? mainArm : mainArm.getOpposite();
            ModelPart arm = getArm((HumanoidModel<T>) (Object) this, attackArm);
            SpearAnimations.thirdPersonAttackHand((HumanoidModel<?>) (Object) this, arm, entity);
        }

        // 蓄力动画 — 手臂
        float useTicks = entity.getTicksUsingItem();
        if (useTicks > 0.0F) {
            if (mainIsSpear) {
                ModelPart arm = getArm((HumanoidModel<T>) (Object) this, mainArm);
                SpearAnimations.thirdPersonHandUse(arm, head,
                        entity.getUsedItemHand() == InteractionHand.MAIN_HAND,
                        mainHand, (HumanoidModel<?>) (Object) this, entity);
            }
            if (offIsSpear) {
                ModelPart arm = getArm((HumanoidModel<T>) (Object) this, mainArm.getOpposite());
                SpearAnimations.thirdPersonHandUse(arm, head,
                        entity.getUsedItemHand() == InteractionHand.OFF_HAND,
                        offHand, (HumanoidModel<?>) (Object) this, entity);
            }
        }
    }

    private static ModelPart getArm(HumanoidModel<?> model, HumanoidArm arm) {
        return arm == HumanoidArm.LEFT ? model.leftArm : model.rightArm;
    }
}