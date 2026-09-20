package dev.benluvzbacon.rickmorty.world.gen;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.loot.LootTable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.StructureWorldAccess;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

/**
 * A code-built structure ("schematic" = map of relative pos -> block state) that can be
 * pasted into the world one chunk-slice at a time, so multi-chunk structures generate
 * seamlessly and deterministically during chunked generation.
 * Chests attached via {@link #loot} get loot tables assigned on placement.
 */
public class SchematicWorld {
	public final Map<BlockPos, BlockState> blocks = new HashMap<>();
	public final Map<BlockPos, RegistryKey<LootTable>> loot = new HashMap<>();
	public final int sizeX, sizeY, sizeZ;

	private SchematicWorld(int sx, int sy, int sz) {
		this.sizeX = sx;
		this.sizeY = sy;
		this.sizeZ = sz;
	}

	// small cache so repeated chunk generations don't rebuild the same schematic
	private static final LoadingCache<String, SchematicWorld> CACHE = CacheBuilder.newBuilder()
			.maximumSize(64).expireAfterAccess(5, TimeUnit.MINUTES)
			.build(new CacheLoader<>() {
				@Override
				public SchematicWorld load(String key) {
					throw new UnsupportedOperationException();
				}
			});

	@FunctionalInterface
	public interface Builder {
		void build(SchematicWorld s);
	}

	public static SchematicWorld create(String cacheKey, int sx, int sy, int sz, Builder builder) {
		SchematicWorld existing = CACHE.getIfPresent(cacheKey);
		if (existing != null) return existing;
		SchematicWorld s = new SchematicWorld(sx, sy, sz);
		builder.build(s);
		CACHE.put(cacheKey, s);
		return s;
	}

	public void set(int x, int y, int z, BlockState state) {
		blocks.put(new BlockPos(x, y, z), state);
	}

	public void fill(int x1, int y1, int z1, int x2, int y2, int z2, BlockState state) {
		for (int x = Math.min(x1, x2); x <= Math.max(x1, x2); x++) {
			for (int y = Math.min(y1, y2); y <= Math.max(y1, y2); y++) {
				for (int z = Math.min(z1, z2); z <= Math.max(z1, z2); z++) {
					set(x, y, z, state);
				}
			}
		}
	}

	/** hollow box shell */
	public void shell(int x1, int y1, int z1, int x2, int y2, int z2, BlockState state) {
		for (int x = Math.min(x1, x2); x <= Math.max(x1, x2); x++) {
			for (int y = Math.min(y1, y2); y <= Math.max(y1, y2); y++) {
				for (int z = Math.min(z1, z2); z <= Math.max(z1, z2); z++) {
					boolean edge = x == x1 || x == x2 || y == y1 || y == y2 || z == z1 || z == z2;
					if (edge) set(x, y, z, state);
				}
			}
		}
	}

	public void chest(int x, int y, int z, RegistryKey<LootTable> table) {
		set(x, y, z, Blocks.CHEST.getDefaultState());
		loot.put(new BlockPos(x, y, z), table);
	}

	/**
	 * Pastes the slice of this schematic that intersects the given chunk.
	 * origin = world position of schematic (0,0,0). Returns placed block count.
	 */
	public int paste(StructureWorldAccess world, BlockPos origin, ChunkPos chunk, BiConsumer<BlockPos, BlockState> visitor) {
		int placed = 0;
		BlockPos.Mutable p = new BlockPos.Mutable();
		for (Map.Entry<BlockPos, BlockState> e : blocks.entrySet()) {
			BlockPos rel = e.getKey();
			int wx = origin.getX() + rel.getX();
			int wy = origin.getY() + rel.getY();
			int wz = origin.getZ() + rel.getZ();
			if ((wx >> 4) != chunk.x || (wz >> 4) != chunk.z) continue;
			if (wy < world.getBottomY() || wy >= world.getTopY()) continue;
			p.set(wx, wy, wz);
			world.setBlockState(p, e.getValue(), 3);
			placed++;
			if (visitor != null) visitor.accept(p.toImmutable(), e.getValue());
			RegistryKey<LootTable> table = loot.get(rel);
			if (table != null) {
				BlockEntity be = world.getBlockEntity(p);
				if (be instanceof LootableContainerBlockEntity container) {
					container.setLootTable(table, world.getSeed() ^ p.toImmutable().asLong());
				}
			}
		}
		return placed;
	}

	public BlockPos boundsMax() {
		return new BlockPos(sizeX, sizeY, sizeZ);
	}
}
