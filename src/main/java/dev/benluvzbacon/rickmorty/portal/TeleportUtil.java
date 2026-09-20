package dev.benluvzbacon.rickmorty.portal;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import org.jetbrains.annotations.Nullable;

/**
 * Safe cross-dimensional teleportation. Every destination is validated and repaired:
 * clamps to the world border, searches a safe standable position, and if no natural
 * ground exists (e.g. the pocket dimension void) a small platform is generated.
 */
public final class TeleportUtil {
	private TeleportUtil() {}

	public static final class SafePos {
		public final BlockPos pos;
		public final boolean needsPlatform;

		public SafePos(BlockPos pos, boolean needsPlatform) {
			this.pos = pos;
			this.needsPlatform = needsPlatform;
		}
	}

	@Nullable
	public static ServerWorld getWorld(MinecraftServer server, RegistryKey<World> key) {
		return server.getWorld(key);
	}

	/** Finds a standable position for an entity near (x, z). Never returns inside solid blocks. */
	public static SafePos findSafePos(ServerWorld world, double x, double z, double hintY) {
		WorldBorder border = world.getWorldBorder();
		double margin = 8.0;
		x = Math.max(border.getBoundWest() + margin, Math.min(border.getBoundEast() - margin, x));
		z = Math.max(border.getBoundNorth() + margin, Math.min(border.getBoundSouth() - margin, z));

		int baseX = (int) Math.floor(x);
		int baseZ = (int) Math.floor(z);

		// Spiral search up to radius 48
		for (int radius = 0; radius <= 48; radius += 2) {
			for (int dx = -radius; dx <= radius; dx += 2) {
				for (int dz = -radius; dz <= radius; dz += 2) {
					if (Math.max(Math.abs(dx), Math.abs(dz)) != radius && radius > 0) continue;
					int cx = baseX + dx;
					int cz = baseZ + dz;
					if (!border.contains(cx, cz)) continue;
					world.getChunk(cx >> 4, cz >> 4); // force chunk load
					SafePos found = scanColumn(world, cx, cz, hintY);
					if (found != null) return found;
				}
			}
		}
		// Nothing solid: build a platform near the hint height.
		int y = (int) Math.max(world.getBottomY() + 40, Math.min(world.getTopY() - 10, hintY > world.getBottomY() ? hintY : world.getBottomY() + 70));
		return new SafePos(new BlockPos(baseX, y, baseZ), true);
	}

	@Nullable
	private static SafePos scanColumn(ServerWorld world, int x, int z, double hintY) {
		int top = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z);
		int min = world.getBottomY() + 1;
		if (top <= min) return null;
		BlockPos.Mutable pos = new BlockPos.Mutable(x, top, z);
		for (int y = top; y > Math.max(min, top - 80); y--) {
			pos.setY(y);
			BlockState ground = world.getBlockState(pos.down());
			BlockState feet = world.getBlockState(pos);
			BlockState head = world.getBlockState(pos.up());
			if (ground.isSolidBlock(world, pos.down()) && ground.getFluidState().isEmpty()
					&& feet.isAir() && head.isAir()
					&& feet.getFluidState().isEmpty() && head.getFluidState().isEmpty()) {
				return new SafePos(new BlockPos(x, y, z), false);
			}
		}
		return null;
	}

	/**
	 * Teleports an entity to the given world at a validated safe location near (x, z).
	 * Works for players and regular entities. Safe to call server-side only.
	 */
	public static boolean teleportSafe(Entity entity, ServerWorld dest, double x, double z, float yaw) {
		return teleportSafe(entity, dest, x, dest.getBottomY() + 70, z, yaw);
	}

	public static boolean teleportSafe(Entity entity, ServerWorld dest, double x, double hintY, double z, float yaw) {
		try {
			SafePos safe = findSafePos(dest, x, z, hintY);
			if (safe.needsPlatform) {
				buildPlatform(dest, safe.pos);
			}
			clearHeadroom(dest, safe.pos);
			Vec3d target = new Vec3d(safe.pos.getX() + 0.5, safe.pos.getY(), safe.pos.getZ() + 0.5);
			TeleportTarget t = new TeleportTarget(dest, target, entity.getVelocity(), yaw, entity.getPitch(),
					TeleportTarget.NO_OP);
			Entity result = entity.teleportTo(t);
			return result != null;
		} catch (Exception e) {
			return false;
		}
	}

	public static void buildPlatform(ServerWorld world, BlockPos pos) {
		for (int dx = -1; dx <= 1; dx++) {
			for (int dz = -1; dz <= 1; dz++) {
				world.setBlockState(pos.down().add(dx, 0, dz),
						dev.benluvzbacon.rickmorty.registry.ModBlocks.SCI_FI_METAL.getDefaultState());
				for (int dy = 0; dy < 3; dy++) {
					BlockPos clear = pos.add(dx, dy, dz);
					if (!world.getBlockState(clear).isAir()) {
						world.breakBlock(clear, false);
					}
				}
			}
		}
	}

	public static void clearHeadroom(ServerWorld world, BlockPos pos) {
		for (int dy = 0; dy < 2; dy++) {
			BlockState s = world.getBlockState(pos.up(dy));
			if (!s.isAir() && s.getHardness(world, pos.up(dy)) >= 0 && !s.isOf(net.minecraft.block.Blocks.BEDROCK)) {
				world.breakBlock(pos.up(dy), false);
			}
		}
	}

	/** Overworld spawn fallback. */
	public static boolean teleportToSpawn(ServerPlayerEntity player) {
		MinecraftServer server = player.getServer();
		if (server == null) return false;
		ServerWorld overworld = server.getOverworld();
		BlockPos spawn = overworld.getSpawnPos();
		return teleportSafe(player, overworld, spawn.getX(), spawn.getZ(), player.getYaw());
	}
}
