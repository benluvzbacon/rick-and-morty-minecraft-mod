package dev.benluvzbacon.rickmorty.event;

import dev.benluvzbacon.rickmorty.blockentity.UnstablePortalBlockEntity;
import dev.benluvzbacon.rickmorty.config.ModConfig;
import dev.benluvzbacon.rickmorty.entity.RickEntity;
import dev.benluvzbacon.rickmorty.entity.SecurityBotEntity;
import dev.benluvzbacon.rickmorty.registry.*;
import dev.benluvzbacon.rickmorty.portal.TeleportUtil;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * Random dimensional events. Kept deliberately rare so they stay interesting.
 *  - Portal Storm: unstable rifts open, alien wildlife slips through, rift shards remain.
 *  - Rick Arrival: a portal spits out a Rick near a lonely player.
 *  - Citadel Patrol: security drones sweep for troublemakers.
 *  - Dimensional Anomaly: a patch of reality starts behaving... differently.
 */
public class DimensionalEventManager {
	private static final int CHECK_INTERVAL_TICKS = 600; // every 30 s

	private static final Map<UUID, Long> STORM_COOLDOWN = new HashMap<>();
	private static final Map<UUID, Long> RICK_COOLDOWN = new HashMap<>();
	private static final Map<UUID, Long> PATROL_COOLDOWN = new HashMap<>();
	private static final Map<UUID, Long> ANOMALY_COOLDOWN = new HashMap<>();

	// active anomalies per world
	private static final Map<World, List<Anomaly>> ANOMALIES = new HashMap<>();

	private record Anomaly(BlockPos center, int radius, long expiry) {}

	public static void tick(MinecraftServer server) {
		tickAnomalies(server);
		if (server.getTicks() % CHECK_INTERVAL_TICKS != 0) return;
		if (!ModConfig.get().enableEvents) return;
		double freq = ModConfig.get().eventFrequencyMultiplier;
		if (freq <= 0) return;

		for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
			if (player.isSpectator()) continue;
			Random random = new Random();
			ServerWorld world = (ServerWorld) player.getWorld();

			if (world.getRegistryKey() == ModDimensions.CITADEL_WORLD) {
				maybeCitadelPatrol(world, player, random, freq);
			} else if (world.getRegistryKey() == World.OVERWORLD) {
				maybePortalStorm(world, player, random, freq);
				maybeRickArrival(world, player, random, freq);
			}
			if (world.getRegistryKey() != ModDimensions.CITADEL_WORLD) {
				maybeAnomaly(world, player, random, freq);
			}
		}
	}

	// ------------------------------------------------------------ portal storm

	public static void maybePortalStorm(ServerWorld world, ServerPlayerEntity player, Random random, double freq) {
		if (world.getTime() % 2400 != 2100) return; // aligned, rare window
		if (roll(player, STORM_COOLDOWN, world.getTime(), 120 * 20)) return;
		if (random.nextDouble() > 0.5 * freq) return;

		int portalCount = 2 + random.nextInt(3);
		int placed = spawnStormAt(world, player.getBlockPos(), portalCount);
		if (placed == 0) return;
		cooldown(player, STORM_COOLDOWN, world.getTime());
		world.playSound(null, player.getBlockPos(), ModSounds.PORTAL_STORM, SoundCategory.HOSTILE, 1.6f, 0.5f);
		player.sendMessage(Text.translatable("msg.rickmorty.event.portal_storm")
				.formatted(Formatting.DARK_PURPLE), false);
	}

	/** Testable entry: spawns a storm around a position. Returns portals placed. */
	public static int spawnStormAt(ServerWorld world, BlockPos center, int portalCount) {
		int placed = 0;
		Random random = new Random();
		for (int i = 0; i < portalCount * 4 && placed < portalCount; i++) {
			double angle = random.nextDouble() * Math.PI * 2;
			double dist = 16 + random.nextInt(24);
			int x = center.getX() + (int) (Math.cos(angle) * dist);
			int z = center.getZ() + (int) (Math.sin(angle) * dist);
			int y = world.getTopY(Heightmap.Type.MOTION_BLOCKING, x, z);
			if (y <= world.getBottomY() + 1) continue;
			BlockPos pos = new BlockPos(x, y, z);
			if (!world.getBlockState(pos).isAir() || !world.getBlockState(pos.up()).isAir()) continue;
			world.setBlockState(pos, ModBlocks.UNSTABLE_PORTAL.getDefaultState());
			if (world.getBlockEntity(pos) instanceof UnstablePortalBlockEntity rift) {
				rift.configure(2400 + random.nextInt(2400), 1 + random.nextInt(2));
			}
			placed++;
		}
		return placed;
	}

	// ------------------------------------------------------------ rick arrival

	private static void maybeRickArrival(ServerWorld world, ServerPlayerEntity player, Random random, double freq) {
		if (random.nextDouble() > 0.22 * freq) return;
		if (roll(player, RICK_COOLDOWN, world.getTime(), 240 * 20)) return;
		// no party underfoot
		var ricks = world.getEntitiesByClass(RickEntity.class, player.getBoundingBox().expand(60), e -> true);
		if (!ricks.isEmpty()) return;

		BlockPos where = TeleportUtil.findSafePos(world,
				player.getX() + random.nextInt(24) - 12, player.getZ() + random.nextInt(24) - 12,
				player.getY()).pos;
		RickEntity rick = new RickEntity(ModEntities.RICK, world);
		rick.refreshPositionAndAngles(where, random.nextFloat() * 360, 0);
		world.spawnEntity(rick);
		dev.benluvzbacon.rickmorty.portal.PortalLogic.spawnFx(world, where);
		world.playSound(null, where, ModSounds.PORTAL_OPEN, SoundCategory.NEUTRAL, 1.4f, 1.0f);
		cooldown(player, RICK_COOLDOWN, world.getTime());
		player.sendMessage(Text.translatable("msg.rickmorty.event.rick_arrival")
				.formatted(Formatting.AQUA), false);
	}

	// ------------------------------------------------------------ citadel patrol

	private static void maybeCitadelPatrol(ServerWorld world, ServerPlayerEntity player, Random random, double freq) {
		if (random.nextDouble() > 0.3 * freq) return;
		if (roll(player, PATROL_COOLDOWN, world.getTime(), 180 * 20)) return;
		int count = 2 + random.nextInt(2);
		for (int i = 0; i < count; i++) {
			BlockPos where = TeleportUtil.findSafePos(world,
					player.getX() + random.nextInt(40) - 20, player.getZ() + random.nextInt(40) - 20,
					player.getY()).pos;
			SecurityBotEntity bot = new SecurityBotEntity(ModEntities.SECURITY_BOT, world);
			bot.refreshPositionAndAngles(where, random.nextFloat() * 360, 0);
			world.spawnEntity(bot);
		}
		cooldown(player, PATROL_COOLDOWN, world.getTime());
		player.sendMessage(Text.translatable("msg.rickmorty.event.citadel_patrol")
				.formatted(Formatting.GRAY), false);
	}

	// ------------------------------------------------------------ anomaly

	private static void maybeAnomaly(ServerWorld world, ServerPlayerEntity player, Random random, double freq) {
		if (random.nextDouble() > 0.25 * freq) return;
		if (roll(player, ANOMALY_COOLDOWN, world.getTime(), 200 * 20)) return;
		BlockPos center = TeleportUtil.findSafePos(world,
				player.getX() + random.nextInt(40) - 20, player.getZ() + random.nextInt(40) - 20,
				player.getY()).pos;
		ANOMALIES.computeIfAbsent(world, w -> new ArrayList<>())
				.add(new Anomaly(center, 12, world.getTime() + 3600));
		cooldown(player, ANOMALY_COOLDOWN, world.getTime());
		player.sendMessage(Text.translatable("msg.rickmorty.event.anomaly")
				.formatted(Formatting.DARK_AQUA), false);
		world.playSound(null, center, ModSounds.ANOMALY, SoundCategory.AMBIENT, 2.0f, 0.6f);
	}

	private static void tickAnomalies(MinecraftServer server) {
		Iterator<Map.Entry<World, List<Anomaly>>> it = ANOMALIES.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<World, List<Anomaly>> entry = it.next();
			World world = entry.getKey();
			if (!(world instanceof ServerWorld sw)) {
				it.remove();
				continue;
			}
			entry.getValue().removeIf(a -> sw.getTime() > a.expiry());
			for (Anomaly anomaly : entry.getValue()) {
				// ambient particles
				if (sw.getTime() % 6 == 0) {
					sw.spawnParticles(ParticleTypes.PORTAL,
							anomaly.center().getX() + (sw.random.nextDouble() - 0.5) * anomaly.radius() * 1.4,
							anomaly.center().getY() + sw.random.nextDouble() * 6,
							anomaly.center().getZ() + (sw.random.nextDouble() - 0.5) * anomaly.radius() * 1.4,
							2, 0.3, 0.6, 0.3, 0.05);
				}
				if (sw.getTime() % 30 == 0) {
					for (ServerPlayerEntity player : sw.getPlayers(p -> p.getBlockPos().isWithinDistance(anomaly.center(), anomaly.radius()))) {
						player.addStatusEffect(new StatusEffectInstance(ModStatusEffects.DIMENSIONAL_INSTABILITY,
								200, 0, true, false, true));
					}
				}
			}
		}
	}

	// ------------------------------------------------------------ cooldown helpers

	private static boolean roll(ServerPlayerEntity player, Map<UUID, Long> cooldowns, long now, int required) {
		Long last = cooldowns.get(player.getUuid());
		return last != null && now - last < required;
	}

	private static void cooldown(ServerPlayerEntity player, Map<UUID, Long> cooldowns, long now) {
		cooldowns.put(player.getUuid(), now);
	}
}
