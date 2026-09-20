package dev.benluvzbacon.rickmorty.world.gen;

import dev.benluvzbacon.rickmorty.registry.ModBlocks;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.chunk.Chunk;

/**
 * Scattered abandoned structures: alien ruins / bunker remnants with loot.
 * Single-chunk, placed only when the chunk owns a ruin cell.
 */
public final class RuinBuilder {
	private RuinBuilder() {}

	/**
	 * @param chunksPerRuin rarity: one ruin per roughly this many chunks
	 */
	public static void tryBuild(StructureWorldAccess world, Chunk chunk, Random random,
								RMChunkGenerator.Preset preset, int chunksPerRuin) {
		if (random.nextInt(chunksPerRuin) != 0) return;
		ChunkPos cp = chunk.getPos();
		int x = cp.getStartX() + 2 + random.nextInt(12);
		int z = cp.getStartZ() + 2 + random.nextInt(12);
		int y = world.getTopY(Heightmap.Type.MOTION_BLOCKING, x, z);
		if (y <= world.getBottomY() + 2 || y > 200) return;

		SchematicWorld s = SchematicWorld.create("ruin_" + preset + "_" + random.nextInt(3), 12, 8, 12, sc -> {
			var wall = switch (preset) {
				case POCKET -> ModBlocks.POCKET_ROCK.getDefaultState();
				default -> ModBlocks.ALIEN_ROCK.getDefaultState();
			};
			var floor = ModBlocks.SCI_FI_METAL.getDefaultState();
			// broken floor
			for (int dx = 0; dx < 12; dx++) {
				for (int dz = 0; dz < 12; dz++) {
					if ((dx * 31 + dz * 17) % 23 < 20) sc.set(dx, 0, dz, floor);
				}
			}
			// crumbled walls
			for (int dx = 0; dx < 12; dx++) {
				for (int dz = 0; dz < 12; dz++) {
					boolean edge = dx == 0 || dx == 11 || dz == 0 || dz == 11;
					if (!edge) continue;
					int h = 1 + (dx * 13 + dz * 29) % 4 / 2;
					for (int dy = 1; dy <= h; dy++) {
						if ((dx * 7 + dz * 11 + dy) % 5 != 0) sc.set(dx, dy, dz, wall);
					}
				}
			}
			// fallen pillar + tech debris
			sc.fill(4, 1, 5, 4, 3, 5, ModBlocks.QUANTUM_COMPUTER.getDefaultState());
			sc.set(6, 1, 6, ModBlocks.PORTAL_ENERGY_BLOCK.getDefaultState());
			sc.chest(2, 1, 2, lootFor(preset));
			// collapsed roof slab
			for (int dx = 3; dx < 8; dx++) {
				for (int dz = 3; dz < 8; dz++) {
					if ((dx + dz) % 3 != 0) sc.set(dx, 4, dz, wall);
				}
			}
		});
		s.paste(world, new BlockPos(x, y - 1, z), cp, null);
	}

	private static net.minecraft.registry.RegistryKey<net.minecraft.loot.LootTable> lootFor(RMChunkGenerator.Preset preset) {
		return switch (preset) {
			case POCKET -> net.minecraft.registry.RegistryKey.of(net.minecraft.registry.RegistryKeys.LOOT_TABLE,
					RickmortyId("pocket_ruins"));
			default -> net.minecraft.registry.RegistryKey.of(net.minecraft.registry.RegistryKeys.LOOT_TABLE,
					RickmortyId("alien_ruins"));
		};
	}

	private static net.minecraft.util.Identifier RickmortyId(String path) {
		return dev.benluvzbacon.rickmorty.RickMortyMod.id("chests/" + path);
	}
}
