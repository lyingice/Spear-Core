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

    /** 原版双击 W 的判定窗口；LocalPlayer 里那个字面量就是 7 刻。 */
    private static final int SPEAR_SPRINT_TAP_WINDOW = 7;

    @Shadow
    public net.minecraft.client.player.Input input;

    @Shadow
    protected int sprintTriggerTime;

    @Unique
    private boolean spearcore$wasUsingSpearLastAiStep;

    @Unique
    private boolean spearcore$wasPressingForward;

    /**
     * 自己维护的双击 W 计时器。
     *
     * <p><b>为什么不能用原版的 {@code sprintTriggerTime}：</b>原版 aiStep 在"正在使用物品"
     * 分支里每刻都把它清零（字节码 offset 209-211：iconst_0 / putfield sprintTriggerTime），
     * 只有后面那段 {@code 0.2F} 减速被 ModifyConstant 抵消了、清零没有被抵消。
     * 于是举矛时用它做双击判定永远只会读到 0，第二次按 W 只是重新装填，永远进不了奔跑。</p>
     */
    @Unique
    private int spearcore$sprintTapTimer;

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
     * 2. 双击 W 时用自维护的计时器判定，在第二次前进按下时进入奔跑。
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
                    this.spearcore$sprintTapTimer = 0;
                } else if (!this.spearcore$wasPressingForward) {
                    // 只在这一刻"刚按下 W"时判定，按住不算
                    if (this.spearcore$sprintTapTimer > 0) {
                        self.setSprinting(true);
                        this.spearcore$sprintTapTimer = 0;
                    } else {
                        this.spearcore$sprintTapTimer = SPEAR_SPRINT_TAP_WINDOW;
                    }
                }
            }
        } else {
            this.spearcore$sprintTapTimer = 0;
            if (this.spearcore$wasUsingSpearLastAiStep) {
                // 收矛的瞬间把原版触发器顶一下，保住已经在跑的奔跑状态
                this.sprintTriggerTime = Math.max(this.sprintTriggerTime, 1);
            }
        }

        if (this.spearcore$sprintTapTimer > 0) {
            this.spearcore$sprintTapTimer--;
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
