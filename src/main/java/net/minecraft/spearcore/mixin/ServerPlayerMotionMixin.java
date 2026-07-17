package net.minecraft.spearcore.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.spearcore.item.SpearItem;
import net.minecraft.spearcore.util.KnownMovementAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMotionMixin extends Player implements KnownMovementAccessor {

    @SuppressWarnings("deprecation")
    public ServerPlayerMotionMixin() {
        super(null, null, 0.0f, null);
    }

    @Unique
    private Vec3 LastKnownClientMovement = Vec3.ZERO;

    @Override
    @Unique
    public Vec3 GetKnownMovement() {
        Entity vehicle = this.getVehicle();
        if (vehicle != null && vehicle.getControllingPassenger() != (Object) this) {
            return SpearItem.getMotion(vehicle).scale(0.05D);
        }
        return this.LastKnownClientMovement;
    }

    @Override
    @Unique
    public void SetKnownMovement(Vec3 vec3) {
        this.LastKnownClientMovement = vec3;
    }
}
