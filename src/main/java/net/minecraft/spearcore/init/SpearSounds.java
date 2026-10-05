/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.minecraft.spearcore.init;

import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.resources.ResourceLocation;

import net.minecraft.spearcore.SpearcoreMod;

public class SpearSounds {
	public static final DeferredRegister<SoundEvent> REGISTRY = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, SpearcoreMod.MODID);
	public static final RegistryObject<SoundEvent> ITEM_SPEAR_ATTACK = REGISTRY.register("item.spear.attack", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("spearcore", "item.spear.attack")));
	public static final RegistryObject<SoundEvent> ITEM_SPEAR_HIT = REGISTRY.register("item.spear.hit", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("spearcore", "item.spear.hit")));
	public static final RegistryObject<SoundEvent> ITEM_SPEAR_USE = REGISTRY.register("item.spear.use", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("spearcore", "item.spear.use")));
	public static final RegistryObject<SoundEvent> ITEM_SPEAR_WOOD_ATTACK = REGISTRY.register("item.spear_wood.attack", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("spearcore", "item.spear_wood.attack")));
	public static final RegistryObject<SoundEvent> ITEM_SPEAR_WOOD_USE = REGISTRY.register("item.spear_wood.use", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("spearcore", "item.spear_wood.use")));
	public static final RegistryObject<SoundEvent> ITEM_SPEAR_WOOD_HIT = REGISTRY.register("item.spear_wood.hit", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("spearcore", "item.spear_wood.hit")));
	public static final RegistryObject<SoundEvent> ITEM_SPEAR_LUNGE_1 = REGISTRY.register("item.spear.lunge_1", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("spearcore", "item.spear.lunge_1")));
	public static final RegistryObject<SoundEvent> ITEM_SPEAR_LUNGE_2 = REGISTRY.register("item.spear.lunge_2", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("spearcore", "item.spear.lunge_2")));
	public static final RegistryObject<SoundEvent> ITEM_SPEAR_LUNGE_3 = REGISTRY.register("item.spear.lunge_3", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("spearcore", "item.spear.lunge_3")));
}
