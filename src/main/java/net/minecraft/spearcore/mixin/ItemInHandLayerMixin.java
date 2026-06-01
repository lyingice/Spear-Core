package net.minecraft.spearcore.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.spearcore.client.animation.SpearAnimations;
import net.minecraft.spearcore.item.SpearItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandLayer.class)
public abstract class ItemInHandLayerMixin<T extends LivingEntity, M extends EntityModel<T> & ArmedModel> extends RenderLayer<T, M> {

    @Shadow @Final private ItemInHandRenderer itemInHandRenderer;

    public ItemInHandLayerMixin(RenderLayerParent<T, M> renderLayerParent) {
        super(renderLayerParent);
    }

    @Inject(method = "renderArmWithItem", at = @At("HEAD"), cancellable = true)
    public void onRenderArmWithItem(LivingEntity livingEntity, ItemStack itemStack,
                                    ItemDisplayContext itemDisplayContext, HumanoidArm humanoidArm,
                                    PoseStack poseStack, MultiBufferSource buffer, int light,
                                    CallbackInfo ci) {
        if (!(itemStack.getItem() instanceof SpearItem)) return;
        if (itemStack.isEmpty()) return;

        poseStack.pushPose();
        ((ArmedModel) this.getParentModel()).translateToHand(humanoidArm, poseStack);
        poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        boolean flag = humanoidArm == HumanoidArm.LEFT;
        poseStack.translate((float)(flag ? -1 : 1) / 16.0F, 0.125F, -0.625F);

        // 应用第三人称动画
        if (livingEntity.attackAnim > 0.0F && livingEntity.getMainArm() == humanoidArm) {
            SpearAnimations.thirdPersonAttackItem(livingEntity.attackAnim, poseStack, livingEntity);
        }
        float useTicks = livingEntity.getTicksUsingItem();
        if (useTicks != 0.0f) {
            SpearAnimations.thirdPersonUseItem(livingEntity.attackAnim, poseStack, useTicks,
                    humanoidArm, itemStack, livingEntity, Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false));
        }

        // 渲染物品
        this.itemInHandRenderer.renderItem(livingEntity, itemStack, itemDisplayContext,
                flag, poseStack, buffer, light);

        poseStack.popPose();
        ci.cancel();
    }
}