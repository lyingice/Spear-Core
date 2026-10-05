package net.minecraft.spearcore.client;

import net.minecraft.spearcore.SpearcoreMod;
import net.minecraft.spearcore.item.SpearItem;
import net.minecraft.spearcore.network.SpearStabAttackPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;


@Mod.EventBusSubscriber(modid = SpearcoreMod.MODID, value = Dist.CLIENT)
public class SpearClientStabHandler {
    @SubscribeEvent
    public static void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
        Player player = event.getEntity();
        ItemStack stack = event.getEntity().getMainHandItem();
        if (!(stack.getItem() instanceof SpearItem)) {
            return;
        }
        SpearStabAttackPacket.CHANNEL.sendToServer(new SpearStabAttackPacket());
        SpearItem.jerotesLungeForwardMaybe(player);
        //player.resetAttackStrengthTicker();
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide()) {
            return;
        }
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof SpearItem)) {
            return;
        }
        SpearItem.jerotesLungeForwardMaybe(player);
        //player.resetAttackStrengthTicker();
    }
}
