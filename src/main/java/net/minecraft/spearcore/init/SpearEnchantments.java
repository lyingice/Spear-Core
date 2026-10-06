package net.minecraft.spearcore.init;

import net.minecraft.spearcore.SpearcoreMod;
import net.minecraft.spearcore.item.SpearItem;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Set;

/**
 * 突进附魔。
 *
 * <p>1.21 的附魔是数据包 JSON（data/&lt;ns&gt;/enchantment/*.json），而 1.20.1 的附魔是
 * <b>注册表对象</b>，必须在 Java 里注册，所以这里用 DeferredRegister + Enchantment 子类实现。</p>
 */
public class SpearEnchantments {

    /** 只接受矛的附魔类别；原版 WEAPON 类别只认 SwordItem，接不住矛。 */
    public static final EnchantmentCategory SPEAR_CATEGORY =
            EnchantmentCategory.create("spearcore:spear", item -> item instanceof SpearItem);

    /**
     * 矛可以正常附上的原版附魔。
     *
     * <p><b>1.21 是数据驱动的</b>：由 {@code data/minecraft/tags/item/enchantable/*.json}
     * 与覆写的 {@code data/minecraft/enchantment/*.json} 决定，矛被加进了这些标签：
     * <ul>
     *   <li>{@code enchantable/sharp_weapon} → 锋利、击退</li>
     *   <li>{@code enchantable/weapon} → 亡灵杀手、节肢杀手</li>
     *   <li>{@code enchantable/fire_aspect} → 火焰附加</li>
     *   <li>{@code enchantable/looting} → 抢夺</li>
     *   <li>{@code enchantable/durability} → 耐久、经验修补</li>
     * </ul>
     *
     * <p><b>1.20.1 是硬编码的</b>：适用范围写死在 {@link EnchantmentCategory#canEnchant} 里
     * （原版 WEAPON 只认 {@code SwordItem}），所以这里照上面那套标签逐条复刻，
     * 由 {@code SpearItem#canApplyAtEnchantingTable} 使用 —— 这样两个版本玩家能上的附魔一致。</p>
     */
    private static final Set<Enchantment> SUPPORTED_VANILLA = Set.of(
            Enchantments.SHARPNESS,          // 锋利          <- enchantable/sharp_weapon
            Enchantments.KNOCKBACK,          // 击退          <- enchantable/sharp_weapon
            Enchantments.SMITE,              // 亡灵杀手      <- enchantable/weapon
            Enchantments.BANE_OF_ARTHROPODS, // 节肢杀手      <- enchantable/weapon
            Enchantments.FIRE_ASPECT,        // 火焰附加      <- enchantable/fire_aspect
            Enchantments.MOB_LOOTING,        // 抢夺          <- enchantable/looting
            Enchantments.UNBREAKING,         // 耐久          <- enchantable/durability
            Enchantments.MENDING);           // 经验修补      <- enchantable/durability

    /** 该附魔能否附在矛上（原版那套白名单；本模组自己的附魔走 SPEAR_CATEGORY）。 */
    public static boolean supportsVanilla(Enchantment enchantment) {
        return SUPPORTED_VANILLA.contains(enchantment);
    }

    public static final DeferredRegister<Enchantment> REGISTRY =
            DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, SpearcoreMod.MODID);

    public static final RegistryObject<Enchantment> LUNGE = REGISTRY.register("lunge", LungeEnchantment::new);

    public static class LungeEnchantment extends Enchantment {
        public LungeEnchantment() {
            super(Rarity.RARE, SPEAR_CATEGORY, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
        }

        @Override
        public int getMaxLevel() {
            return 3;
        }

        @Override
        public int getMinCost(int level) {
            return 5 + (level - 1) * 8;
        }

        @Override
        public int getMaxCost(int level) {
            return getMinCost(level) + 50;
        }
    }
}
