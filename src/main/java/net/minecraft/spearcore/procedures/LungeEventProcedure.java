package net.minecraft.spearcore.procedures;

import net.minecraft.spearcore.item.SpearItem;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.Event;
import net.neoforged.api.distmarker.Dist;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.core.SectionPos;

import net.minecraft.spearcore.SpearcoreMod;

import javax.annotation.Nullable;

@EventBusSubscriber(Dist.CLIENT)
public class LungeEventProcedure {
	@SubscribeEvent
	public static void onLeftClick(PlayerInteractEvent.LeftClickEmpty event) {
		// The active client entry point is SpearClientStabHandler.
	}

	@EventBusSubscriber
	public record LungeEventMessage() implements CustomPacketPayload {
		public static final Type<LungeEventMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(SpearcoreMod.MODID, "procedure_lunge_event"));
		public static final StreamCodec<RegistryFriendlyByteBuf, LungeEventMessage> STREAM_CODEC = StreamCodec.of((RegistryFriendlyByteBuf buffer, LungeEventMessage message) -> {
		}, (RegistryFriendlyByteBuf buffer) -> new LungeEventMessage());

		@Override
		public Type<LungeEventMessage> type() {
			return TYPE;
		}

		public static void handleData(final LungeEventMessage message, final IPayloadContext context) {
			if (context.flow() == PacketFlow.SERVERBOUND) {
				context.enqueueWork(() -> {
					if (!context.player().level().getChunkSource().hasChunk(SectionPos.blockToSectionCoord(context.player().getX()), SectionPos.blockToSectionCoord(context.player().getZ())))
						return;
					execute(context.player().level(), context.player());
				}).exceptionally(e -> {
					context.connection().disconnect(Component.literal(e.getMessage()));
					return null;
				});
			}
		}

		@SubscribeEvent
		public static void registerMessage(FMLCommonSetupEvent event) {
			SpearcoreMod.addNetworkMessage(LungeEventMessage.TYPE, LungeEventMessage.STREAM_CODEC, LungeEventMessage::handleData);
		}
	}

	public static void execute(LevelAccessor world, Entity entity) {
		execute(null, world, entity);
	}

    private static void execute(@Nullable Event event, LevelAccessor world, Entity entity) {
        if (entity == null) return;
        if (!(entity instanceof Player player)) return;
        if (player.level().isClientSide()) return;
        SpearItem.jerotesLungeForwardMaybe(player);
    }
}
