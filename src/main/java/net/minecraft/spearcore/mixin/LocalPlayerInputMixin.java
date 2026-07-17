package net.minecraft.spearcore.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.spearcore.item.SpearItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public class LocalPlayerInputMixin {

    @Shadow
    public net.minecraft.client.player.Input input;

    @Shadow
    protected int sprintTriggerTime;

    @Unique
    private boolean spearcore$wasUsingSpearLastAiStep;

    @Unique
    private boolean spearcore$wasPressingForward;

    /**
     * 原版 LocalPlayer.aiStep 在使用物品时会把 leftImpulse / forwardImpulse 乘以 0.2F。
     * 直接把持矛时的 0.2F 常量改为 1.0F，让移动计算阶段拿到未减速输入。
     */
    @ModifyConstant(method = "aiStep", constant = @Constant(floatValue = 0.2F))
    private float spearcore$disableUseItemSlowdownForSpear(float original) {
        LocalPlayer self = (LocalPlayer) (Object) this;
        return spearcore$isUsingSpear(self) ? 1.0F : original;
    }

    /**
     * 支持举矛状态下奔跑：
     * 1. 按住疾跑键 + 向前移动时直接进入奔跑；
     * 2. 双击 W 时复刻原版 sprintTriggerTime 逻辑，在第二次前进按下时进入奔跑。
     */
    @Inject(method = "aiStep", at = @At("TAIL"))
    public void afterAiStep(CallbackInfo ci) {
        LocalPlayer self = (LocalPlayer) (Object) this;
        boolean usingSpear = spearcore$isUsingSpear(self);
        boolean pressingForward = this.input.forwardImpulse >= 0.8F;

        if (usingSpear) {
            Minecraft minecraft = Minecraft.getInstance();
            boolean sprintKeyDown = minecraft.options.keySprint.isDown();

            if (pressingForward && spearcore$canStartSpearSprinting(self)) {
                if (sprintKeyDown) {
                    self.setSprinting(true);
                    this.sprintTriggerTime = 7;
                } else if (!this.spearcore$wasPressingForward) {
                    if (this.sprintTriggerTime > 0) {
                        self.setSprinting(true);
                        this.sprintTriggerTime = 0;
                    } else {
                        this.sprintTriggerTime = 7;
                    }
                }
            }
        } else if (this.spearcore$wasUsingSpearLastAiStep) {
            this.sprintTriggerTime = Math.max(this.sprintTriggerTime, 1);
        }

        this.spearcore$wasPressingForward = pressingForward;
        this.spearcore$wasUsingSpearLastAiStep = usingSpear;
    }

    @Unique
    private static boolean spearcore$isUsingSpear(LocalPlayer player) {
        return player.isUsingItem()
                && !player.getUseItem().isEmpty()
                && player.getUseItem().getItem() instanceof SpearItem;
    }

    @Unique
    private static boolean spearcore$canStartSpearSprinting(LocalPlayer player) {
        return !player.isSprinting()
                && !player.isCrouching()
                && !player.isPassenger()
                && !player.hasEffect(net.minecraft.world.effect.MobEffects.BLINDNESS)
                && (player.getFoodData().getFoodLevel() > 6.0F || player.getAbilities().mayfly);
    }
}
