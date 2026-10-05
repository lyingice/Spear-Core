package net.minecraft.spearcore.event;

import net.minecraft.spearcore.SpearcoreMod;
import net.minecraft.spearcore.item.SpearItem;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SpearcoreMod.MODID)
public class SpearAttackHandler {
    @SubscribeEvent
    public static void onPlayerAttack(AttackEntityEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        Player player = event.getEntity();
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof SpearItem spear)) return;

        event.setCanceled(true);
        if (!SpearItem.canPlayerPerformStab(player, spear)) return;

        spear.performStabAttack(player, stack, EquipmentSlot.MAINHAND, true);
        player.resetAttackStrengthTicker();
    }
}
