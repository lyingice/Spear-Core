package net.minecraft.spearcore.compat.punchy.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * 访问 Punchy 的 ChargeInfo（包私有嵌套类，字段为 final）。
 *
 * <p>用 targets 字符串而不是类字面量：未安装 Punchy 时本 Mixin 不会被应用，
 * 也不会因为常量池引用缺失的类而报错。
 */
// remap = false：目标字段属于 Punchy（mod 类不被混淆）。本接口里没有 MC 引用，所以类级关掉 remap 是安全的。
@Mixin(targets = "punchy.client.state.SpearStateMachine$ChargeInfo", remap = false)
public interface ChargeInfoAccessor {

    @Mutable
    @Accessor("stage")
    void spearcore$setStage(int stage);

    @Mutable
    @Accessor("elapsedTicks")
    void spearcore$setElapsedTicks(int elapsedTicks);

    @Mutable
    @Accessor("stage3Limit")
    void spearcore$setStage3Limit(int stage3Limit);

    @Mutable
    @Accessor("hasLimit")
    void spearcore$setHasLimit(boolean hasLimit);
}
