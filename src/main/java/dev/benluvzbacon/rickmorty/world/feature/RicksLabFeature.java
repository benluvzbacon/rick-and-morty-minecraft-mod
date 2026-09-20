package dev.benluvzbacon.rickmorty.world.feature;

import dev.benluvzbacon.rickmorty.RickMortyMod;
import dev.benluvzbacon.rickmorty.registry.ModBlocks;
import dev.benluvzbacon.rickmorty.world.gen.SchematicWorld;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

/**
 * Rick's Laboratory, hidden away in the overworld. Contains the full starter tech
 * line: workbench, quantum computer, portal fluid tank and (rarely) a portal station,
 * plus lab loot. Spawns roughly once per 24x24 chunk region.
 */
public class RicksLabFeature extends Feature<DefaultFeatureConfig> {
	private static final int CELL_CHUNKS = 24;

	public RicksLabFeature() {
		super(DefaultFeatureConfig.CODEC);
	}

	@Override
	public boolean generate(FeatureContext<DefaultFeatureConfig> context) {
		StructureWorldAccess world = context.getWorld();
		BlockPos origin = context.getOrigin();
		ChunkPos chunk = new ChunkPos(origin);

		long seed = world.getSeed();
		int cellX = Math.floorDiv(chunk.x, CELL_CHUNKS);
		int cellZ = Math.floorDiv(chunk.z, CELL_CHUNKS);
		long h = seed ^ (cellX * 341873128712L) ^ (cellZ * 132897987541L) ^ 0x51AB3L;
		h ^= h >>> 33;
		h *= -49064778989728563L;
		h ^= h >>> 33;
		// ~60% of cells get a lab
		if ((h & 1023) >= 614) return false;
		int anchorX = cellX * CELL_CHUNKS + (int) ((h >>> 10) % CELL_CHUNKS);
		int anchorZ = cellZ * CELL_CHUNKS + (int) ((h >>> 20) % CELL_CHUNKS);
		if (chunk.x != anchorX || chunk.z != anchorZ) return false;

		// pick reasonably dry land
		int cx = chunk.getStartX() + 1;
		int cz = chunk.getStartZ() + 1;
		int y = world.getTopY(Heightmap.Type.MOTION_BLOCKING, cx + 7, cz + 7);
		int min = world.getBottomY() + 3;
		if (y <= min || y > 120) return false;
		if (world.getBlockState(new BlockPos(cx + 7, y - 1, cz + 7)).getFluidState().isEmpty() == false) return false;

		long variant = (h >>> 30) & 1;
		SchematicWorld lab = buildLab(variant);
		lab.paste(world, new BlockPos(cx, y, cz), chunk, null);
		RickMortyMod.LOGGER.debug("[RickMorty] Rick's Lab generated at {},{}", cx, cz);
		return true;
	}

	private static SchematicWorld buildLab(long variant) {
		return SchematicWorld.create("ricks_lab_" + variant, 14, 10, 14, sc -> {
			var metal = ModBlocks.SCI_FI_METAL.getDefaultState();
			var glass = ModBlocks.LAB_GLASS.getDefaultState();
			// foundation + pad
			sc.fill(0, -2, 0, 13, -1, 13, metal);
			sc.fill(0, 0, 0, 13, 0, 13, metal);
			// walls
			for (int x = 0; x < 14; x++) {
				for (int z = 0; z < 14; z++) {
					boolean edge = x == 0 || x == 13 || z == 0 || z == 13;
					if (!edge) continue;
					for (int y = 1; y <= 4; y++) {
						boolean window = (y >= 2 && y <= 3) && ((x + z) % 4 < 2) && x != 0;
						sc.set(x, y, z, window ? glass : metal);
					}
				}
			}
			// roof with skylight
			for (int x = 0; x < 14; x++) {
				for (int z = 0; z < 14; z++) {
					boolean skylight = x >= 4 && x <= 9 && z >= 4 && z <= 9;
					sc.set(x, 5, z, skylight && variant == 0 ? glass : metal);
				}
			}
			// antenna + beacon
			sc.fill(6, 6, 6, 6, 8, 6, metal);
			sc.set(6, 9, 6, ModBlocks.PORTAL_ENERGY_BLOCK.getDefaultState());
			// door on south wall
			for (int dy = 1; dy <= 3; dy++) {
				sc.set(6, dy, 0, Blocks.AIR.getDefaultState());
				sc.set(7, dy, 0, Blocks.AIR.getDefaultState());
			}
			sc.set(6, 4, 0, metal);
			sc.set(7, 4, 0, metal);
			// tech islands
			sc.set(2, 1, 2, ModBlocks.RICK_WORKBENCH.getDefaultState());
			sc.set(1, 1, 2, ModBlocks.QUANTUM_COMPUTER.getDefaultState());
			sc.set(2, 1, 1, ModBlocks.PORTAL_FLUID_TANK.getDefaultState());
			sc.set(11, 1, 2, ModBlocks.ALIEN_REACTOR.getDefaultState());
			sc.set(11, 1, 11, ModBlocks.PORTAL_MACHINE.getDefaultState());
			// central experiment table
			sc.fill(5, 1, 6, 8, 1, 7, metal);
			sc.set(5, 2, 6, glass);
			sc.set(8, 2, 7, ModBlocks.PORTAL_ENERGY_BLOCK.getDefaultState());
			// storage wall
			sc.chest(1, 1, 6, lootTable());
			sc.chest(1, 1, 8, lootTable());
			sc.set(1, 1, 10, Blocks.BARREL.getDefaultState());
			// meeseeks box in the corner for flavor
			if (variant == 1) sc.set(11, 1, 6, ModBlocks.MEESEEKS_BOX.getDefaultState());
			// lamps
			sc.set(3, 4, 3, ModBlocks.PORTAL_ENERGY_BLOCK.getDefaultState());
			sc.set(10, 4, 3, ModBlocks.PORTAL_ENERGY_BLOCK.getDefaultState());
			sc.set(3, 4, 10, ModBlocks.PORTAL_ENERGY_BLOCK.getDefaultState());
			sc.set(10, 4, 10, ModBlocks.PORTAL_ENERGY_BLOCK.getDefaultState());
		});
	}

	private static net.minecraft.registry.RegistryKey<net.minecraft.loot.LootTable> lootTable() {
		return net.minecraft.registry.RegistryKey.of(net.minecraft.registry.RegistryKeys.LOOT_TABLE,
				RickMortyMod.id("chests/ricks_lab"));
	}
}
