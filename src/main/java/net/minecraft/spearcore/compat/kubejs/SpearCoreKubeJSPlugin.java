package net.minecraft.spearcore.compat.kubejs;

import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.registry.BuilderTypeRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.spearcore.SpearcoreMod;

/**
 * spearcore 的 KubeJS 插件入口。
 *
 * <p><b>类加载隔离：</b>本类及其所在的 {@code compat.kubejs} 包是项目中唯一引用
 * {@code dev.latvian.mods.kubejs} 的地方。KubeJS 通过自身资源里的
 * {@code kubejs.plugins.txt}（本 mod 的 {@code src/main/resources/kubejs.plugins.txt}）
 * 用字符串 + {@code Class.forName} 反射加载插件，加载失败只会在 KubeJS 日志里报错并跳过。
 * 因此：没装 KubeJS 时本类永远不会被加载，核心代码不受影响。</p>
 *
 * <p>所以本包之外<b>不得</b>出现任何 KubeJS 引用；本类也不得被核心代码直接 new 或静态引用。</p>
 */
public class SpearCoreKubeJSPlugin implements KubeJSPlugin {

    /** 脚本里书写的类型名：{@code event.create('my_spear', 'spearcore:spear')}。 */
    public static final ResourceLocation SPEAR_TYPE =
            ResourceLocation.fromNamespaceAndPath(SpearcoreMod.MODID, "spear");

    @Override
    public void registerBuilderTypes(BuilderTypeRegistry registry) {
        try {
            registry.of(Registries.ITEM, item -> item.add(SPEAR_TYPE, SpearItemBuilder.class, SpearItemBuilder::new));
            SpearcoreMod.LOGGER.info("[spearcore] KubeJS 联动已启用：物品类型 '{}' 可用", SPEAR_TYPE);
        } catch (Throwable t) {
            // KubeJS 只保证插件"类加载"失败不炸；回调里抛异常是否被兜住取决于调用方，所以自己兜。
            SpearcoreMod.LOGGER.error("[spearcore] 注册 KubeJS 物品类型 '{}' 失败，联动功能已跳过", SPEAR_TYPE, t);
        }
    }
}
