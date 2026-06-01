package net.minecraft.spearcore.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.spearcore.SpearcoreMod;
import net.minecraft.spearcore.init.SpearStats;
import net.minecraft.spearcore.util.SpearCondition;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public abstract class BaseSpearItem extends SpearItem {

    private final Supplier<Item> selfItemSupplier;

    // ========== 阶段配置字段 ==========
    private final float swingTimes;
    private final float hitboxMargin;
    private final int contactCooldownTicks;
    private final int delayTicks;
    private final Optional<SpearCondition> dismountConditions;
    private final Optional<SpearCondition> knockbackConditions;
    private final Optional<SpearCondition> damageConditions;
    private final float forwardMovement;
    private final float damageMultiplier;
    private final float minRange;
    private final float maxRange;
    private final float minCreativeRange;
    private final float maxCreativeRange;
    private final float hitboxMargin2;
    private final float mobFactor;
    private final boolean dealsKnockback;
    private final boolean dismounts;

    protected BaseSpearItem(SpearStats.Stats stats) {
        super(buildProperties(stats));
        this.selfItemSupplier = () -> this;
        SpearStats.register(this, stats);

        this.swingTimes = stats.swingTimes();
        this.hitboxMargin = stats.hitboxMargin();
        this.contactCooldownTicks = stats.contactCooldownTicks();
        this.delayTicks = stats.delayTicks();
        this.dismountConditions = stats.dismountConditions();
        this.knockbackConditions = stats.knockbackConditions();
        this.damageConditions = stats.damageConditions();
        this.forwardMovement = stats.forwardMovement();
        this.damageMultiplier = stats.damageMultiplier();
        this.minRange = stats.minRange();
        this.maxRange = stats.maxRange();
        this.minCreativeRange = stats.minCreativeRange();
        this.maxCreativeRange = stats.maxCreativeRange();
        this.hitboxMargin2 = stats.hitboxMargin2();
        this.mobFactor = stats.mobFactor();
        this.dealsKnockback = stats.dealsKnockback();
        this.dismounts = stats.dismounts();
    }

    protected BaseSpearItem(SpearStats.Stats stats, Properties customProps) {
        super(customProps);
        this.selfItemSupplier = () -> this;
        SpearStats.register(this, stats);

        this.swingTimes = stats.swingTimes();
        this.hitboxMargin = stats.hitboxMargin();
        this.contactCooldownTicks = stats.contactCooldownTicks();
        this.delayTicks = stats.delayTicks();
        this.dismountConditions = stats.dismountConditions();
        this.knockbackConditions = stats.knockbackConditions();
        this.damageConditions = stats.damageConditions();
        this.forwardMovement = stats.forwardMovement();
        this.damageMultiplier = stats.damageMultiplier();
        this.minRange = stats.minRange();
        this.maxRange = stats.maxRange();
        this.minCreativeRange = stats.minCreativeRange();
        this.maxCreativeRange = stats.maxCreativeRange();
        this.hitboxMargin2 = stats.hitboxMargin2();
        this.mobFactor = stats.mobFactor();
        this.dealsKnockback = stats.dealsKnockback();
        this.dismounts = stats.dismounts();
    }

    // ========== Properties 构建 ==========

    private static Properties buildProperties(SpearStats.Stats stats) {
        Properties props = new Properties()
                .stacksTo(1)
                .durability(stats.durability())
                .rarity(stats.rarity());
        if (stats.fireResistant()) {
            props.fireResistant();
        }
        return props;
    }

    protected static Properties buildPropertiesWithAffix(SpearStats.Stats stats, CustomData affixData) {
        Properties props = buildProperties(stats);
        props.component(DataComponents.CUSTOM_DATA, affixData);
        return props;
    }

    private Item getSelfItem() {
        return selfItemSupplier.get();
    }

    // ========== 属性修饰符 ==========

    private static ItemAttributeModifiers buildAttributeModifiers(SpearStats.Stats stats) {
        var builder = ItemAttributeModifiers.builder();

        builder.add(Attributes.ATTACK_DAMAGE,
                new AttributeModifier(BASE_ATTACK_DAMAGE_ID,
                        stats.attackDamageBonus(),
                        AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.MAINHAND);

        builder.add(Attributes.ATTACK_SPEED,
                new AttributeModifier(BASE_ATTACK_SPEED_ID,
                        stats.getAttackSpeedModifier(),
                        AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.MAINHAND);

        builder.add(Attributes.ENTITY_INTERACTION_RANGE,
                new AttributeModifier(ResourceLocation.fromNamespaceAndPath(SpearcoreMod.MODID, "spear_range"),
                        1.5, AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.MAINHAND);



        return builder.build();
    }

    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
        return buildAttributeModifiers(SpearStats.get(getSelfItem()));
    }

    // ========== SpearItem 抽象方法实现 ==========

    @Override
    public float getAttackDuration() {
        return SpearStats.attackDuration(getSelfItem());
    }

    @Override
    public  float getDamageMultiplier() {
        return SpearStats.damageMultiplier(getSelfItem());
    }

    @Override
    public  SoundEvent getUseSound() {
        return SpearStats.useSound(getSelfItem());
    }

    @Override
    public SoundEvent getHitSound() {
        return SpearStats.hitSound(getSelfItem());
    }

    @Override
    public SoundEvent getAttackSound() {
        return SpearStats.attackSound(getSelfItem());
    }

    @Override
    public int getEnchantmentValue() {
        return SpearStats.enchantmentValue(getSelfItem());
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return SpearStats.repairIngredient(getSelfItem()).test(repair);
    }

    @Override
    public void postHurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
    }

    // ========== 蓄力阶段 Getter ==========

    @Override
    public int getDelayTicks() { return delayTicks; }

    @Override
    public int getDismountEndTick() {
        return delayTicks + dismountConditions.map(SpearCondition::maxDurationTicks).orElse(0);
    }

    @Override
    public int getKnockbackEndTick() {
        return delayTicks + knockbackConditions.map(SpearCondition::maxDurationTicks).orElse(0);
    }

    @Override
    public int getDamageEndTick() {
        return delayTicks + damageConditions.map(SpearCondition::maxDurationTicks).orElse(0);
    }

    @Override
    public Optional<SpearCondition> getDismountConditions() { return dismountConditions; }

    @Override
    public Optional<SpearCondition> getKnockbackConditions() { return knockbackConditions; }

    @Override
    public Optional<SpearCondition> getDamageConditions() { return damageConditions; }

    @Override
    public float getForwardMovement() { return forwardMovement; }

    @Override
    public float getMinRange() { return minRange; }

    @Override
    public float getMaxRange() { return maxRange; }

    @Override
    public float getHitboxMargin() { return hitboxMargin; }

    @Override
    public float getHitboxMargin2() { return hitboxMargin2; }

    @Override
    public int getContactCooldownTicks() { return contactCooldownTicks; }

    @Override
    public float getSwingTimes() { return swingTimes; }

    // ========== 内置子类 ==========

    public static class WoodenSpearItem extends BaseSpearItem {
        public WoodenSpearItem() { super(SpearStats.WOOD); }
    }
    public static class StoneSpearItem extends BaseSpearItem {
        public StoneSpearItem() { super(SpearStats.STONE); }
    }
    public static class CopperSpearItem extends BaseSpearItem {
        public CopperSpearItem() { super(SpearStats.COPPER); }
    }
    public static class IronSpearItem extends BaseSpearItem {
        public IronSpearItem() { super(SpearStats.IRON); }
    }
    public static class GoldenSpearItem extends BaseSpearItem {
        public GoldenSpearItem() { super(SpearStats.GOLD); }
    }
    public static class DiamondSpearItem extends BaseSpearItem {
        public DiamondSpearItem() { super(SpearStats.DIAMOND); }
    }
    public static class NetheriteSpearItem extends BaseSpearItem {
        public NetheriteSpearItem() { super(SpearStats.NETHERITE); }
    }
}