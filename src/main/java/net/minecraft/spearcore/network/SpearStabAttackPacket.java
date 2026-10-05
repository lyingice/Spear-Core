package net.minecraft.spearcore.network;

import net.minecraft.core.SectionPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.spearcore.SpearcoreMod;
import net.minecraft.spearcore.item.SpearItem;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Supplier;

/**
 * 客户端左键戳刺 -> 服务端。
 *
 * <p>1.21/NeoForge 用 CustomPacketPayload + StreamCodec + RegisterPayloadHandlersEvent，
 * 1.20.1 的 Forge 用 SimpleImpl 的 SimpleChannel，所以整个类重写。</p>
 */
public class SpearStabAttackPacket {

    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(SpearcoreMod.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);

    public static void register() {
        CHANNEL.registerMessage(0, SpearStabAttackPacket.class,
                SpearStabAttackPacket::encode,
                SpearStabAttackPacket::decode,
                SpearStabAttackPacket::handle);
    }

    public SpearStabAttackPacket() {
    }

    public static void encode(SpearStabAttackPacket packet, FriendlyByteBuf buffer) {
        // 无载荷
    }

    public static SpearStabAttackPacket decode(FriendlyByteBuf buffer) {
        return new SpearStabAttackPacket();
    }

    public static void handle(SpearStabAttackPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;

            if (!player.level().getChunkSource().hasChunk(
                    SectionPos.blockToSectionCoord(player.getX()),
                    SectionPos.blockToSectionCoord(player.getZ()))) {
                return;
            }
            ItemStack stack = player.getMainHandItem();
            if (!(stack.getItem() instanceof SpearItem spear)) {
                return;
            }
            if (!SpearItem.canPlayerPerformStab(player, spear)) {
                return;
            }
            spear.performStabAttack(player, stack, EquipmentSlot.MAINHAND, true);
            player.resetAttackStrengthTicker();
        });
        context.setPacketHandled(true);
    }
}
