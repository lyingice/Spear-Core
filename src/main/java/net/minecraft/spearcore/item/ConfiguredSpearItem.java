package net.minecraft.spearcore.item;

import net.minecraft.spearcore.init.SpearStats;

/**
 * 通用（可配置）矛实现。
 *
 * <p>与内建的 {@code WoodenSpearItem} 等不同，它不绑定任何材质：全部行为参数来自构造时
 * 传入的 {@link SpearStats.Stats}。因此它可以被非 Java 的注册路径复用——
 * 目前是 KubeJS 的 {@code spearcore:spear} 物品类型（见
 * {@code net.minecraft.spearcore.compat.kubejs}），将来也可以是数据包/JSON 驱动的矛。</p>
 *
 * <p>本类刻意不引用任何第三方 mod 的类，保证在没有 KubeJS 的环境下也能照常加载。</p>
 */
public class ConfiguredSpearItem extends BaseSpearItem {

    /** 使用 Stats 推导出的默认物品属性（堆叠数 1、耐久、稀有度、防火）。 */
    public ConfiguredSpearItem(SpearStats.Stats stats) {
        super(stats);
    }

    /**
     * 使用外部提供的物品属性。
     *
     * <p>调用方需要自行保证"耐久 > 0"（1.21 的 {@code Item.Properties#durability}
     * 会把堆叠数一并设为 1），否则物品会变成无耐久的可堆叠物，与 Stats 中的耐久不一致。</p>
     */
    public ConfiguredSpearItem(SpearStats.Stats stats, Properties properties) {
        super(stats, properties);
    }
}
