package net.minecraft.spearcore.compat.punchy.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.spearcore.compat.punchy.PunchyCompat;
import net.minecraft.spearcore.item.SpearItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Punchy 2.6.2 的矛蓄力阶段是硬编码的：elapsed &lt; 10 → 阶段 1、&lt; 20 → 阶段 2、否则阶段 3
 * （stage3Limit=30 且 hasLimit=false），它只用 getUseDuration / getUseItemRemainingTicks
 * 计算 elapsed，不会读取物品的任何阶段参数。
 *
 * <p>本模组的矛是逐材质的 4 段式时间轴（delay → dismount → knockback → damage），
 * 因此这里在 Punchy 算完 ChargeInfo 之后，用 SpearItem 自己的 tick 覆盖它的阶段：
 * 举矛段(delay+dismount) → 1(Engaged)、击退段 → 2(Tired)、仅伤害段 → 3(Disengaged)。
 *
 * <p>hasLimit 固定为 false：让姿势在"不再使用物品"时自然收起。若设为 true，Punchy 会置
 * chargeMaxed 并在按键不松手时一直抑制后续蓄力动画，而本模组蓄力结束后按住使用键会自动
 * 重新蓄力，那样会出现"第二轮没有举矛动画"的回归。
 */
@Mixin(targets = "punchy.client.state.SpearStateMachine")
public abstract class SpearStateMachineMixin {

    // remap = false：目标是 Punchy 的方法（mod 类不被混淆），
    // 不去 remap 才能让 Mixin 注解处理器找到它；Mixin 体内对 MC 的调用仍会正常 remap。
    @Inject(method = "resolveChargeInfo", at = @At("RETURN"), remap = false)
    private void spearcore$retimeSpearCharge(Minecraft minecraft, ItemStack stack,
                                             CallbackInfoReturnable<Object> cir) {
        Object info = cir.getReturnValue();
        if (!(info instanceof ChargeInfoAccessor accessor)) return;
        if (minecraft == null || minecraft.player == null) return;
        if (!(stack.getItem() instanceof SpearItem spear)) return;
        if (!PunchyCompat.isStageBridgeEnabled()) return;

        int usedTicks = Math.max(0, stack.getUseDuration()
                - minecraft.player.getUseItemRemainingTicks());

        accessor.spearcore$setStage(PunchyCompat.chargeStage(spear, usedTicks));
        accessor.spearcore$setElapsedTicks(usedTicks);
        accessor.spearcore$setStage3Limit(PunchyCompat.chargeEndTick(spear));
        accessor.spearcore$setHasLimit(false);
        PunchyCompat.logStageBridgeOnce();
    }
}
