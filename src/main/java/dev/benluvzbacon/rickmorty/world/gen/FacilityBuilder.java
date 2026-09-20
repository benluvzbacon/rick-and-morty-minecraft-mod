package dev.benluvzbacon.rickmorty.world.gen;

import dev.benluvzbacon.rickmorty.registry.ModBlocks;
import dev.benluvzbacon.rickmorty.RickMortyMod;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.chunk.Chunk;

/**
 * Experimental Facility: rare, contains the Containment Core that summons the
 * Interdimensional Abomination boss, plus decent loot.
 */
public final class FacilityBuilder {
	private FacilityBuilder() {}

	public static void tryBuild(StructureWorldAccess world, Chunk chunk, Random random, int chunksPerFacility) {
		if (random.nextInt(chunksPerFacility) != 0) return;
		ChunkPos cp = chunk.getPos();
		int x = cp.getStartX() + 2;
		int z = cp.getStartZ() + 2;
		int y = world.getTopY(Heightmap.Type.MOTION_BLOCKING, x + 6, z + 6);
		if (y <= world.getBottomY() + 2 || y > 190) return;

		SchematicWorld s = SchematicWorld.create("facility", 12, 9, 12, sc -> {
			var metal = ModBlocks.SCI_FI_METAL.getDefaultState();
			sc.fill(0, 0, 0, 11, 0, 11, metal);
			sc.shell(0, 1, 0, 11, 5, 11, metal);
			// ceiling
			sc.fill(0, 5, 0, 11, 5, 11, metal);
			// doorway (gap in wall, south side)
			for (int dy = 1; dy <= 3; dy++) {
				sc.set(5, dy, 0, net.minecraft.block.Blocks.AIR.getDefaultState());
				sc.set(6, dy, 0, net.minecraft.block.Blocks.AIR.getDefaultState());
			}
			// doorway lintel back on
			sc.set(5, 4, 0, metal);
			sc.set(6, 4, 0, metal);
			// windows (lab glass)
			for (int i = 2; i <= 3; i++) {
				sc.set(i, 2, 0, ModBlocks.LAB_GLASS.getDefaultState());
				sc.set(i, 3, 0, ModBlocks.LAB_GLASS.getDefaultState());
			}
			// interior: machinery + the core
			sc.set(3, 1, 5, ModBlocks.QUANTUM_COMPUTER.getDefaultState());
			sc.set(2, 1, 8, ModBlocks.PORTAL_FLUID_TANK.getDefaultState());
			sc.set(9, 1, 2, ModBlocks.ALIEN_REACTOR.getDefaultState());
			sc.set(5, 1, 5, ModBlocks.CONTAINMENT_CORE.getDefaultState());
			// lamp ring around the core
			sc.set(4, 1, 4, ModBlocks.PORTAL_ENERGY_BLOCK.getDefaultState());
			sc.set(6, 1, 4, ModBlocks.PORTAL_ENERGY_BLOCK.getDefaultState());
			sc.set(4, 1, 6, ModBlocks.PORTAL_ENERGY_BLOCK.getDefaultState());
			sc.set(6, 1, 6, ModBlocks.PORTAL_ENERGY_BLOCK.getDefaultState());
			sc.set(4, 2, 4, ModBlocks.LAB_GLASS.getDefaultState());
			// loot
			sc.chest(1, 1, 1, net.minecraft.registry.RegistryKey.of(
					net.minecraft.registry.RegistryKeys.LOOT_TABLE, RickMortyMod.id("chests/facility")));
			// antenna
			sc.fill(5, 6, 5, 5, 8, 5, ModBlocks.SCI_FI_METAL.getDefaultState());
			sc.set(5, 8, 5, ModBlocks.PORTAL_ENERGY_BLOCK.getDefaultState());
		});
		s.paste(world, new BlockPos(x, y, z), cp, null);
	}
}
