package net.minecraft.spearcore.compat.kubejs;

import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.generator.KubeAssetGenerator;
import dev.latvian.mods.kubejs.item.ItemBuilder;
import dev.latvian.mods.kubejs.util.ID;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.spearcore.SpearcoreMod;
import net.minecraft.spearcore.init.SpearStats;
import net.minecraft.spearcore.item.ConfiguredSpearItem;
import net.minecraft.spearcore.util.SpearCondition;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * KubeJS 侧 {@code spearcore:spear} 物品类型的构建器。
 *
 * <p>脚本用法：{@code event.create('my_spear', 'spearcore:spear')}，随后链式调用下面的方法。
 * 产出的物品是真正的 {@link net.minecraft.spearcore.item.SpearItem} 子类
 * （{@link ConfiguredSpearItem}），因此突刺、冲锋、动画、怪物 AI、附魔全部沿用内建矛的逻辑。</p>
 *
 * <p><b>默认值</b>全部取自内建铁矛 {@link SpearStats#IRON}，脚本只覆盖关心的项即可，
 * 不会因为内建数值调整而与核心漂移。</p>
 *
 * <p><b>求值时机</b>：KubeJS 是在物品 {@code RegisterEvent} 里才调用
 * {@code factory.createBuilder(id)} 与 {@code createObject()}，此时音效注册表已就绪，
 * 所以这里读取 {@link SpearStats} 是安全的。若将来有人把本类改成在更早的时机构造，
 * 需要重新确认这一点。</p>
 */
public class SpearItemBuilder extends ItemBuilder {

    /** 手持模型的共享基座（含第一/第三人称 display 变换），贴图由每个物品自己提供。 */
    public static final ResourceLocation IN_HAND_BASE_MODEL =
            ResourceLocation.fromNamespaceAndPath(SpearcoreMod.MODID, "item/spear_in_hand_base");

    private static final ResourceLocation GENERATED_MODEL =
            ResourceLocation.withDefaultNamespace("item/generated");

    /** 新矛默认并入内建矛的标签，使附魔/标签判定与内建矛一致。 */
    private static final ResourceLocation[] DEFAULT_TAGS = {
            ResourceLocation.withDefaultNamespace("spears"),
            ResourceLocation.fromNamespaceAndPath(SpearcoreMod.MODID, "spears"),
    };

    // ===== 矛参数，默认 = 铁矛 =====
    private int durability = SpearStats.IRON.durability();
    private float attackDuration = SpearStats.IRON.attackDuration();
    private float damageMultiplier = SpearStats.IRON.damageMultiplier();
    private float attackDamageBonus = SpearStats.IRON.attackDamageBonus();
    private int enchantmentValue = SpearStats.IRON.enchantmentValue();
    private Ingredient repairIngredient = SpearStats.IRON.repairIngredient();
    private String materialName = "custom";
    private SoundEvent useSound = SpearStats.IRON.useSound();
    private SoundEvent hitSound = SpearStats.IRON.hitSound();
    private SoundEvent attackSound = SpearStats.IRON.attackSound();
    private float swingTimes = SpearStats.IRON.swingTimes();
    private float hitboxMargin = SpearStats.IRON.hitboxMargin();
    private int contactCooldownTicks = SpearStats.IRON.contactCooldownTicks();
    private int delayTicks = SpearStats.IRON.delayTicks();
    private Optional<SpearCondition> dismountConditions = SpearStats.IRON.dismountConditions();
    private Optional<SpearCondition> knockbackConditions = SpearStats.IRON.knockbackConditions();
    private Optional<SpearCondition> damageConditions = SpearStats.IRON.damageConditions();
    private float forwardMovement = SpearStats.IRON.forwardMovement();
    private float minRange = SpearStats.IRON.minRange();
    private float maxRange = SpearStats.IRON.maxRange();
    private float minCreativeRange = SpearStats.IRON.minCreativeRange();
    private float maxCreativeRange = SpearStats.IRON.maxCreativeRange();
    private float hitboxMargin2 = SpearStats.IRON.hitboxMargin2();
    private float mobFactor = SpearStats.IRON.mobFactor();
    private boolean dealsKnockback = SpearStats.IRON.dealsKnockback();
    private boolean dismounts = SpearStats.IRON.dismounts();

    /** 为 null 时按约定使用 {@code <图标贴图>_in_hand}。 */
    private String inHandTexture;

    /**
     * 平面图标贴图，自己记一份。
     * <p>
     * KubeJS 2101 的 {@code texture(String)} 会写它的 {@code baseTexture} 字段，
     * 本类原来直接读那个字段；自己再存一份，免得以后 KubeJS 改内部实现又静默失效。
     */
    private String flatTexture;

    public SpearItemBuilder(ResourceLocation id) {
        super(id);
        this.rarity = SpearStats.IRON.rarity();
        this.fireResistant = SpearStats.IRON.fireResistant();
        tag(DEFAULT_TAGS);
    }

    // ===== 物品属性 =====

    /** 耐久。默认 250（铁矛）。 */
    public SpearItemBuilder durability(int v) {
        this.durability = Math.max(1, v);
        return this;
    }

    /** 攻击冷却，单位<b>秒</b>；数值越小攻速越快（攻击速度 = 1 / 该值）。默认 0.95。 */
    public SpearItemBuilder attackDuration(float v) {
        this.attackDuration = v;
        return this;
    }

    /** 冲锋伤害系数：伤害 = 基础伤害 + floor(相对速度 × 该值)。默认 0.95。 */
    public SpearItemBuilder damageMultiplier(float v) {
        this.damageMultiplier = v;
        return this;
    }

    /** 固定攻击力加成（加在 attack_damage 属性上）。默认 2。 */
    public SpearItemBuilder attackDamageBonus(float v) {
        this.attackDamageBonus = v;
        return this;
    }

    /** 附魔能力。默认 14。 */
    public SpearItemBuilder enchantmentValue(int v) {
        this.enchantmentValue = v;
        return this;
    }

    /** 修复材料（进阶用法，直接给 {@code Ingredient}）。默认铁锭。 */
    public SpearItemBuilder repairIngredient(Ingredient v) {
        this.repairIngredient = v;
        return this;
    }

    /**
     * 修复材料的简易写法，直接写物品 id，可写多个：
     * {@code .repairItem('minecraft:copper_ingot', 'mypack:bronze_ingot')}。
     */
    public SpearItemBuilder repairItem(String... itemIds) {
        var stacks = new ItemStack[itemIds.length];
        for (int i = 0; i < itemIds.length; i++) {
            var location = ResourceLocation.tryParse(itemIds[i]);
            var item = location == null ? null : BuiltInRegistries.ITEM.get(location);
            if (item == null || item == net.minecraft.world.item.Items.AIR) {
                SpearcoreMod.LOGGER.warn("[spearcore] 矛的修复材料 '{}' 不存在，已忽略", itemIds[i]);
                continue;
            }
            stacks[i] = new ItemStack(item);
        }
        this.repairIngredient = Ingredient.of(Arrays.stream(stacks).filter(Objects::nonNull).toArray(ItemStack[]::new));
        return this;
    }

    /**
     * 稀有度。KubeJS 不会把字符串自动转成 {@code Rarity}，所以这里提供一个字符串重载：
     * {@code 'common'} / {@code 'uncommon'} / {@code 'rare'} / {@code 'epic'}，默认 common。
     */
    public SpearItemBuilder rarity(String name) {
        this.rarity = switch (name == null ? "" : name.toLowerCase(Locale.ROOT)) {
            case "uncommon" -> Rarity.UNCOMMON;
            case "rare" -> Rarity.RARE;
            case "epic" -> Rarity.EPIC;
            default -> Rarity.COMMON;
        };
        return this;
    }

    /** 材质名，仅作标记/调试用。默认 {@code custom}。 */
    public SpearItemBuilder materialName(String v) {
        this.materialName = v;
        return this;
    }

    /** 使用（起手）、命中、挥击三个音效。默认与铁矛一致。 */
    public SpearItemBuilder sounds(SoundEvent use, SoundEvent hit, SoundEvent attack) {
        this.useSound = use;
        this.hitSound = hit;
        this.attackSound = attack;
        return this;
    }

    @Override
    public SpearItemBuilder fireResistant(boolean v) {
        super.fireResistant(v);
        return this;
    }

    @Override
    public SpearItemBuilder texture(String tex) {
        this.flatTexture = tex;
        super.texture(tex);
        return this;
    }

    /**
     * 单独指定手持贴图。默认取 {@code <图标贴图>_in_hand}——
     * 例如 {@code .texture('mypack:item/bronze_spear')} 会自动找
     * {@code mypack:item/bronze_spear_in_hand}。
     */
    public SpearItemBuilder inHandTexture(String tex) {
        this.inHandTexture = tex;
        return this;
    }

    // ===== 蓄力阶段 =====

    /**
     * 四个阶段的时长，单位<b>秒</b>：起手延迟 / 可击落骑手窗口 / 可击退窗口 / 可造成伤害窗口。
     * 默认 0.6 / 2.5 / 6.75 / 11.25（铁矛）。
     */
    public SpearItemBuilder phases(float delaySec, float dismountSec, float knockbackSec, float damageSec) {
        this.delayTicks = (int) (delaySec * 20.0F);
        this.dismountConditions = SpearCondition.ofAttackerSpeed((int) (dismountSec * 20.0F), 0.3F);
        this.knockbackConditions = SpearCondition.ofAttackerSpeed((int) (knockbackSec * 20.0F), 5.1F);
        this.damageConditions = SpearCondition.ofRelativeSpeed((int) (damageSec * 20.0F), 4.6F);
        return this;
    }

    // ===== 命中与手感 =====

    /** 攻击距离：生存最小/最大、创造最小/最大。默认 2 / 4.5 / 2 / 6.5。 */
    public SpearItemBuilder reach(float min, float max, float minCreative, float maxCreative) {
        this.minRange = min;
        this.maxRange = max;
        this.minCreativeRange = minCreative;
        this.maxCreativeRange = maxCreative;
        return this;
    }

    /** 判定箱外扩半径：常规 / 第二阶段。默认 0.25 / 0.125。 */
    public SpearItemBuilder hitbox(float margin, float margin2) {
        this.hitboxMargin = margin;
        this.hitboxMargin2 = margin2;
        return this;
    }

    /** 同一个目标两次突刺之间的免疫时间（刻）。默认 10。 */
    public SpearItemBuilder contactCooldown(int ticks) {
        this.contactCooldownTicks = ticks;
        return this;
    }

    /** 挥击间隔，单位<b>秒</b>（怪物 AI 与动画使用）。默认 0.95。 */
    public SpearItemBuilder swing(float seconds) {
        this.swingTimes = seconds;
        return this;
    }

    /** 冲锋时向前推进的距离系数。默认 0.38。 */
    public SpearItemBuilder forwardMovement(float v) {
        this.forwardMovement = v;
        return this;
    }

    /** 非玩家实体（怪物）的攻击距离倍率。默认 0.5。 */
    public SpearItemBuilder mobFactor(float v) {
        this.mobFactor = v;
        return this;
    }

    /** 是否会把目标击退（还需满足击退窗口条件）。默认 true。 */
    public SpearItemBuilder knockback(boolean v) {
        this.dealsKnockback = v;
        return this;
    }

    /** 是否会把骑手从坐骑上击落（还需满足击落窗口条件）。默认 false。 */
    public SpearItemBuilder dismount(boolean v) {
        this.dismounts = v;
        return this;
    }

    // ===== 注册 =====

    @Override
    public Item createObject() {
        // KubeJS 的 Properties 只在 maxDamage > 0 时调用 durability()，
        // 而 1.21 的 durability() 会同时把堆叠数设为 1 —— 正是矛需要的。
        this.maxDamage = Math.max(1, this.durability);

        var stats = SpearStats.Stats.of(
                this.durability,
                this.attackDuration,
                this.damageMultiplier,
                this.attackDamageBonus,
                this.enchantmentValue,
                this.rarity,
                this.repairIngredient,
                this.fireResistant,
                this.materialName,
                this.useSound,
                this.hitSound,
                this.attackSound,
                this.swingTimes,
                this.hitboxMargin,
                this.contactCooldownTicks,
                this.delayTicks,
                this.dismountConditions,
                this.knockbackConditions,
                this.damageConditions,
                this.forwardMovement,
                this.minRange,
                this.maxRange,
                this.minCreativeRange,
                this.maxCreativeRange,
                this.hitboxMargin2,
                this.mobFactor,
                this.dealsKnockback,
                this.dismounts);

        return new ConfiguredSpearItem(stats, createItemProperties());
    }

    @Override
    protected void generateItemModels(KubeAssetGenerator generator) {
        String flat = this.flatTexture;
        if (flat == null || flat.isEmpty()) {
            flat = baseTexture; // KubeJS 自己那份（textureJson 也可能被 .texture(k, v) 写过）
        }
        if (flat == null || flat.isEmpty()) {
            flat = ResourceLocation.fromNamespaceAndPath(SpearcoreMod.MODID, "item/iron_spear").toString();
        }
        String inHand = (inHandTexture == null || inHandTexture.isEmpty()) ? flat + "_in_hand" : inHandTexture;

        // 矛不能直接用 item/generated：那样拿在手里会被平铺、从中间劈开。
        // 必须用 neoforge:separate_transforms —— 手持用带 display 变换的基座模型，GUI/掉落物用平面图标。
        var model = new JsonObject();
        model.addProperty("loader", "neoforge:separate_transforms");
        model.addProperty("gui_light", "front");

        var base = new JsonObject();
        base.addProperty("parent", IN_HAND_BASE_MODEL.toString());
        var baseTextures = new JsonObject();
        baseTextures.addProperty("layer0", inHand);
        base.add("textures", baseTextures);
        model.add("base", base);

        var perspectives = new JsonObject();
        for (String perspective : new String[]{"gui", "fixed", "ground"}) {
            var entry = new JsonObject();
            entry.addProperty("parent", GENERATED_MODEL.toString());
            var textures = new JsonObject();
            textures.addProperty("layer0", flat);
            entry.add("textures", textures);
            perspectives.add(perspective, entry);
        }
        model.add("perspectives", perspectives);

        generator.json(id.withPath(ID.ITEM_MODEL), model);
    }
}
