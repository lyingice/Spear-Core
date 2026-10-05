package net.minecraft.spearcore.compat.punchy;

/**
 * Punchy 兼容的占位实现。
 *
 * <p>按移植计划，Punchy 兼容属于第二轮；这里先放一个恒为 false 的桩，
 * 让客户端代码（SpearClientExtensions）保持原样，行为等价于"没装 Punchy"。
 * 第二轮只需把这个类替换成真正的 PunchyCompat（含 mixin 配置）。</p>
 */
public final class PunchyCompat {

    private PunchyCompat() {
    }

    public static boolean isActive() {
        return false;
    }
}
