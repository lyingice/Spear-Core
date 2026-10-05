package net.minecraft.spearcore.compat.punchy;

import net.minecraftforge.fml.ModList;
import net.minecraft.spearcore.SpearcoreMod;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * 让 spearcore.punchy.mixins.json 只针对"装了 Punchy"的环境生效。
 *
 * <p>关键坑：Mixin 在每个配置 **准备阶段**（启动早期）就会调用 shouldApplyMixin，此时
 * NeoForge 的 ModList 还没有初始化（ModList.get() 返回 null，实测会 NPE），所以不能用
 * ModList 来判断；这里优先走 FML 早期可用的 LoadingModList（反射，避免对 FML 内部模块
 * 产生编译期依赖）。
 *
 * <p>三态设计：能判定就缓存判定结果；完全判定不了时**先放行**——因为
 * spearcore.punchy.mixins.json 是 required=false，目标类缺失时 Mixin 只会警告并跳过，
 * 不会崩溃。
 */
public final class PunchyMixinPlugin implements IMixinConfigPlugin {

    private static Boolean punchyPresent;

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    private static boolean spearcore$loggedActive;

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        boolean present = punchyPresent();
        if (present && !spearcore$loggedActive) {
            spearcore$loggedActive = true;
            SpearcoreMod.LOGGER.info("[spearcore] Punchy 联动已启用：矛蓄力阶段按本模组各材质的时间轴切换");
        }
        return present;
    }

    private static boolean punchyPresent() {
        if (punchyPresent != null) return punchyPresent;

        Boolean known = null;

        // 1) 启动早期：LoadingModList 已经填充，而 ModList 还是 null
        try {
            Class<?> loadingModListClass = Class.forName("net.minecraftforge.fml.loading.LoadingModList");
            Object loadingModList = loadingModListClass.getMethod("get").invoke(null);
            if (loadingModList != null) {
                Object modFile = loadingModListClass
                        .getMethod("getModFileById", String.class)
                        .invoke(loadingModList, "punchy");
                known = modFile != null;
            }
        } catch (Throwable ignored) {
            // API 不匹配/未就绪：留给下一步
        }

        // 2) 常规阶段：ModList
        if (known == null) {
            try {
                ModList modList = ModList.get();
                if (modList != null) {
                    known = modList.isLoaded("punchy");
                }
            } catch (Throwable ignored) {
            }
        }

        if (known != null) {
            punchyPresent = known;
            return known;
        }
        // 3) 判定不了：放行（required=false 兜底），不缓存以便之后重新判定
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
