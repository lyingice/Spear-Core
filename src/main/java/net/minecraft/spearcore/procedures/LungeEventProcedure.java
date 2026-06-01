package net.minecraft.spearcore.procedures;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.spearcore.init.SpearSounds;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.Event;
import net.neoforged.api.distmarker.Dist;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.SectionPos;

import net.minecraft.spearcore.SpearcoreMod;

import javax.annotation.Nullable;

@EventBusSubscriber(Dist.CLIENT)
public class LungeEventProcedure {
	@SubscribeEvent
	public static void onLeftClick(PlayerInteractEvent.LeftClickEmpty event) {
		PacketDistributor.sendToServer(new LungeEventMessage());
		execute(event.getLevel(), event.getEntity());
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

        // 冷却检查（3秒 = 60 tick）
        if (player.getCooldowns().isOnCooldown(player.getMainHandItem().getItem())) return;

        ItemStack stack = player.getMainHandItem();
        int lungeLevel = stack.getEnchantmentLevel(
                world.registryAccess()
                        .registryOrThrow(Registries.ENCHANTMENT)
                        .getHolderOrThrow(ResourceKey.create(Registries.ENCHANTMENT,
                                ResourceLocation.fromNamespaceAndPath("spearcore", "lunge")))
        );

        if (lungeLevel <= 0) return;
        if (player.isPassenger() || player.isFallFlying() || player.isInWater()) return;
        if (player.getFoodData().getFoodLevel() <= 6) return;

        player.causeFoodExhaustion(4.0f * lungeLevel);

        float strength = 9.16f * lungeLevel;
        Vec3 look = player.getLookAngle();
        Vec3 horizontal = new Vec3(look.x, 0.0, look.z).normalize();
        player.push(horizontal.x * strength * 0.1, 0.0, horizontal.z * strength * 0.1);

        stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);

        // 3秒冷却
        player.getCooldowns().addCooldown(stack.getItem(), 60);

        SoundEvent sound = switch (lungeLevel) {
            case 2 -> SpearSounds.ITEM_SPEAR_LUNGE_2.get();
            case 3 -> SpearSounds.ITEM_SPEAR_LUNGE_3.get();
            default -> SpearSounds.ITEM_SPEAR_LUNGE_1.get();
        };
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                sound, SoundSource.PLAYERS, 1.0F, 1.0F);
    }
}