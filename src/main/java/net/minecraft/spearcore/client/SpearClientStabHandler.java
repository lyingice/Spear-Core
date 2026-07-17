package net.minecraft.spearcore.client;

import net.minecraft.spearcore.SpearcoreMod;
import net.minecraft.spearcore.item.SpearItem;
import net.minecraft.spearcore.network.SpearStabAttackPacket;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = SpearcoreMod.MODID, value = Dist.CLIENT)
public class SpearClientStabHandler {
    @SubscribeEvent
    public static void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
        ItemStack stack = event.getEntity().getMainHandItem();
        if (stack.getItem() instanceof SpearItem) {
            PacketDistributor.sendToServer(new SpearStabAttackPacket());
        }
    }
}
