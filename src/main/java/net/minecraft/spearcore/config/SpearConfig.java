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

    public static final ModConfigSpec SPEC = BUILDER.build();

    public static void register(ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, SPEC);
    }
}