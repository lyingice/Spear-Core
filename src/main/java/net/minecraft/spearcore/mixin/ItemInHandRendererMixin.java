package net.minecraft.spearcore.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.spearcore.item.SpearItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {

    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "renderArmWithItem", at = @At("HEAD"), cancellable = true)
    public void onRenderArmWithItem(AbstractClientPlayer player, float partialTick, float pitch,
                                    InteractionHand hand, float swingProgress, ItemStack stack,
                                    float equipProgress, PoseStack poseStack, MultiBufferSource buffer,
                                    int light, CallbackInfo ci) {
        if (stack.getItem() instanceof SpearItem) {
            boolean flag = hand == InteractionHand.MAIN_HAND;
            HumanoidArm arm = flag ? player.getMainArm() : player.getMainArm().getOpposite();

            poseStack.pushPose();

            // 调用 SpearClientExtensions 做动画
            IClientItemExtensions.of(stack).applyForgeHandTransform(
                    poseStack, minecraft.player, arm, stack, partialTick, equipProgress, swingProgress
            );

            // 渲染物品
            ((ItemInHandRenderer)(Object)this).renderItem(
                    player, stack,
                    flag ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND,
                    !flag, poseStack, buffer, light
            );

            poseStack.popPose();
            ci.cancel();
        }
    }
}