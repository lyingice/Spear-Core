package net.minecraft.spearcore.item;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.spearcore.util.KnownMovementAccessor;
import net.minecraft.spearcore.util.SpearCollision;
import net.minecraft.spearcore.util.SpearCondition;
import net.minecraft.spearcore.util.SpearCooldownAccessor;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import java.util.List;
import java.util.Optional;


public abstract class SpearItem extends Item {

    public SpearItem(Properties properties) {
        super(properties);
    }

    // ========== 基础属性 ==========
    public abstract float getAttackDuration();
    public abstract float getDamageMultiplier();
    public abstract SoundEvent getUseSound();
    public abstract SoundEvent getHitSound();
    public abstract SoundEvent getAttackSound();

    // ========== 蓄力阶段相关 ==========
    public abstract int getDelayTicks();
    public abstract int getDismountEndTick();
    public abstract int getKnockbackEndTick();
    public abstract int getDamageEndTick();
    public abstract Optional<SpearCondition> getDismountConditions();
    public abstract Optional<SpearCondition> getKnockbackConditions();
    public abstract Optional<SpearCondition> getDamageConditions();
    public abstract float getForwardMovement();
    public abstract float getMinRange();
    public abstract float getMaxRange();
    public abstract float getHitboxMargin();
    public abstract float getHitboxMargin2();
    public abstract int getContactCooldownTicks();
    public abstract float getSwingTimes();

    protected int getSpearEnchantmentValue() { return 0; }
    protected boolean canRepair(ItemStack stack, ItemStack repairCandidate) { return false; }

    // ========== 静态工具 ==========

    public static Vec3 getMotion(Entity entity) {
        // 骑乘时用坐骑的 MOVEMENT_SPEED 属性
        if (entity.isPassenger() && entity.getVehicle() != null) {
            Entity vehicle = entity.getVehicle();
            Vec3 motion = vehicle.position().subtract(vehicle.xo, vehicle.yo, vehicle.zo);
            if (motion.lengthSqr() > 0.0001) {
                Vec3 vec3 = motion.scale(20.0);
                if (vehicle.onGround()) {
                    return vec3.with(net.minecraft.core.Direction.Axis.Y, 0.0);
                }
                return vec3;
            }
        }

        // 玩家用已知移动
        if (entity instanceof ServerPlayer serverPlayer && entity.isAlive()) {
            Vec3 known = ((KnownMovementAccessor) serverPlayer).GetKnownMovement();
            if (known.lengthSqr() > 0.001) {
                return known.scale(20.0);
            }
        }

        // fallback
        Vec3 motion = entity.position().subtract(entity.xo, entity.yo, entity.zo);
        if (motion.lengthSqr() < 0.0001) {
            motion = entity.getDeltaMovement();
        }

        Vec3 vec3 = motion.scale(20.0);
        if (entity.onGround()) {
            return vec3.with(net.minecraft.core.Direction.Axis.Y, 0.0);
        }
        return vec3;
    }

    // ========== 物品行为 ==========

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        player.startUsingItem(hand);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                getUseSound(), SoundSource.PLAYERS, 1.0F, 1.0F);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity user, int remainingTicks) {
    }
    // ========== 生物左键戳刺（带蓄力伤害乘算） ==========

    public void attack(LivingEntity attacker, EquipmentSlot slot) {
        if (attacker.level().isClientSide) return;

        // 计算速度加成伤害（与蓄力攻击保持一致）
        Vec3 look = attacker.getLookAngle();
        double attackerSpeed = Math.max(0.0, look.dot(SpearItem.getMotion(attacker)));
        double relSpeed = attackerSpeed; // 左键没有目标，只考虑自身速度

        float baseDamage = (float) attacker.getAttributeValue(Attributes.ATTACK_DAMAGE);
        // 应用 damageMultiplier 和速度加成，与蓄力逻辑一致
        float damage = baseDamage + (float) Mth.floor(relSpeed * getDamageMultiplier());

        boolean hitSomething = false;

        List<EntityHitResult> hits = SpearCollision.getHitEntitiesAlong(
                attacker, this, getHitboxMargin2(),
                entity -> entity instanceof LivingEntity
                        && entity.isAlive()
                        && entity != attacker
        );

        for (EntityHitResult hit : hits) {
            Entity target = hit.getEntity();
            if (target.hurt(attacker.damageSources().mobAttack(attacker), damage)) {
                hitSomething = true;
                causeKnockbackToTarget(attacker, target, 0.4F);
            }
        }

        if (hitSomething) {
            attacker.level().playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(),
                    getHitSound(), SoundSource.PLAYERS, 1.0F, 1.0F);
        }
    }

    // ========== 蓄力 tick（支持生物 + 玩家） ==========

    @Override
    public void onUseTick(Level level, LivingEntity user, ItemStack stack, int remainingTicks) {

        if (level.isClientSide) return;

        // 玩家独有：检查冷却
        if (user instanceof Player player) {
            if (player.getCooldowns().isOnCooldown(this)) {
                player.stopUsingItem();
                return;
            }
        }

        int usedTicks = stack.getUseDuration(user) - remainingTicks;

        if (usedTicks < getDelayTicks()) return;

        int effectiveTicks = usedTicks - getDelayTicks();
        Vec3 look = user.getLookAngle();

        // 计算攻击者速度（对所有 LivingEntity 通用）
        double attackerSpeed = Math.max(0.0, look.dot(SpearItem.getMotion(user)));

        // needSpeed 统一为 1.0（SpearCondition 预设值以此为基础）
        double needSpeed = 1.0F;

        // 脱离阶段
        if (usedTicks >= getDamageEndTick()) {
            user.stopUsingItem();
            return;
        }

        boolean hitSomething = false;

        List<EntityHitResult> hits = SpearCollision.getHitEntitiesAlong(
                user, this, getHitboxMargin(),
                entity -> entity instanceof LivingEntity
                        && entity.isAlive()
                        && entity != user
                        && entity != user.getVehicle()  // 排除坐骑
        );

        for (EntityHitResult hit : hits) {
            Entity target = hit.getEntity();
            if (!(target instanceof LivingEntity)) continue;

            // LivingEntityMixin 让所有 LivingEntity 都实现了 SpearCooldownAccessor
            if (user instanceof SpearCooldownAccessor accessor) {
                if (accessor.WasRecentlyStabbed(target, getContactCooldownTicks())) {
                    continue;
                }
            }

            double targetSpeed = look.dot(SpearItem.getMotion(target));
            double relSpeed = Math.max(0.0, attackerSpeed - targetSpeed);

            // 用 Condition.test() 判断
            boolean canDismount = getDismountConditions().isPresent()
                    && getDismountConditions().get().test(effectiveTicks, attackerSpeed, relSpeed, needSpeed);
            boolean canKnockback = getKnockbackConditions().isPresent()
                    && getKnockbackConditions().get().test(effectiveTicks, attackerSpeed, relSpeed, needSpeed);
            boolean canDamage = getDamageConditions().isPresent()
                    && getDamageConditions().get().test(effectiveTicks, attackerSpeed, relSpeed, needSpeed);

            if (canDismount || canKnockback || canDamage) {
                float baseDamage = (float) user.getAttributeValue(Attributes.ATTACK_DAMAGE);
                float damage = baseDamage + (float) Mth.floor(relSpeed * getDamageMultiplier());

                if (canDamage && target.hurt(user.damageSources().mobAttack(user), damage)) {
                    hitSomething = true;
                    stack.hurtAndBreak(1, user, EquipmentSlot.MAINHAND);
                    if (user instanceof Player player) {
                        player.setLastHurtMob(target);
                    }
                }
                if (canKnockback || (canDamage && hitSomething)) {
                    causeKnockbackToTarget(user, target, 0.4F);
                }
                if (canDismount && target.isPassenger()) {
                    target.stopRiding();
                    hitSomething = true;
                }

                if (user instanceof SpearCooldownAccessor accessor) {
                    accessor.RememberStabbedEntity(target);
                }
            }
        }

        if (hitSomething) {
            user.level().broadcastEntityEvent(user, (byte) 2);
            user.level().playSound(null, user.getX(), user.getY(), user.getZ(),
                    getHitSound(), SoundSource.PLAYERS, 1.0F, 1.0F);
        }
    }

    // ========== 通用击退工具（攻击者 + 目标） ==========

    private static void causeKnockbackToTarget(LivingEntity attacker, Entity target, float strength) {
        if (strength <= 0.0F) return;
        float yRotRad = attacker.getYRot() * ((float) Math.PI / 180);
        if (target instanceof LivingEntity living) {
            living.knockback(strength, Mth.sin(yRotRad), -Mth.cos(yRotRad));
        } else {
            target.push(-Mth.sin(yRotRad) * strength, 0.1, Mth.cos(yRotRad) * strength);
        }
        attacker.setDeltaMovement(attacker.getDeltaMovement().multiply(0.6, 1.0, 0.6));
        attacker.hurtMarked = false;
    }
    // ========== 重写原版行为 ==========

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return false;
    }

    @Override
    public boolean canAttackBlock(net.minecraft.world.level.block.state.BlockState state, Level level,
                                  net.minecraft.core.BlockPos pos, Player player) {
        return !player.isCreative();
    }
    // ========== 附魔/修复 ==========
    @Override
    public boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
        if (enchantment.is(Enchantments.SWEEPING_EDGE)) return false;
        return super.supportsEnchantment(stack, enchantment);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public int getEnchantmentValue() {
        return getSpearEnchantmentValue();
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repairCandidate) {
        return canRepair(stack, repairCandidate);
    }
}