package net.minecraft.spearcore.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.spearcore.SpearcoreMod;
import net.minecraft.spearcore.init.SpearStats;
import net.minecraft.spearcore.util.SpearCondition;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.util.UUID;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraftforge.common.ForgeMod;

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


    private Item getSelfItem() {
        return selfItemSupplier.get();
    }

    // ========== 属性修饰符 ==========

    private static final java.util.UUID SPEAR_RANGE_UUID =
            java.util.UUID.fromString("7c9a1e2b-6f43-4d18-9a05-2b8f1c3d4e56");

    private static Multimap<Attribute, AttributeModifier> buildAttributeModifiers(SpearStats.Stats stats) {
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();

        builder.put(Attributes.ATTACK_DAMAGE,
                new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier",
                        stats.attackDamageBonus(), AttributeModifier.Operation.ADDITION));

        builder.put(Attributes.ATTACK_SPEED,
                new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier",
                        stats.getAttackSpeedModifier(), AttributeModifier.Operation.ADDITION));

        builder.put(ForgeMod.ENTITY_REACH.get(),
                new AttributeModifier(SPEAR_RANGE_UUID, "Spear reach",
                        1.5, AttributeModifier.Operation.ADDITION));

        return builder.build();
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        if (slot != EquipmentSlot.MAINHAND) {
            return super.getDefaultAttributeModifiers(slot);
        }
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

    /**
     * 1.20.1 的耐久扣减发生在 hurtEnemy，而 1.21 搬到了 postHurtEnemy。
     * 矛的 performStabAttack 已经自己扣过一次耐久，这里留空是为了在 1.20.1 上保持与 1.21 完全一致的行为（不双扣）。
     */
    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        return true;
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

    @Override
    public float getMinCreativeRange() { return minCreativeRange; }

    @Override
    public float getMaxCreativeRange() { return maxCreativeRange; }

    @Override
    public float getMobFactor() { return mobFactor; }

    @Override
    public boolean dealsKnockback() { return dealsKnockback; }

    @Override
    public boolean dismounts() { return dismounts; }

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
