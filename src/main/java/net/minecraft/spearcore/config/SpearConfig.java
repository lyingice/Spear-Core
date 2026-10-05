package net.minecraft.spearcore.config;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public class SpearConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue ENABLE_VANILLA_SPEARS = BUILDER
            .comment("是否启用原版材质长矛（木、石、铁、金、钻石、下界合金）")
            .define("enableVanillaSpears", false);

    public static final ModConfigSpec.BooleanValue ENABLE_COPPER_SPEAR = BUILDER
            .comment("是否启用铜长矛")
            .define("enableCopperSpear", false);

    /**
     * 安装 Punchy 时，让其矛蓄力动画按本模组各材质的阶段(tick)切换。
     * 关闭后使用 Punchy 自带的固定时间轴（10/20 tick）。仅客户端读取。
     */
    public static final ModConfigSpec.BooleanValue PUNCHY_STAGE_BRIDGE = BUILDER
            .comment("安装 Punchy 时，让其矛蓄力动画按本模组各材质的阶段(tick)切换；关闭则用 Punchy 自带的固定 10/20 tick 时间轴")
            .define("punchyStageBridge", true);

    /**
     * 命中目标后给攻击者自己减速（水平速度 ×0.6）并取消奔跑，也就是"撞完得重新起跑"。
     * 默认关闭：命中后保持速度与奔跑状态，可以接着冲、连续撞。
     * 只影响攻击者自己的动量，不影响被打飞目标的击退力度。
     */
    public static final ModConfigSpec.BooleanValue SLOW_DOWN_ATTACKER_ON_HIT = BUILDER
            .comment("命中目标时给攻击者自己减速（水平速度 ×0.6）并取消奔跑，",
                    "也就是原版那股\"撞完得重新起跑\"的手感。",
                    "默认关闭：命中后保持速度与奔跑状态，撞完接着冲。")
            .define("slowDownAttackerOnHit", false);

    public static final ModConfigSpec SPEC = BUILDER.build();

    public static void register(ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, SPEC);
    }
}
