package net.minecraft.spearcore.init;

import net.minecraft.spearcore.SpearcoreMod;
import net.minecraft.spearcore.item.SpearItem;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

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
