package net.minecraft.spearcore.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.spearcore.util.KnownMovementAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMotionMixin extends Player implements KnownMovementAccessor {

    @SuppressWarnings("deprecation")
    public ServerPlayerMotionMixin() {
        super(null, null, 0.0f, null);
    }

    @Unique
    private Vec3 LastKnownClientMovement = Vec3.ZERO;
    @Unique
    private Vec3 LastPosition = Vec3.ZERO;
    @Unique
    private boolean FirstTick = true;

    @Inject(method = "tick", at = @At("HEAD"))
    public void onTickHead(CallbackInfo ci) {
        ServerPlayer self = (ServerPlayer) (Object) this;
        if (this.FirstTick) {
            this.LastPosition = self.position();
            this.FirstTick = false;
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    public void onTickTail(CallbackInfo ci) {
        ServerPlayer self = (ServerPlayer) (Object) this;
        // 用位置差计算速度，不受减速影响
        Vec3 currentPos = self.position();
        this.LastKnownClientMovement = currentPos.subtract(this.LastPosition);
        this.LastPosition = currentPos;
    }

    @Override
    @Unique
    public Vec3 GetKnownMovement() {
        return this.LastKnownClientMovement;
    }
    @Override
    @Unique
    public void SetKnownMovement(Vec3 vec3) {
        this.LastKnownClientMovement = vec3;
    }
}