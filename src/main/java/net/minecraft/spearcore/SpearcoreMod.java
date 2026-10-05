package net.minecraft.spearcore;

import net.minecraft.spearcore.config.SpearConfig;
import net.minecraft.spearcore.init.SpearAttributes;
import net.minecraft.spearcore.init.SpearCoreItems;
import net.minecraft.spearcore.init.SpearEnchantments;
import net.minecraft.spearcore.init.SpearSounds;
import net.minecraft.spearcore.network.SpearStabAttackPacket;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.fml.util.thread.SidedThreadGroups;
import net.minecraft.server.TickTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import it.unimi.dsi.fastutil.ints.IntObjectPair;
import it.unimi.dsi.fastutil.ints.IntObjectImmutablePair;

@Mod(SpearcoreMod.MODID)
public class SpearcoreMod {
	public static final Logger LOGGER = LogManager.getLogger(SpearcoreMod.class);
	public static final String MODID = "spearcore";

	public SpearcoreMod() {
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

		SpearStabAttackPacket.register();
		// Start of user code block mod init
		SpearSounds.REGISTRY.register(modEventBus);
		SpearCoreItems.REGISTRY.register(modEventBus);
		SpearAttributes.ATTRIBUTES.register(modEventBus);
		SpearEnchantments.REGISTRY.register(modEventBus);
		modEventBus.addListener(SpearAttributes::addToPlayer);
		// End of user code block mod init

		modEventBus.addListener(this::commonSetup);
		SpearConfig.register();
		MinecraftForge.EVENT_BUS.register(this);
	}

	/** 启动自检：确认七把矛真的注册进物品注册表（1.20.1 版的"怎么确认成功"标记）。 */
	@SubscribeEvent
	public void commonSetup(FMLCommonSetupEvent event) {
		event.enqueueWork(() -> {
			long spears = ForgeRegistries.ITEMS.getValues().stream()
					.filter(i -> MODID.equals(ForgeRegistries.ITEMS.getKey(i).getNamespace()))
					.count();
			LOGGER.info("[spearcore] 已注册 {} 把长矛（Minecraft 1.20.1 / Forge）", spears);
		});
	}

	// ========== 延后任务队列（与原版一致，供后续阶段使用） ==========

	private static final Queue<IntObjectPair<Runnable>> workToBeScheduled = new ConcurrentLinkedQueue<>();
	private static final PriorityQueue<TickTask> workQueue = new PriorityQueue<>(Comparator.comparingInt(TickTask::getTick));

	public static void queueServerWork(int delay, Runnable action) {
		if (Thread.currentThread().getThreadGroup() == SidedThreadGroups.SERVER)
			workToBeScheduled.add(new IntObjectImmutablePair<>(delay, action));
	}

	@SubscribeEvent
	public void tick(TickEvent.ServerTickEvent event) {
		if (event.phase != TickEvent.Phase.END) return;
		int currentTick = event.getServer().getTickCount();
		IntObjectPair<Runnable> work;
		while ((work = workToBeScheduled.poll()) != null) {
			workQueue.add(new TickTask(currentTick + work.leftInt(), work.right()));
		}
		while (!workQueue.isEmpty() && currentTick >= workQueue.peek().getTick()) {
			workQueue.poll().run();
		}
	}
}
