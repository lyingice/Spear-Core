package net.minecraft.spearcore.client;

import net.minecraft.client.Minecraft;
import net.minecraft.spearcore.SpearcoreMod;
import net.minecraft.spearcore.client.animation.SpearAnimations;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = SpearcoreMod.MODID, value = Dist.CLIENT)
public class SpearChargeHandler {

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // 命中反馈动画倒计时
        if (SpearAnimations.spearHitTicks > 0) {
            SpearAnimations.spearHitTicks--;
        }
    }
}