package net.minecraft.spearcore.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.spearcore.SpearcoreMod;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public final class SpearDamageTypes {
    public static final ResourceKey<DamageType> SPEAR = ResourceKey.create(
            Registries.DAMAGE_TYPE,
            new ResourceLocation(SpearcoreMod.MODID, "spear")
    );

    private SpearDamageTypes() {
    }

    public static DamageSource spear(Level level, Entity attacker) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(SPEAR), attacker);
    }
}
