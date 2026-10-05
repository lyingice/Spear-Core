package net.minecraft.spearcore.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class SpearConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.BooleanValue ENABLE_VANILLA_SPEARS = BUILDER
            .comment("是否启用原版材质长矛（木、石、铁、金、钻石、下界合金）")
            .define("enableVanillaSpears", false);

    public static final ForgeConfigSpec.BooleanValue ENABLE_COPPER_SPEAR = BUILDER
            .comment("是否启用铜长矛")
            .define("enableCopperSpear", false);

    public static final ForgeConfigSpec.BooleanValue SLOW_DOWN_ATTACKER_ON_HIT = BUILDER
            .comment("命中目标时给攻击者自己减速（水平速度 ×0.6）并取消奔跑，",
                    "也就是原版那股\"撞完得重新起跑\"的手感。",
                    "默认关闭：命中后保持速度与奔跑状态，撞完接着冲。")
            .define("slowDownAttackerOnHit", false);

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SPEC);
    }
}
