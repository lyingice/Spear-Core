package net.minecraft.spearcore.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.spearcore.item.SpearItem;
import net.minecraft.spearcore.network.SpearStabAttackPacket;
import net.minecraft.spearcore.util.SpearRules;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class SpearCooldownAttackMixin {

    @Shadow
    public HitResult hitResult;

    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void spearcore$startSpearAttack(CallbackInfoReturnable<Boolean> cir) {
        Minecraft self = (Minecraft)(Object)this;
        LocalPlayer player = self.player;
        if (player == null) return;

        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof SpearItem spear)) return;

        boolean ready = SpearRules.usesUseCooldown()
                ? !player.getCooldowns().isOnCooldown(spear)
                : player.getAttackStrengthScale(0.0F) >= 0.95F;

        if (!ready) {
            // 原版 Java：攻击蓄力不满时点左键不出手。
            // 基岩版走物品使用冷却，交给服务端 canPlayerPerformStab 判定，这里不拦。
            if (!SpearRules.usesUseCooldown()) {
                cir.setReturnValue(false);
                cir.cancel();
            }
            return;
        }

        // 准星锁到实体时由服务端 AttackEntityEvent 接手，这里只补「MISS / 命中方块」两种情况：
        // 玩家交互距离是 3.0，长矛能戳到 4.5，3~4.5 格的目标在原版拾取里会变成 MISS 或命中它身后的方块。
        if (this.hitResult == null || this.hitResult.getType() != HitResult.Type.ENTITY) {
            SpearStabAttackPacket.CHANNEL.sendToServer(new SpearStabAttackPacket());
            SpearItem.jerotesLungeForwardMaybe(player);
        }
    }
}
