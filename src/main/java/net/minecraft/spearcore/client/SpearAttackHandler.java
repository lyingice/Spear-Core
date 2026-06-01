package net.minecraft.spearcore.client;

import net.minecraft.spearcore.SpearcoreMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.spearcore.item.SpearItem;
import net.minecraft.spearcore.util.SpearCollision;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.List;

@EventBusSubscriber(modid = SpearcoreMod.MODID)
public class SpearAttackHandler {

    @SubscribeEvent
    public static void onPlayerAttack(AttackEntityEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        Player player = event.getEntity();
        ItemStack stack = player.getMainHandItem();

        if (!(stack.getItem() instanceof SpearItem spear)) return;

        // 攻击冷却未满则跳过
        if (player.getAttackStrengthScale(0) < 1.0F) return;

        event.setCanceled(true);
        player.resetAttackStrengthTicker();

        // 播放攻击音效
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                spear.getAttackSound(), SoundSource.PLAYERS, 1.0F, 1.0F);

        float baseDamage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
        boolean hitSomething = false;

        List<EntityHitResult> hits = SpearCollision.getHitEntitiesAlong(
                player, spear, spear.getHitboxMargin2(),
                entity -> entity instanceof LivingEntity
                        && entity.isAlive()
                        && entity != player
                        && canHitEntity(player, entity)
        );

        for (EntityHitResult hit : hits) {
            Entity target = hit.getEntity();
            DamageSource source = player.damageSources().mobAttack(player);
            if (target.hurt(source, baseDamage)) {
                hitSomething = true;
                causeKnockback(player, target, 0.4F);
            }
        }

        if (hitSomething) {
            stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
            Entity lastHit = hits.get(0).getEntity();
            player.setLastHurtMob(lastHit);
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    spear.getHitSound(), SoundSource.PLAYERS, 1.0F, 1.0F);
            // 触发命中反馈动画
            net.minecraft.spearcore.client.animation.SpearAnimations.triggerHitFeedback();
        }
    }

    private static boolean canHitEntity(Player player, Entity target) {
        if (target instanceof Player other && !player.canHarmPlayer(other)) return false;
        if (player.isPassengerOfSameVehicle(target)) return false;
        if (!net.minecraft.spearcore.util.SpearCollision.hasLineOfSight(player, target)) return false;
        return true;
    }

    public static void causeKnockback(Player player, Entity target, float strength) {
        if (strength > 0.0F) {
            if (target instanceof LivingEntity living) {
                living.knockback(strength,
                        Mth.sin(player.getYRot() * ((float) Math.PI / 180)),
                        -Mth.cos(player.getYRot() * ((float) Math.PI / 180)));
            } else {
                target.push(
                        -Mth.sin(player.getYRot() * ((float) Math.PI / 180)) * strength,
                        0.1,
                        Mth.cos(player.getYRot() * ((float) Math.PI / 180)) * strength);
            }
        }
    }
}