package net.minecraft.spearcore.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.spearcore.SpearcoreMod;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 举矛时允许疾跑。
 *
 * <p>原版 {@code LocalPlayer.aiStep} 在"正在使用物品"分支里会把 {@code sprintTriggerTime}
 * 清零，所以举矛时双击 W 的奔跑判定永远读不到值。早期版本靠 {@code @Inject} 到
 * {@code aiStep} 的 TAIL 来补断言，但一旦有别的模组 mixin 同一个方法（尤其
 * {@code @Overwrite}），断言就会失效，表现为"举矛跑不动"。</p>
 *
 * <p>现在改为走客户端 tick 事件：不碰 LocalPlayer、不依赖任何 mixin，
 * 无论别的模组怎么改 aiStep，这里每刻都把疾跑状态重新顶起来。</p>
 */
@Mod.EventBusSubscriber(modid = SpearcoreMod.MODID, value = Dist.CLIENT)
public class SpearSprintHandler {

    /** 原版双击 W 的判定窗口（tick）。 */
    private static final int SPRINT_TAP_WINDOW = 7;

    /** 自己维护的双击 W 计时器，不依赖原版 sprintTriggerTime。 */
    private static int sprintTapTimer;
    private static boolean wasPressingForward;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        // 原版蓄力（举矛）本来就能疾跑，这里只负责按疾跑键 / 双击 W 放行。
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) {
            reset();
            return;
        }

        boolean usingSpear = isUsingSpear(player);
        boolean pressingForward = player.input.forwardImpulse >= 0.8F;

        if (usingSpear) {
            boolean sprintKeyDown = mc.options.keySprint.isDown();
            if (pressingForward && canStartSprinting(player)) {
                if (sprintKeyDown) {
                    player.setSprinting(true);
                    sprintTapTimer = 0;
                } else if (!wasPressingForward) {
                    // 只在"刚按下 W"这一瞬间判定，按住不算
                    if (sprintTapTimer > 0) {
                        player.setSprinting(true);
                        sprintTapTimer = 0;
                    } else {
                        sprintTapTimer = SPRINT_TAP_WINDOW;
                    }
                }
            }
        } else {
            sprintTapTimer = 0;
        }

        if (sprintTapTimer > 0) {
            sprintTapTimer--;
        }
        wasPressingForward = pressingForward;
    }

    /** 是否正在举矛（蓄力）。 */
    private static boolean isUsingSpear(LocalPlayer player) {
        ItemStack use = player.getUseItem();
        return player.isUsingItem() && !use.isEmpty()
                && use.getItem() instanceof net.minecraft.spearcore.item.SpearItem;
    }

    /** 原版允许起跑的条件（饥饿、潜行、骑乘、失明、飞行）。 */
    private static boolean canStartSprinting(LocalPlayer player) {
        return !player.isSprinting()
                && !player.isCrouching()
                && !player.isPassenger()
                && !player.hasEffect(MobEffects.BLINDNESS)
                && (player.getFoodData().getFoodLevel() > 6.0F || player.getAbilities().mayfly);
    }

    private static void reset() {
        sprintTapTimer = 0;
        wasPressingForward = false;
    }
}
