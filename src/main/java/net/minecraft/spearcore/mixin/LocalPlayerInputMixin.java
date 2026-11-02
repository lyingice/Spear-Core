package net.minecraft.spearcore.mixin;

import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.spearcore.item.SpearItem;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 举矛时取消原版"正在使用物品 → 移动输入 ×0.2"的减速，并允许起跑。
 *
 * <p><b>为什么是 @Redirect 而不是 @ModifyConstant（issue #3）：</b>
 * {@code @ModifyConstant} 对同一条常量只允许一个模组生效。我们抢先改掉 aiStep 里的 0.2F 后，
 * 其它模组（例如 Tensura 的 useItemFirst）对同一常量的 {@code @ModifyConstant} 会找不到注入目标，
 * 抛 {@code InjectionError: Critical injection failure ... (0/1) succeeded} 直接启动崩溃，
 * 且与 priority 无关。</p>
 *
 * <p>{@code @Redirect} 拦的是 {@code input.leftImpulse = ...} 这次写回，不碰常量本身：
 * 别的模组仍可自由 {@code @ModifyConstant} 那条 0.2F；写回前读到的字段就是未被乘过的旧值，
 * 直接写回去即可。代价是：如果另一个模组也用 {@code @Redirect} 拦同一次写回，两者会冲突，
 * 但那属于同一注入点的硬冲突，无法用普通 Mixin 化解。</p>
 *
 * <p><b>为什么不再用 MixinExtras 的 @WrapOperation：</b>
 * 1.20.1 的 Forge 不自带 MixinExtras，原先靠 shading 把它摊进本模组 jar。结果本模组变成
 * 导出 {@code com.llamalad7.mixinextras.*} 包的模块，只要别的模组（如 justenoughbreeding）
 * 也通过 jar-in-jar 带了 MixinExtras，ModLauncher 就会报
 * {@code Modules spearcore and MixinExtras export package ... to module ...} 直接启动失败。
 * 上游在 3.2.0hotfix 里同样去掉了内嵌（他们退回 @ModifyConstant），这里改用 @Redirect，
 * 既不需要 MixinExtras，又保留了"不抢常量"的兼容性。</p>
 */
@Mixin(value = LocalPlayer.class, priority = 2000)
public class LocalPlayerInputMixin {

    @Redirect(method = "aiStep",
            at = @At(value = "FIELD",
                    target = "Lnet/minecraft/client/player/Input;leftImpulse:F",
                    opcode = Opcodes.PUTFIELD))
    private void spearcore$keepLeftImpulse(Input input, float value) {
        input.leftImpulse = spearcore$shouldKeepImpulse() ? input.leftImpulse : value;
    }

    @Redirect(method = "aiStep",
            at = @At(value = "FIELD",
                    target = "Lnet/minecraft/client/player/Input;forwardImpulse:F",
                    opcode = Opcodes.PUTFIELD))
    private void spearcore$keepForwardImpulse(Input input, float value) {
        input.forwardImpulse = spearcore$shouldKeepImpulse() ? input.forwardImpulse : value;
    }

    /** 原版蓄力时移动输入会被乘 0.2，这里去掉这层减速（原版 Java 版蓄力本来就不减速）。 */
    private boolean spearcore$shouldKeepImpulse() {
        return spearcore$isUsingSpear((LocalPlayer) (Object) this);
    }

    /**
     * 举矛时允许起跑。
     *
     * <p>原版"使用物品 → 移动输入 ×0.2"的分支里还有一句 {@code sprintTriggerTime = 0}，
     * 它让双击 W / 按冲刺键都无法起跑。这里在源头放行（参考 Backported Spears 的做法），
     * 比每 tick 事后 {@code setSprinting(true)} 干净得多。</p>
     *
     * <p>{@code require = 0}：该方法若在某个版本不存在，注入静默跳过而不是崩；
     * 那种情况由 {@code SpearSprintHandler} 兜底。</p>
     */
    @Inject(method = "canStartSprinting", at = @At("HEAD"), cancellable = true, require = 0)
    private void spearcore$allowSprintWhileUsingSpear(CallbackInfoReturnable<Boolean> cir) {
        if (spearcore$isUsingSpear((LocalPlayer) (Object) this)) {
            cir.setReturnValue(true);
        }
    }

    private static boolean spearcore$isUsingSpear(LocalPlayer player) {
        return player.isUsingItem()
                && !player.getUseItem().isEmpty()
                && player.getUseItem().getItem() instanceof SpearItem;
    }
}
