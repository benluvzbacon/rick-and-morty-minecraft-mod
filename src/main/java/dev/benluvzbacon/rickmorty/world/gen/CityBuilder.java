package dev.benluvzbacon.rickmorty.world.gen;

import dev.benluvzbacon.rickmorty.RickMortyMod;
import dev.benluvzbacon.rickmorty.registry.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.loot.LootTable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.chunk.Chunk;

/**
 * The Citadel of Ricks: an endless procedural city.
 * Layout: 4x4-chunk districts separated by single-chunk streets. Each district is built
 * deterministically (from its hash) as either tower blocks, labs, shops or a park.
 * A portal hub plaza sits at the origin.
 */
public final class CityBuilder {
	private CityBuilder() {}

	private static final int GROUND = 64;
	private static final RegistryKey<LootTable> CITADEL_LOOT = RegistryKey.of(RegistryKeys.LOOT_TABLE,
			RickMortyMod.id("chests/citadel"));

	private static long plotHash(int px, int pz) {
		long h = px * 0x9E3779B97F4A7C15L ^ pz * 0xC2B2AE3D27D4EB4FL;
		h ^= h >>> 33;
		h *= 0xff51afd7ed558ccdL;
		h ^= h >>> 33;
		return h;
	}

	public static void buildInto(StructureWorldAccess world, Chunk chunk, Random ignored) {
		ChunkPos cp = chunk.getPos();
		int px = Math.floorDiv(cp.x, 4);
		int pz = Math.floorDiv(cp.z, 4);
		int localX = Math.floorMod(cp.x, 4);
		int localZ = Math.floorMod(cp.z, 4);

		BlockPos base = new BlockPos(cp.getStartX(), GROUND, cp.getStartZ());

		if (cp.x == 0 && cp.z == 0) {
			buildPortalHub(world, cp);
			return;
		}

		boolean streetX = localX == 3;
		boolean streetZ = localZ == 3;

		if (streetX || streetZ) {
			buildStreet(world, base, streetX, streetZ, px, pz);
			return;
		}

		long hash = plotHash(px, pz);
		int kind = (int) (hash & 3);
		switch (kind) {
			case 0 -> buildTower(world, cp, px, pz, localX, localZ, hash);
			case 1 -> buildLab(world, cp, px, pz, localX, localZ, hash);
			case 2 -> buildShops(world, cp, px, pz, localX, localZ, hash);
			default -> buildPark(world, cp, px, pz, localX, localZ, hash);
		}
	}

	// ---------------------------------------------------------------- pieces

	private static void buildStreet(StructureWorldAccess world, BlockPos base, boolean streetX, boolean streetZ, int px, int pz) {
		BlockPos.Mutable p = new BlockPos.Mutable();
		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {
				p.set(base.getX() + x, GROUND - 1, base.getZ() + z);
				world.setBlockState(p, ModBlocks.CITADEL_METAL.getDefaultState(), 2);
			}
		}
		// street lamps at intersections
		if (streetX && streetZ) {
			for (int dx = 2; dx < 14; dx += 11) {
				for (int dz = 2; dz < 14; dz += 11) {
					for (int y = 0; y < 4; y++) {
						world.setBlockState(base.add(dx, y, dz), ModBlocks.SCI_FI_METAL.getDefaultState(), 2);
					}
					world.setBlockState(base.add(dx, 4, dz), ModBlocks.PORTAL_ENERGY_BLOCK.getDefaultState(), 2);
				}
			}
		}
	}

	private static int roomLocalX(int localX) {
		return localX; // 0..2 buildable chunk columns inside a district
	}

	/** Tall buildings: 2 towers per district, sliced per chunk. */
	private static void buildTower(StructureWorldAccess world, ChunkPos cp, int px, int pz,
								   int localX, int localZ, long hash) {
		int height = 30 + (int) (hash >> 8 & 31);
		int plotMinX = px * 64, plotMinZ = pz * 64; // district world origin (4 chunks = 64 blocks)
		// tower footprint 18x18 centered in the 48x48 buildable area
		int fx0 = plotMinX + 14, fz0 = plotMinZ + 14, fx1 = fx0 + 17, fz1 = fz0 + 17;
		int startX = cp.getStartX(), startZ = cp.getStartZ();
		Random rand = Random.create(hash);
		int windowPhase = (int) (hash >> 4 & 7);

		for (int x = startX; x < startX + 16; x++) {
			for (int z = startZ; z < startZ + 16; z++) {
				boolean inX = x >= fx0 && x <= fx1;
				boolean inZ = z >= fz0 && z <= fz1;
				if (!inX || !inZ) {
					world.setBlockState(new BlockPos(x, GROUND - 1, z), ModBlocks.CITADEL_METAL.getDefaultState(), 2);
					continue;
				}
				world.setBlockState(new BlockPos(x, GROUND - 1, z), ModBlocks.SCI_FI_METAL.getDefaultState(), 2);
				boolean border = x == fx0 || x == fx1 || z == fz0 || z == fz1;
				for (int y = GROUND; y < GROUND + height; y++) {
					if (!border) {
						if (y == GROUND + 6 || (y - GROUND) % 8 == 0) {
							world.setBlockState(new BlockPos(x, y, z), ModBlocks.CITADEL_METAL.getDefaultState(), 2); // floors
						}
						continue;
					}
					boolean window = ((y - GROUND + windowPhase) % 8) >= 3 && ((y - GROUND + windowPhase) % 8) <= 5;
					boolean doorway = y >= GROUND && y <= GROUND + 2 && ((x - fx0) == 8 || (x - fx0) == 9) && z == fz0;
					if (doorway) {
						continue; // hole = door
					}
					BlockState wall = window ? ModBlocks.LAB_GLASS.getDefaultState()
							: ((x + y + z) % 11 == 0 ? ModBlocks.PORTAL_ENERGY_BLOCK.getDefaultState()
							: ModBlocks.CITADEL_METAL.getDefaultState());
					world.setBlockState(new BlockPos(x, y, z), wall, 2);
				}
				// roof
				world.setBlockState(new BlockPos(x, GROUND + height, z), ModBlocks.SCI_FI_METAL.getDefaultState(), 2);
			}
		}
		// ground floor interior props + loot on one designated chunk
		if (localX == 1 && localZ == 1) {
			BlockPos.Mutable prop = new BlockPos.Mutable();
			BlockPos center = new BlockPos(fx0 + 8, GROUND, fz0 + 8);
			for (int i = 0; i < 4; i++) {
				prop.set(center.getX() - 5 + i * 3, GROUND, center.getZ() - 5);
				world.setBlockState(prop, i == 0 ? ModBlocks.QUANTUM_COMPUTER.getDefaultState()
						: ModBlocks.QUANTUM_COMPUTER.getDefaultState(), 2);
			}
			if ((hash >> 12 & 3) == 0) {
				prop.set(center.getX() + 4, GROUND, center.getZ() + 4);
				world.setBlockState(prop, Blocks.CHEST.getDefaultState(), 2);
				lootChest(world, prop, hash);
			}
			prop.set(center.getX() - 6, GROUND, center.getZ() + 6);
			world.setBlockState(prop, ModBlocks.PORTAL_ENERGY_BLOCK.getDefaultState(), 2);
		}
	}

	/** Low R&D labs with rick-tech content. */
	private static void buildLab(StructureWorldAccess world, ChunkPos cp, int px, int pz,
								 int localX, int localZ, long hash) {
		int plotMinX = px * 64, plotMinZ = pz * 64;
		int fx0 = plotMinX + 8, fz0 = plotMinZ + 8, fx1 = fx0 + 31, fz1 = fz0 + 31;
		int height = 9;
		int startX = cp.getStartX(), startZ = cp.getStartZ();
		for (int x = startX; x < startX + 16; x++) {
			for (int z = startZ; z < startZ + 16; z++) {
				if (x < fx0 || x > fx1 || z < fz0 || z > fz1) {
					world.setBlockState(new BlockPos(x, GROUND - 1, z), ModBlocks.CITADEL_METAL.getDefaultState(), 2);
					continue;
				}
				world.setBlockState(new BlockPos(x, GROUND - 1, z), ModBlocks.SCI_FI_METAL.getDefaultState(), 2);
				boolean border = x == fx0 || x == fx1 || z == fz0 || z == fz1;
				for (int y = GROUND; y <= GROUND + height; y++) {
					if (y == GROUND + height) {
						world.setBlockState(new BlockPos(x, y, z), ModBlocks.CITADEL_METAL.getDefaultState(), 2);
						continue;
					}
					if (!border) continue;
					boolean window = ((x + z) % 6 < 2) && y >= GROUND + 2 && y <= GROUND + 3;
					boolean doorway = y >= GROUND && y <= GROUND + 2 && ((z - fz0) == 15 || (z - fz0) == 16) && x == fx0;
					if (doorway) continue;
					world.setBlockState(new BlockPos(x, y, z),
							window ? ModBlocks.LAB_GLASS.getDefaultState() : ModBlocks.SCI_FI_METAL.getDefaultState(), 2);
				}
			}
		}
		if (localX == 1 && localZ == 1) {
			BlockPos c = new BlockPos(fx0 + 16, GROUND, fz0 + 16);
			// rick-tech line-up
			world.setBlockState(c.add(-6, 0, -6), ModBlocks.RICK_WORKBENCH.getDefaultState(), 2);
			world.setBlockState(c.add(-6, 0, -5), ModBlocks.QUANTUM_COMPUTER.getDefaultState(), 2);
			world.setBlockState(c.add(-4, 0, -6), ModBlocks.PORTAL_FLUID_TANK.getDefaultState(), 2);
			world.setBlockState(c.add(5, 0, 5), ModBlocks.QUANTUM_COMPUTER.getDefaultState(), 2);
			world.setBlockState(c.add(5, 0, 6), ModBlocks.QUANTUM_COMPUTER.getDefaultState(), 2);
			world.setBlockState(c.add(0, 0, -7), ModBlocks.PORTAL_ENERGY_BLOCK.getDefaultState(), 2);
			world.setBlockState(c.add(4, 0, -2), Blocks.CHEST.getDefaultState(), 2);
			lootChest(world, c.add(4, 0, -2), hash);
		}
	}

	/** A row of small shops facing the street. */
	private static void buildShops(StructureWorldAccess world, ChunkPos cp, int px, int pz,
								   int localX, int localZ, long hash) {
		int plotMinX = px * 64, plotMinZ = pz * 64;
		int startX = cp.getStartX(), startZ = cp.getStartZ();
		int shopCount = 4;
		for (int s = 0; s < shopCount; s++) {
			int sx0 = plotMinX + 6 + s * 12;
			int sz0 = plotMinZ + 10;
			int sx1 = sx0 + 9, sz1 = sz0 + 13;
			int height = 6 + (int) ((hash >> (s * 3)) & 3);
			for (int x = startX; x < startX + 16; x++) {
				for (int z = startZ; z < startZ + 16; z++) {
					if (x < sx0 || x > sx1 || z < sz0 || z > sz1) continue;
					world.setBlockState(new BlockPos(x, GROUND - 1, z), ModBlocks.SCI_FI_METAL.getDefaultState(), 2);
					boolean border = x == sx0 || x == sx1 || z == sz0 || z == sz1;
					for (int y = GROUND; y <= GROUND + height; y++) {
						if (y == GROUND + height) {
							world.setBlockState(new BlockPos(x, y, z), ModBlocks.CITADEL_METAL.getDefaultState(), 2);
							continue;
						}
						if (!border) continue;
						boolean window = y >= GROUND + 1 && y <= GROUND + 2 && ((x + z) % 4 < 2);
						boolean doorway = y >= GROUND && y <= GROUND + 1 && x == sx0 + 4 && z == sz0;
						if (doorway) continue;
						world.setBlockState(new BlockPos(x, y, z),
								window ? ModBlocks.LAB_GLASS.getDefaultState() : ModBlocks.CITADEL_METAL.getDefaultState(), 2);
					}
				}
			}
			// shop sign
			world.setBlockState(new BlockPos(sx0 + 4, GROUND + height + 1, sz0),
					ModBlocks.PORTAL_ENERGY_BLOCK.getDefaultState(), 2);
			// chest counter in some shops
			if (((hash >> s) & 1) == 1 && localX == 1 && localZ == 0) {
				world.setBlockState(new BlockPos(sx0 + 5, GROUND, sz0 + 6), Blocks.CHEST.getDefaultState(), 2);
				lootChest(world, new BlockPos(sx0 + 5, GROUND, sz0 + 6), hash + s);
			}
		}
		// sidewalk area
		for (int x = startX; x < startX + 16; x++) {
			for (int z = startZ; z < startZ + 16; z++) {
				world.setBlockState(new BlockPos(x, GROUND - 1, z), ModBlocks.CITADEL_METAL.getDefaultState(), 2);
			}
		}
	}

	/** Open plaza with trees of glass and crystal. */
	private static void buildPark(StructureWorldAccess world, ChunkPos cp, int px, int pz,
								  int localX, int localZ, long hash) {
		int startX = cp.getStartX(), startZ = cp.getStartZ();
		Random rand = Random.create(hash ^ (localX * 31L + localZ * 37L));
		for (int x = startX; x < startX + 16; x++) {
			for (int z = startZ; z < startZ + 16; z++) {
				world.setBlockState(new BlockPos(x, GROUND - 1, z), ModBlocks.CITADEL_METAL.getDefaultState(), 2);
			}
		}
		for (int i = 0; i < 3; i++) {
			int x = startX + 2 + rand.nextInt(12);
			int z = startZ + 2 + rand.nextInt(12);
			int h = 4 + rand.nextInt(4);
			for (int y = 0; y < h; y++) {
				world.setBlockState(new BlockPos(x, GROUND + y, z), ModBlocks.SCI_FI_METAL.getDefaultState(), 2);
			}
			world.setBlockState(new BlockPos(x, GROUND + h, z), ModBlocks.CRYSTAL_CLUSTER.getDefaultState(), 2);
		}
		if (rand.nextInt(4) == 0) {
			int x = startX + 8, z = startZ + 8;
			world.setBlockState(new BlockPos(x, GROUND, z), ModBlocks.MEESEEKS_BOX.getDefaultState(), 2);
		}
		if (rand.nextInt(6) == 0) {
			int x = startX + 4, z = startZ + 4;
			world.setBlockState(new BlockPos(x, GROUND, z), Blocks.CHEST.getDefaultState(), 2);
			lootChest(world, new BlockPos(x, GROUND, z), hash);
		}
	}

	/** The spawn plaza: four fixed dimension portals + the portal station. */
	private static void buildPortalHub(StructureWorldAccess world, ChunkPos cp) {
		int startX = cp.getStartX(), startZ = cp.getStartZ();
		for (int x = startX; x < startX + 16; x++) {
			for (int z = startZ; z < startZ + 16; z++) {
				world.setBlockState(new BlockPos(x, GROUND - 1, z), ModBlocks.CITADEL_METAL.getDefaultState(), 2);
				if (x >= 3 && x <= 12 && z >= 3 && z <= 12) {
					world.setBlockState(new BlockPos(x, GROUND - 1, z), ModBlocks.SCI_FI_METAL.getDefaultState(), 2);
				}
			}
		}
		// lamp pillars
		for (int c = 0; c < 4; c++) {
			int x = c % 2 == 0 ? 3 : 12;
			int z = c < 2 ? 3 : 12;
			for (int y = 0; y < 3; y++) {
				world.setBlockState(new BlockPos(x, GROUND + y, z), ModBlocks.SCI_FI_METAL.getDefaultState(), 2);
			}
			world.setBlockState(new BlockPos(x, GROUND + 3, z), ModBlocks.PORTAL_ENERGY_BLOCK.getDefaultState(), 2);
		}
		RickMortyMod.LOGGER.debug("[RickMorty] citadel portal hub generated at chunk {}", cp);
	}

	private static void lootChest(StructureWorldAccess world, BlockPos pos, long seed) {
		var be = world.getBlockEntity(pos);
		if (be instanceof net.minecraft.block.entity.LootableContainerBlockEntity container) {
			container.setLootTable(CITADEL_LOOT, seed);
		}
	}
}
