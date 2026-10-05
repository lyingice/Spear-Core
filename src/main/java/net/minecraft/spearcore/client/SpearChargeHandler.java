package net.minecraft.spearcore.client;

import net.minecraft.client.Minecraft;
import net.minecraft.spearcore.SpearcoreMod;
import net.minecraft.spearcore.client.animation.SpearAnimations;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.TickEvent;

@Mod.EventBusSubscriber(modid = SpearcoreMod.MODID, value = Dist.CLIENT)
public class SpearChargeHandler {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // 命中反馈动画倒计时
        if (SpearAnimations.spearHitTicks > 0) {
            SpearAnimations.spearHitTicks--;
        }
    }
}
