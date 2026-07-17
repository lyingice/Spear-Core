package net.minecraft.spearcore.mixin;

import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.spearcore.util.KnownMovementAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 复刻 J 库 ServerGamePacketListenerImplMixin 的核心用途：
 * 直接从客户端移动包记录玩家真实位移，而不是在 tick 末尾用服务端位置差兜底。
 * 这样长矛速度判定不会被使用物品减速、服务端移动同步时序或骑乘包处理干扰。
 */
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerSpearMixin {

    @Shadow
    public ServerPlayer player;

    @Shadow
    private Entity lastVehicle;

    @Unique
    private double spearcore$movePlayerX;
    @Unique
    private double spearcore$movePlayerY;
    @Unique
    private double spearcore$movePlayerZ;
    @Unique
    private double spearcore$moveVehicleX;
    @Unique
    private double spearcore$moveVehicleY;
    @Unique
    private double spearcore$moveVehicleZ;

    @Inject(method = "handleMovePlayer", at = @At("HEAD"))
    private void spearcore$handleMovePlayerHead(ServerboundMovePlayerPacket packet, CallbackInfo ci) {
        this.spearcore$movePlayerX = this.player.getX();
        this.spearcore$movePlayerY = this.player.getY();
        this.spearcore$movePlayerZ = this.player.getZ();
    }

    @Inject(method = "handleMovePlayer", at = @At("TAIL"))
    private void spearcore$handleMovePlayerTail(ServerboundMovePlayerPacket packet, CallbackInfo ci) {
        this.spearcore$rememberKnownMovement(new Vec3(
                this.player.getX() - this.spearcore$movePlayerX,
                this.player.getY() - this.spearcore$movePlayerY,
                this.player.getZ() - this.spearcore$movePlayerZ
        ));
    }

    @Inject(method = "handleMoveVehicle", at = @At("HEAD"))
    private void spearcore$handleMoveVehicleHead(ServerboundMoveVehiclePacket packet, CallbackInfo ci) {
        Entity vehicle = this.player.getVehicle();
        if (vehicle != null && vehicle != this.player && vehicle.getControllingPassenger() == this.player && vehicle == this.lastVehicle) {
            this.spearcore$moveVehicleX = vehicle.getX();
            this.spearcore$moveVehicleY = vehicle.getY();
            this.spearcore$moveVehicleZ = vehicle.getZ();
        }
    }

    @Inject(method = "handleMoveVehicle", at = @At("TAIL"))
    private void spearcore$handleMoveVehicleTail(ServerboundMoveVehiclePacket packet, CallbackInfo ci) {
        Entity vehicle = this.player.getVehicle();
        if (vehicle != null && vehicle != this.player && vehicle.getControllingPassenger() == this.player && vehicle == this.lastVehicle) {
            this.spearcore$rememberKnownMovement(new Vec3(
                    vehicle.getX() - this.spearcore$moveVehicleX,
                    vehicle.getY() - this.spearcore$moveVehicleY,
                    vehicle.getZ() - this.spearcore$moveVehicleZ
            ));
        }
    }

    @Unique
    private void spearcore$rememberKnownMovement(Vec3 movement) {
        if (movement.lengthSqr() > 1.0E-5D) {
            this.player.resetFallDistance();
        }
        ((KnownMovementAccessor) this.player).SetKnownMovement(movement);
    }
}
