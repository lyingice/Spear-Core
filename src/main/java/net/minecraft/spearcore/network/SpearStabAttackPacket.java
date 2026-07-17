package net.minecraft.spearcore.network;

import net.minecraft.core.SectionPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.spearcore.SpearcoreMod;
import net.minecraft.spearcore.item.SpearItem;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SpearStabAttackPacket() implements CustomPacketPayload {
    public static final Type<SpearStabAttackPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(SpearcoreMod.MODID, "spear_stab_attack")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, SpearStabAttackPacket> STREAM_CODEC = StreamCodec.of(
            (RegistryFriendlyByteBuf buffer, SpearStabAttackPacket packet) -> {
            },
            (RegistryFriendlyByteBuf buffer) -> new SpearStabAttackPacket()
    );

    public static void register() {
        SpearcoreMod.addNetworkMessage(TYPE, STREAM_CODEC, SpearStabAttackPacket::handleData);
    }

    @Override
    public Type<SpearStabAttackPacket> type() {
        return TYPE;
    }

    public static void handleData(final SpearStabAttackPacket packet, final IPayloadContext context) {
        if (context.flow() != PacketFlow.SERVERBOUND) {
            return;
        }
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
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
        }).exceptionally(e -> {
            context.connection().disconnect(Component.literal(e.getMessage()));
            return null;
        });
    }
}
