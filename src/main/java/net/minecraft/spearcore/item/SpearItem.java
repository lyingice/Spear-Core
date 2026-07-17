package net.minecraft.spearcore.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.spearcore.init.SpearSounds;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.minecraft.spearcore.init.SpearDamageTypes;
import net.minecraft.spearcore.util.KnownMovementAccessor;
import net.minecraft.spearcore.util.SpearCollision;
import net.minecraft.spearcore.util.SpearCondition;
import net.minecraft.spearcore.util.SpearCooldownAccessor;
import net.minecraft.spearcore.util.UseSpearSpecialEntity;

import java.util.*;
import java.util.function.Predicate;

/**
 * SpearItem — 矛的核心类，严格基于 J库 ItemToolBaseSpearBase 移植。
 */
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
    public abstract float getMinCreativeRange();
    public abstract float getMaxCreativeRange();
    public abstract float getMobFactor();
    public abstract boolean dealsKnockback();
    public abstract boolean dismounts();

    protected int getSpearEnchantmentValue() { return 0; }
    protected boolean canRepair(ItemStack stack, ItemStack repairCandidate) { return false; }

    // ========== getKnownMovement / getMotion（J库 static） ==========

    private static Vec3 getKnownMovement(Entity entity) {
        if (entity instanceof KnownMovementAccessor accessor && entity instanceof ServerPlayer && entity.isAlive()) {
            return accessor.GetKnownMovement();
        }
        LivingEntity rider = entity.getControllingPassenger();
        if (rider instanceof Player && entity.isAlive()) {
            return getKnownMovement(rider);
        }
        return entity.getDeltaMovement();
    }

    public static Vec3 getMotion(Entity entity) {
        if (!(entity instanceof Player)
                && !(entity instanceof UseSpearSpecialEntity u && u.isJerotesSpearGetMotionLikePlayer())
                && entity.isPassenger()) {
            entity = entity.getRootVehicle();
        }
        Vec3 vec3 = getKnownMovement(entity).scale(20.0);
        if (entity.onGround()) {
            return vec3.with(Direction.Axis.Y, 0.0);
        }
        return vec3;
    }

    // ========== effectiveMinRange / effectiveMaxRange（J库） ==========

    public float effectiveMinRange(Entity entity) {
        if (entity instanceof Player player) {
            if (player.isSpectator()) return 0.0f;
            return player.isCreative() ? getMinCreativeRange() : getMinRange();
        }
        return getMinRange() * getMobFactor();
    }

    public float effectiveMaxRange(Entity entity) {
        if (entity instanceof Player player) {
            return (player.isCreative() ? getMaxCreativeRange() : getMaxRange())
                    + (player.getAttribute(Attributes.ENTITY_INTERACTION_RANGE) != null
                    ? (float) Math.max(0, player.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE) - 3) : 0);
        }
        return getMaxRange() * getMobFactor()
                + (entity instanceof LivingEntity le && le.getAttribute(Attributes.ENTITY_INTERACTION_RANGE) != null
                ? (float) Math.max(0, le.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE) - 3) : 0);
    }

    // ========== use（J库） ==========

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
        makeSound(player);
        return InteractionResultHolder.consume(stack);
    }

    private void makeSound(Entity entity) {
        if (entity instanceof Player player) {
            entity.level().playSound(player, entity.getX(), entity.getY(), entity.getZ(),
                    getUseSound(), entity.getSoundSource(), 1.0f, 1.0f);
        } else {
            entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                    getUseSound(), entity.getSoundSource(), 1.0f, 1.0f);
        }
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return false;
    }

    @Override
    public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
        return !player.isCreative();
    }

    public int computeDamageUseDuration() {
        return getDelayTicks() + getDamageConditions().map(SpearCondition::maxDurationTicks).orElse(0);
    }

    // ========== onUseTick — 等价于 J库 damageEntities ==========

    @Override
    public void onUseTick(Level level, LivingEntity user, ItemStack stack, int remainingTicks) {
        if (level.isClientSide) return;

        if (user instanceof Player player && player.getCooldowns().isOnCooldown(this)) {
            player.stopUsingItem();
            return;
        }

        int usedTicks = stack.getUseDuration(user) - remainingTicks;
        if (usedTicks < getDelayTicks()) return;
        int effectiveTicks = usedTicks - getDelayTicks();

        if (usedTicks >= getDamageEndTick()) {
            user.stopUsingItem();
            return;
        }

        Vec3 look = user.getLookAngle();
        double attackerSpeed = look.dot(getMotion(user));

        // J库 needSpeed 逻辑
        float needSpeed = user instanceof Player ? 1.0f : 0.2f;
        if (user instanceof UseSpearSpecialEntity u) {
            needSpeed = u.getJerotesSpearNeedSpeed();
        }

        double baseDamage = user.getAttribute(Attributes.ATTACK_DAMAGE) != null
                ? user.getAttributeBaseValue(Attributes.ATTACK_DAMAGE) : 0.0;

        boolean hitSomething = false;

        for (EntityHitResult hit : getHitEntitiesAlong(user, this, getHitboxMargin(),
                entity -> canHitEntity(user, entity))) {
            Entity target = hit.getEntity();
            if (!(target instanceof LivingEntity)) continue;

            if (user instanceof SpearCooldownAccessor accessor) {
                if (accessor.WasRecentlyStabbed(target, getContactCooldownTicks())) continue;
            }
            if (user instanceof SpearCooldownAccessor accessor) {
                accessor.RememberStabbedEntity(target);
            }

            double targetSpeed = look.dot(getMotion(target));
            double relSpeed = Math.max(0.0, attackerSpeed - targetSpeed);
            if (user instanceof UseSpearSpecialEntity u) {
                relSpeed *= u.getJerotesSpearDamageMultiple();
            }

            boolean canDismount = dismounts() && getDismountConditions().isPresent()
                    && getDismountConditions().get().test(effectiveTicks, attackerSpeed, relSpeed, needSpeed);
            boolean canKnockback = dealsKnockback() && getKnockbackConditions().isPresent()
                    && getKnockbackConditions().get().test(effectiveTicks, attackerSpeed, relSpeed, needSpeed);
            boolean canDamage = getDamageConditions().isPresent()
                    && getDamageConditions().get().test(effectiveTicks, attackerSpeed, relSpeed, needSpeed);

            if (!canDismount && !canKnockback && !canDamage) continue;

            float damage = (float) baseDamage + (float) Mth.floor(relSpeed * (double) getDamageMultiplier());
            hitSomething |= stabAttack(
                    SlotUtil.slotForHand(user, user.getUsedItemHand()),
                    target, damage, canDamage, canKnockback, canDismount, user);
        }

        if (hitSomething) {
            user.level().broadcastEntityEvent(user, (byte) 2);
        }
    }

    // ========== attack（J库） ==========

    public void attack(LivingEntity attacker, EquipmentSlot slot) {
        float baseDamage = attacker.getAttribute(Attributes.ATTACK_DAMAGE) != null
                ? (float) attacker.getAttributeValue(Attributes.ATTACK_DAMAGE) : 0f;
        boolean hitSomething = false;

        for (EntityHitResult hit : getHitEntitiesAlong(attacker, this, getHitboxMargin2(),
                entity -> canHitEntity(attacker, entity))) {
            hitSomething |= stabAttack(slot, hit.getEntity(), baseDamage, true,
                    dealsKnockback(), dismounts(), attacker);
        }

        if (attacker instanceof ServerPlayer serverPlayer) {
            serverPlayer.resetAttackStrengthTicker();
        }
        jerotesLungeForwardMaybe(attacker);

        if (hitSomething) {
            attacker.level().playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(),
                    getHitSound(), SoundSource.PLAYERS, 1.0f, 1.0f);
        }
        attacker.level().playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(),
                getAttackSound(), SoundSource.PLAYERS, 1.0f, 1.0f);
        attacker.swing(InteractionHand.MAIN_HAND, false);
    }

    // ========== performStabAttack（兼容 SpearStabAttackPacket） ==========

    public boolean performStabAttack(LivingEntity attacker, ItemStack stack, EquipmentSlot slot, boolean playSound) {
        if (attacker.level().isClientSide || stack.isEmpty() || stack.getItem() != this) return false;
        if (attacker instanceof Player player && !canPlayerPerformStab(player, this)) return false;

        Vec3 look = attacker.getLookAngle();
        double attackerSpeed = Math.max(0.0, look.dot(getMotion(attacker)));

        float baseDamage = (float) attacker.getAttributeValue(Attributes.ATTACK_DAMAGE);
        float damage = baseDamage + (float) Mth.floor(attackerSpeed * getDamageMultiplier());

        boolean hitSomething = false;
        if (playSound) {
            attacker.level().playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(),
                    getAttackSound(), SoundSource.PLAYERS, 1.0F, 1.0F);
        }

        for (EntityHitResult hit : getHitEntitiesAlong(attacker, this, getHitboxMargin2(),
                entity -> canHitEntity(attacker, entity))) {
            Entity target = hit.getEntity();
            if (attacker instanceof SpearCooldownAccessor accessor
                    && accessor.WasRecentlyStabbed(target, getContactCooldownTicks())) {
                continue;
            }
            if (target.hurt(SpearDamageTypes.spear(attacker.level(), attacker), damage)) {
                hitSomething = true;
                stack.hurtAndBreak(1, attacker, slot);
                if (dealsKnockback()) {
                    causeExtraKnockback(attacker, target, 0.4F, target.getDeltaMovement());
                }
                if (attacker instanceof Player player) {
                    player.setLastHurtMob(target);
                }
                if (attacker instanceof SpearCooldownAccessor accessor) {
                    accessor.RememberStabbedEntity(target);
                }
            }
        }

        if (hitSomething) {
            attacker.level().broadcastEntityEvent(attacker, (byte) 2);
            attacker.level().playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(),
                    getHitSound(), SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        jerotesLungeForwardMaybe(attacker);
        return hitSomething;
    }

    public static boolean canPlayerPerformStab(Player player, SpearItem spear) {
        return player.isAlive() && !player.isSpectator()
                && !player.getCooldowns().isOnCooldown(spear)
                && player.getAttackStrengthScale(0.0F) >= 0.95F;
    }

    // ========== stabAttack（J库核心） ==========

    private boolean stabAttack(EquipmentSlot slot, Entity target, float damage, boolean canDamage,
                               boolean canKnockback, boolean canDismount, LivingEntity attacker) {
        Level level = attacker.level();
        if (!(level instanceof ServerLevel serverLevel)) return false;

        ItemStack stack = attacker.getItemBySlot(slot);
        DamageSource damageSource = SpearDamageTypes.spear(level, attacker);

        float finalDamage = damage;
        Vec3 preMotion = target.getDeltaMovement();
        boolean hitSomething = canKnockback;

        boolean damaged = false;
        if (canDamage) {
            damaged = target.hurt(damageSource, finalDamage);
            hitSomething |= damaged;
        }

        if (canKnockback) {
            causeExtraKnockback(attacker, target, 0.4f, preMotion);
        }

        if (canDismount && target.isPassenger()) {
            hitSomething = true;
            target.stopRiding();
        }

        if (target instanceof LivingEntity livingTarget && attacker instanceof Player player) {
            stack.hurtEnemy(livingTarget, player);
        }

        if (!hitSomething) return false;

        attacker.setLastHurtMob(target);
        stack.hurtAndBreak(1, attacker, slot);
        return true;
    }

    // ========== causeExtraKnockback（J库） ==========

    private static void causeExtraKnockback(LivingEntity attacker, Entity target, float strength, Vec3 preMotion) {
        boolean asPlayer = attacker instanceof Player
                || (attacker instanceof UseSpearSpecialEntity u && u.isJerotesSpearCauseExtraKnockbackPlayer());

        if (asPlayer) {
            if (strength > 0.0f) {
                if (target instanceof LivingEntity living) {
                    living.knockback(strength, Mth.sin(attacker.getYRot() * ((float) Math.PI / 180)),
                            -Mth.cos(attacker.getYRot() * ((float) Math.PI / 180)));
                } else {
                    target.push(-Mth.sin(attacker.getYRot() * ((float) Math.PI / 180)) * strength, 0.1,
                            Mth.cos(attacker.getYRot() * ((float) Math.PI / 180)) * strength);
                }
                attacker.setDeltaMovement(attacker.getDeltaMovement().multiply(0.6, 1.0, 0.6));
                attacker.setSprinting(false);
            }
            if (target instanceof ServerPlayer serverPlayer && target.hurtMarked) {
                serverPlayer.connection.send(
                        new net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket(target));
                target.hurtMarked = false;
                target.setDeltaMovement(preMotion);
            }
        } else {
            if (strength > 0.0f && target instanceof LivingEntity living) {
                living.knockback(strength, Mth.sin(attacker.getYRot() * ((float) Math.PI / 180)),
                        -Mth.cos(attacker.getYRot() * ((float) Math.PI / 180)));
                attacker.setDeltaMovement(attacker.getDeltaMovement().multiply(0.6, 1.0, 0.6));
            }
        }
    }

    // ========== canHitEntity（J库 static） ==========

    public static boolean canHitEntity(Entity attacker, Entity target) {
        if (!target.canBeHitByProjectile()) return false;
        if (!target.isAlive() || target == attacker) return false;
        if (target == attacker.getVehicle()) return false;
        if (attacker.isPassengerOfSameVehicle(target)) return false;
        if (attacker instanceof Player p1 && target instanceof Player p2 && !p1.canHarmPlayer(p2)) return false;
        return hasLineOfSight(attacker, target);
    }

    public static boolean hasLineOfSight(Entity from, Entity to) {
        if (to.level() != from.level()) return false;
        Vec3 fromEye = from.getEyePosition();
        Vec3 toCenter = to.position().add(0, to.getBbHeight() / 2, 0);
        BlockHitResult hit = from.level().clip(
                new ClipContext(fromEye, toCenter, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, from));
        return hit.getType() == HitResult.Type.MISS;
    }

    // ========== getHitEntitiesAlong（J库） ==========

    public static List<EntityHitResult> getHitEntitiesAlong(LivingEntity user, SpearItem spear,
                                                             float hitboxMargin, Predicate<Entity> predicate) {
        Vec3 look = getHeadLookAngle(user);
        Vec3 eyePos = user.getEyePosition();
        Vec3 start = eyePos.add(look.scale(spear.effectiveMinRange(user)));
        double speedBonus = getKnownMovement(user).dot(look);
        Vec3 end = eyePos.add(look.scale((double) spear.effectiveMaxRange(user) + Math.max(0.0, speedBonus)));

        Level level = user.level();
        BlockHitResult blockHit = clipIncludingBorder(
                new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, user), level);
        if (blockHit.getType() != HitResult.Type.MISS) {
            end = blockHit.getLocation();
        }

        AABB searchBox = AABB.ofSize(start, hitboxMargin, hitboxMargin, hitboxMargin)
                .expandTowards(end.subtract(start)).inflate(1.0);

        List<EntityHitResult> results = new ArrayList<>();
        for (Entity target : level.getEntities(user, searchBox, predicate)) {
            AABB targetBox = target.getBoundingBox().inflate(hitboxMargin);
            if (targetBox.contains(start)) {
                results.add(new EntityHitResult(target, start));
                continue;
            }
            Optional<Vec3> hit = targetBox.clip(start, end);
            hit.ifPresent(vec3 -> results.add(new EntityHitResult(target, vec3)));
        }

        results.sort((a, b) -> Double.compare(
                a.getLocation().distanceToSqr(start),
                b.getLocation().distanceToSqr(start)));
        return results;
    }

    private static Vec3 getHeadLookAngle(Entity entity) {
        return calculateViewVector(entity.getXRot(), entity.getYHeadRot());
    }

    private static Vec3 calculateViewVector(float xRot, float yRot) {
        float f = xRot * ((float) Math.PI / 180F);
        float f1 = -yRot * ((float) Math.PI / 180F);
        float f2 = Mth.cos(f1);
        float f3 = Mth.sin(f1);
        float f4 = Mth.cos(f);
        float f5 = Mth.sin(f);
        return new Vec3(f3 * f4, -f5, f2 * f4);
    }

    private static BlockHitResult clipIncludingBorder(ClipContext ctx, Level level) {
        BlockHitResult result = level.clip(ctx);
        WorldBorder border = level.getWorldBorder();
        if (border.isWithinBounds(BlockPos.containing(ctx.getFrom()))
                && !border.isWithinBounds(BlockPos.containing(result.getLocation()))) {
            Vec3 diff = result.getLocation().subtract(ctx.getFrom());
            Direction dir = getApproximateNearest((float) diff.x, (float) diff.y, (float) diff.z);
            Vec3 clamped = new Vec3(
                    Mth.clamp(result.getLocation().x, border.getMinX(), border.getMaxX() - 1.0E-5f),
                    result.getLocation().y,
                    Mth.clamp(result.getLocation().z, border.getMinZ(), border.getMaxZ() - 1.0E-5f));
            return new BlockHitResult(clamped, dir, BlockPos.containing(clamped), false);
        }
        return result;
    }

    private static Direction getApproximateNearest(float x, float y, float z) {
        Direction best = Direction.NORTH;
        float bestVal = Float.MIN_VALUE;
        for (Direction dir : Direction.values()) {
            float val = x * dir.getStepX() + y * dir.getStepY() + z * dir.getStepZ();
            if (val > bestVal) {
                bestVal = val;
                best = dir;
            }
        }
        return best;
    }

    // ========== Lunge ==========

    public static void jerotesLungeForwardMaybe(LivingEntity user) {
        if (!(user.getMainHandItem().getItem() instanceof SpearItem)) {
            return;
        }
        ItemStack stack = user.getMainHandItem();
        int lungeLevel = stack.getEnchantmentLevel(
                user.level().registryAccess()
                        .registryOrThrow(Registries.ENCHANTMENT)
                        .getHolderOrThrow(ResourceKey.create(Registries.ENCHANTMENT,
                                ResourceLocation.fromNamespaceAndPath("spearcore", "lunge")))
        );

        if (lungeLevel <= 0) {
            return;
        }
        if (user instanceof Player player && player.getFoodData().getFoodLevel() <= 7.0f) {
            return;
        }
        if (user.isPassenger() || user.isFallFlying() || user.isInWater()) {
            return;
        }

        if (stack.isDamageableItem() && user instanceof Player) {
            stack.hurtAndBreak(1, user, EquipmentSlot.MAINHAND);
        }
        if (user instanceof Player player) {
            player.causeFoodExhaustion(4.0f * lungeLevel);
        }

        rushAttack(user, 0.65f * lungeLevel);

        SoundEvent sound = SpearSounds.ITEM_SPEAR_LUNGE_1.get();
        if (lungeLevel > 1) {
            sound = SpearSounds.ITEM_SPEAR_LUNGE_2.get();
        }
        if (lungeLevel > 2) {
            sound = SpearSounds.ITEM_SPEAR_LUNGE_3.get();
        }
        user.level().playSound(null, user.getX(), user.getY(), user.getZ(),
                sound, user.getSoundSource(), 1.0F, 1.0F);
    }

    public static boolean rushAttack(LivingEntity user, float strength) {
        Vec3 look = user.getLookAngle();
        user.push(look.x * strength, 0.0D, look.z * strength);
        return true;
    }

    // ========== 附魔 ==========

    @Override
    public boolean isEnchantable(ItemStack stack) { return true; }

    @Override
    public int getEnchantmentValue() { return getSpearEnchantmentValue(); }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repairCandidate) {
        return canRepair(stack, repairCandidate);
    }

    // ========== 工具类 ==========

    public static final class SlotUtil {
        public static EquipmentSlot slotForHand(LivingEntity user, InteractionHand hand) {
            return hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
        }
    }
}
