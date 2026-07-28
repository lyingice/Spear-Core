package net.minecraft.spearcore.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.minecraft.spearcore.SpearcoreMod;

public class SpearAttributes {
    public static final DeferredRegister<Attribute> ATTRIBUTES =
            DeferredRegister.create(Registries.ATTRIBUTE, SpearcoreMod.MODID);

    public static final DeferredHolder<Attribute, Attribute> SPEAR_STAB_MULTIPLIER =
            ATTRIBUTES.register("spear_stab_multiplier",
                    () -> new RangedAttribute("attribute.spearcore.spear_stab_multiplier", 1.0, 0.0, 1024.0)
                            .setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> SPEAR_CHARGE_MULTIPLIER =
            ATTRIBUTES.register("spear_charge_multiplier",
                    () -> new RangedAttribute("attribute.spearcore.spear_charge_multiplier", 1.0, 0.0, 1024.0)
                            .setSyncable(true));
    public static void addToPlayer(EntityAttributeModificationEvent event) {
        event.add(EntityType.PLAYER, SPEAR_STAB_MULTIPLIER);
        event.add(EntityType.PLAYER, SPEAR_CHARGE_MULTIPLIER);
    }
}