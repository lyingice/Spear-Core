package net.minecraft.spearcore.client;

import net.minecraft.spearcore.SpearcoreMod;
import net.minecraft.spearcore.item.SpearItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.entity.player.AttackEntityEvent;


@Mod.EventBusSubscriber(modid = SpearcoreMod.MODID, value = Dist.CLIENT)
public class SpearClientStabHandler {
    // 左键戳刺的发包已挪到 SpearCooldownAttackMixin（startAttack），因为玩家交互距离只有 3.0，
    // 3~4.5 格的目标在原版拾取里可能被判定成 MISS 或命中方块，LeftClickEmpty 会漏发。
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
    }
}
