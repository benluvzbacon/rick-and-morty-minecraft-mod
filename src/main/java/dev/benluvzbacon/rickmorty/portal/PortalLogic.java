package dev.benluvzbacon.rickmorty.portal;

import dev.benluvzbacon.rickmorty.RickMortyMod;
import dev.benluvzbacon.rickmorty.config.ModConfig;
import dev.benluvzbacon.rickmorty.registry.ModBlocks;
import dev.benluvzbacon.rickmorty.registry.ModSounds;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.joml.Vector3f;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

public final class PortalLogic {
	private PortalLogic() {}

	private static final Map<UUID, Long> LAST_USE = new WeakHashMap<>();
	private static final int REENTRY_COOLDOWN_TICKS = 40;

	public static void tryUsePortal(PortalBlockEntity portal, Entity entity) {
		World world = entity.getWorld();
		if (!(world instanceof ServerWorld sw)) return;
		MinecraftServer server = sw.getServer();
		long now = sw.getTime();
		Long last = LAST_USE.get(entity.getUuid());
		if (last != null && now - last < REENTRY_COOLDOWN_TICKS) return;
		if (entity.hasVehicle()) return; // don't rip players out of vehicles

		boolean ok;
		if (portal.getTargetDimension() != null) {
			ok = useDimensionPortal(portal, entity, server);
		} else if (portal.getPairId() != null) {
			ok = usePairPortal(portal, entity, server);
		} else {
			return;
		}
		if (ok) {
			LAST_USE.put(entity.getUuid(), sw.getTime());
			if (entity instanceof LivingEntity living) {
				living.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 50, 0, true, false, false));
			}
		}
	}

	private static boolean useDimensionPortal(PortalBlockEntity portal, Entity entity, MinecraftServer server) {
		RegistryKey<World> key = portal.getTargetDimension();
		ServerWorld dest = server.getWorld(key);
		if (dest == null) {
			notify(entity, Text.translatable("msg.rickmorty.portal.no_dimension").formatted(Formatting.RED));
			return false;
		}
		double x = entity.getX();
		double y = entity.getY();
		double z = entity.getZ();
		spawnFx(entity.getWorld() instanceof ServerWorld ew ? ew : null, portal.getPos());
		boolean ok = TeleportUtil.teleportSafe(entity, dest, x, Math.max(y, dest.getBottomY() + 40), z, entity.getYaw());
		if (ok) {
			spawnFx(dest, entity.getBlockPos());
			playTeleportSound(dest, entity.getBlockPos());
		} else {
			notify(entity, Text.translatable("msg.rickmorty.portal.no_safe_spot").formatted(Formatting.RED));
		}
		return ok;
	}

	private static boolean usePairPortal(PortalBlockEntity portal, Entity entity, MinecraftServer server) {
		PortalWorldState state = PortalWorldState.get(server);
		PortalWorldState.Link link = state.get(portal.getPairId());
		if (link == null) return false;
		boolean isA = portal.getCachedState().get(PortalBlock.COLOR) == PortalBlock.PortalColor.GREEN;
		RegistryKey<World> destKey = isA ? (link.hasB ? link.dimB : null) : (link.hasA ? link.dimA : null);
		BlockPos destPos = isA ? link.posB : link.posA;
		if (destKey == null || destPos == null) {
			notify(entity, Text.translatable("msg.rickmorty.portal.no_pair").formatted(Formatting.YELLOW));
			return false;
		}
		ServerWorld dest = server.getWorld(destKey);
		if (dest == null) {
			notify(entity, Text.translatable("msg.rickmorty.portal.no_dimension").formatted(Formatting.RED));
			return false;
		}
		spawnFx(entity.getWorld() instanceof ServerWorld ew ? ew : null, portal.getPos());
		// Keep momentum: portal travel preserves velocity & yaw like a certain someone's gun.
		boolean ok = TeleportUtil.teleportSafe(entity, dest, destPos.getX() + 0.5, destPos.getY(), destPos.getZ() + 0.5, entity.getYaw());
		if (ok) {
			spawnFx(dest, entity.getBlockPos());
			playTeleportSound(dest, entity.getBlockPos());
		}
		return ok;
	}

	public static void playTeleportSound(ServerWorld world, BlockPos pos) {
		world.playSound(null, pos, ModSounds.TELEPORT, SoundCategory.PLAYERS, 0.9f, 1.0f);
	}

	public static void spawnFx(ServerWorld world, BlockPos pos) {
		if (world == null || pos == null) return;
		try {
			world.spawnParticles(new DustParticleEffect(new Vector3f(0.2f, 1.0f, 0.5f), 1.4f),
					pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 40, 0.5, 1.0, 0.5, 0.05);
			world.spawnParticles(ParticleTypes.END_ROD,
					pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 10, 0.4, 0.8, 0.4, 0.02);
		} catch (Throwable t) {
			RickMortyMod.LOGGER.debug("portal fx failed: {}", t.getMessage());
		}
	}

	private static void notify(Entity entity, Text text) {
		if (entity instanceof ServerPlayerEntity player) {
			player.sendMessage(text, true);
		}
	}
}
