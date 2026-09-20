package dev.benluvzbacon.rickmorty.world.gen;

import dev.benluvzbacon.rickmorty.RickMortyMod;
import dev.benluvzbacon.rickmorty.portal.PortalBlock;
import dev.benluvzbacon.rickmorty.portal.PortalBlockEntity;
import dev.benluvzbacon.rickmorty.registry.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

/**
 * Decoration + code-built structure generator for the four mod dimensions.
 * Runs inside the chunk generator's feature phase. Fully deterministic.
 */
public final class RMFeatures {
	private RMFeatures() {}

	public static void decorate(StructureWorldAccess world, Chunk chunk, RMChunkGenerator generator) {
		ChunkPos cp = chunk.getPos();
		long seed = world.getSeed() ^ ((long) cp.x * 341873128712L) ^ ((long) cp.z * 132897987541L)
				^ generator.preset().ordinal() * 8191L;
		Random random = Random.create(seed);

		switch (generator.preset()) {
			case ALIEN -> {
				crystalBlobs(world, chunk, random, ModBlocks.ALIEN_CRYSTAL_ORE.getDefaultState(),
						ModBlocks.ALIEN_ROCK.getDefaultState(), 2, -40, 90);
				surfaceClusters(world, chunk, random, 2, ModBlocks.CRYSTAL_CLUSTER.getDefaultState());
				alienSpires(world, chunk, random);
				RuinBuilder.tryBuild(world, chunk, random, generator.preset(), 14);
			}
			case CRONENBERG -> {
				fleshPatches(world, chunk, random);
				crystalBlobs(world, chunk, random, ModBlocks.ALIEN_CRYSTAL_ORE.getDefaultState(),
						ModBlocks.ALIEN_ROCK.getDefaultState(), 1, -40, 70);
				FacilityBuilder.tryBuild(world, chunk, random, 60);
				RuinBuilder.tryBuild(world, chunk, random, generator.preset(), 20);
			}
			case POCKET -> {
				debris(world, chunk, random);
				pocketCrystals(world, chunk, random);
				RuinBuilder.tryBuild(world, chunk, random, generator.preset(), 16);
				FacilityBuilder.tryBuild(world, chunk, random, 90);
				if (cp.x == 0 && cp.z == 0) spawnIslandExtras(world, chunk);
			}
			case CITADEL -> CityBuilder.buildInto(world, chunk, random);
		}
	}

	/** Small ore veins: replaces target rock only, so veins embed naturally. */
	private static void crystalBlobs(StructureWorldAccess world, Chunk chunk, Random random,
									 BlockState ore, BlockState host, int veinsPerChunk, int minY, int maxY) {
		int baseX = chunk.getPos().getStartX();
		int baseZ = chunk.getPos().getStartZ();
		for (int v = 0; v < veinsPerChunk; v++) {
			int cx = baseX + random.nextInt(16);
			int cz = baseZ + random.nextInt(16);
			int cy = minY + random.nextInt(Math.max(1, maxY - minY));
			int size = 3 + random.nextInt(4);
			for (int i = 0; i < size; i++) {
				BlockPos p = new BlockPos(cx + random.nextInt(3) - 1, cy + random.nextInt(3) - 1, cz + random.nextInt(3) - 1);
				BlockState current = world.getBlockState(p);
				if (current.isOf(host.getBlock()) || current.isOf(Blocks.STONE) || current.isOf(Blocks.DEEPSLATE)) {
					world.setBlockState(p, ore, 2);
				}
			}
		}
	}

	private static void surfaceClusters(StructureWorldAccess world, Chunk chunk, Random random, int tries, BlockState state) {
		int baseX = chunk.getPos().getStartX();
		int baseZ = chunk.getPos().getStartZ();
		for (int i = 0; i < tries; i++) {
			if (random.nextInt(3) != 0) continue;
			int x = baseX + random.nextInt(16);
			int z = baseZ + random.nextInt(16);
			int y = world.getTopY(Heightmap.Type.MOTION_BLOCKING, x, z);
			if (y <= world.getBottomY() + 1) continue;
			BlockPos p = new BlockPos(x, y, z);
			if (!world.getBlockState(p.down()).isAir() && world.getBlockState(p).isAir()) {
				world.setBlockState(p, state, 2);
				if (random.nextBoolean()) {
					BlockPos side = p.offset(net.minecraft.util.math.Direction.random(random));
					if (world.getBlockState(side).isAir()) world.setBlockState(side, state, 2);
				}
			}
		}
	}

	private static void alienSpires(StructureWorldAccess world, Chunk chunk, Random random) {
		if (random.nextInt(5) != 0) return;
		int x = chunk.getPos().getStartX() + random.nextInt(16);
		int z = chunk.getPos().getStartZ() + random.nextInt(16);
		int y = world.getTopY(Heightmap.Type.MOTION_BLOCKING, x, z);
		if (y <= world.getBottomY() + 2) return;
		int height = 4 + random.nextInt(7);
		for (int i = 0; i < height; i++) {
			world.setBlockState(new BlockPos(x, y + i, z), ModBlocks.ALIEN_ROCK.getDefaultState(), 2);
		}
		world.setBlockState(new BlockPos(x, y + height, z), ModBlocks.CRYSTAL_CLUSTER.getDefaultState(), 2);
	}

	private static void fleshPatches(StructureWorldAccess world, Chunk chunk, Random random) {
		int baseX = chunk.getPos().getStartX();
		int baseZ = chunk.getPos().getStartZ();
		for (int i = 0; i < 3; i++) {
			int x = baseX + random.nextInt(16);
			int z = baseZ + random.nextInt(16);
			int y = world.getTopY(Heightmap.Type.MOTION_BLOCKING, x, z);
			if (y <= world.getBottomY() + 1) continue;
			BlockState patch = random.nextBoolean() ? Blocks.NETHER_WART_BLOCK.getDefaultState()
					: Blocks.CRIMSON_HYPHAE.getDefaultState();
			for (int dx = -2; dx <= 2; dx++) {
				for (int dz = -2; dz <= 2; dz++) {
					if (dx * dx + dz * dz > 5) continue;
					int py = world.getTopY(Heightmap.Type.MOTION_BLOCKING, x + dx, z + dz);
					if (py <= world.getBottomY() + 1) continue;
					world.setBlockState(new BlockPos(x + dx, py - 1, z + dz), patch, 2);
				}
			}
		}
	}

	private static void debris(StructureWorldAccess world, Chunk chunk, Random random) {
		int baseX = chunk.getPos().getStartX();
		int baseZ = chunk.getPos().getStartZ();
		for (int i = 0; i < 3; i++) {
			int x = baseX + random.nextInt(16);
			int y = 48 + random.nextInt(150);
			int z = baseZ + random.nextInt(16);
			BlockPos center = new BlockPos(x, y, z);
			BlockState what = random.nextInt(4) == 0 ? ModBlocks.PORTAL_ENERGY_BLOCK.getDefaultState()
					: ModBlocks.POCKET_ROCK.getDefaultState();
			int size = 1 + random.nextInt(3);
			for (int j = 0; j < size; j++) {
				world.setBlockState(center.add(random.nextInt(3) - 1, random.nextInt(3) - 1, random.nextInt(3) - 1), what, 2);
			}
		}
	}

	private static void pocketCrystals(StructureWorldAccess world, Chunk chunk, Random random) {
		// embed pocket crystal ore into island rock
		int baseX = chunk.getPos().getStartX();
		int baseZ = chunk.getPos().getStartZ();
		for (int i = 0; i < 2; i++) {
			int x = baseX + random.nextInt(16);
			int y = 40 + random.nextInt(160);
			int z = baseZ + random.nextInt(16);
			BlockPos p = new BlockPos(x, y, z);
			if (world.getBlockState(p).isOf(ModBlocks.POCKET_ROCK)) {
				world.setBlockState(p, ModBlocks.POCKET_CRYSTAL_ORE.getDefaultState(), 2);
			}
		}
	}

	/** The pocket dimension spawn island always offers a way home. */
	private static void spawnIslandExtras(StructureWorldAccess world, Chunk chunk) {
		BlockPos center = new BlockPos(3, 67, 3);
		// clear a little space
		for (int dy = 0; dy < 4; dy++) {
			for (int dx = -1; dx <= 1; dx++) {
				for (int dz = -1; dz <= 1; dz++) {
					world.setBlockState(center.add(dx, dy, dz), Blocks.AIR.getDefaultState(), 2);
				}
			}
		}
		PortalBlockEntity portal = PortalBlock.place(world, center, PortalBlock.PortalColor.PURPLE);
		if (portal != null) {
			portal.configure(null, World.OVERWORLD, -1);
		}
		// a couple of crystals to get stranded players started
		for (int i = 0; i < 3; i++) {
			BlockPos p = center.add(-3 + i * 3, -1, 4);
			if (!world.getBlockState(p).isAir()) world.setBlockState(p.up(), ModBlocks.CRYSTAL_CLUSTER.getDefaultState(), 2);
		}
		RickMortyMod.LOGGER.debug("[RickMorty] pocket spawn island decorated at {}", center);
	}
}
