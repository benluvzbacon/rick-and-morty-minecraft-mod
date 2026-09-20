package dev.benluvzbacon.rickmorty.world.gen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.benluvzbacon.rickmorty.registry.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.*;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.biome.source.FixedBiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.HeightContext;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.Blender;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import net.minecraft.world.gen.chunk.VerticalBlockSample;
import net.minecraft.world.gen.noise.NoiseConfig;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Self-contained chunk generator powering all four mod dimensions. Terrain is derived
 * from deterministic value noise keyed per dimension preset, so no vanilla noise
 * settings are required and generation stays fast and stable.
 */
public class RMChunkGenerator extends ChunkGenerator {
	public static final MapCodec<RMChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(instance ->
			instance.group(
					Biome.REGISTRY_CODEC.fieldOf("biome").forGetter(g -> g.biome),
					Codec.STRING.fieldOf("preset").forGetter(g -> g.preset)
			).apply(instance, RMChunkGenerator::new));

	public enum Preset { ALIEN, CITADEL, CRONENBERG, POCKET }

	private final RegistryEntry<Biome> biome;
	private final String preset;
	private final Preset presetEnum;
	private final long seed;

	public RMChunkGenerator(RegistryEntry<Biome> biome, String preset) {
		super(new FixedBiomeSource(biome));
		this.biome = biome;
		this.preset = preset;
		Preset p;
		try {
			p = Preset.valueOf(preset.toUpperCase());
		} catch (IllegalArgumentException e) {
			p = Preset.ALIEN;
		}
		this.presetEnum = p;
		this.seed = 0x5DEECE66DL ^ (long) preset.hashCode() * 1000003L;
	}

	@Override
	protected MapCodec<? extends ChunkGenerator> getCodec() {
		return CODEC;
	}

	public Preset preset() {
		return presetEnum;
	}

	// ---------------------------------------------------------------- heights

	private int heightAt(int x, int z) {
		switch (presetEnum) {
			case ALIEN -> {
				double base = SimpleNoise.fbm2(x, z, 0.0042, 4, seed);
				double h = 64 + (base * 2 - 1) * 20;
				double ridge = SimpleNoise.fbm2(x, z, 0.0014, 3, seed + 99);
				if (ridge > 0.60) {
					h += (ridge - 0.60) * 2.4 * 50;
				}
				return (int) Math.min(180, Math.max(30, h));
			}
			case CRONENBERG -> {
				double base = SimpleNoise.fbm2(x, z, 0.005, 5, seed + 7);
				double h = 66 + (base * 2 - 1) * 32;
				// rare organic spikes
				int cx = x >> 5, cz = z >> 5;
				double roll = SimpleNoise.hash2(cx, cz, seed + 31);
				if (roll < 0.12) {
					int sx = (cx << 5) + 8 + (int) (SimpleNoise.hash2(cx, cz, seed + 32) * 16);
					int sz = (cz << 5) + 8 + (int) (SimpleNoise.hash2(cx, cz, seed + 33) * 16);
					double d = Math.sqrt((x - sx) * (x - sx) + (z - sz) * (z - sz));
					if (d < 4) {
						h += (1 - d / 4) * 26;
					}
				}
				return (int) Math.min(180, Math.max(34, h));
			}
			case CITADEL -> {
				return 64;
			}
			case POCKET -> {
				// handled fully in 3D; return a plausible top for heightmaps
				return 216;
			}
			default -> {
				return 64;
			}
		}
	}

	private boolean inIsland(int x, int y, int z) {
		if (y < 32 || y > 216) return false;
		// guaranteed spawn island near origin
		double ox = x - 0.5, oz = z - 0.5;
		double dSpawn = Math.sqrt(ox * ox + oz * oz);
		if (dSpawn <= 11 && Math.abs(y - 64) <= 3 - dSpawn * 0.12) return true;
		// mega islands on a 128-block grid
		int cx = x >> 7, cz = z >> 7;
		double megaRoll = SimpleNoise.hash2(cx, cz, seed + 61);
		if (megaRoll < 0.45) {
			double icx = (cx << 7) + 32 + SimpleNoise.hash2(cx, cz, seed + 62) * 64;
			double icz = (cz << 7) + 32 + SimpleNoise.hash2(cx, cz, seed + 63) * 64;
			double icy = 80 + SimpleNoise.hash2(cx, cz, seed + 64) * 90;
			double rx = 14 + SimpleNoise.hash2(cx, cz, seed + 65) * 22;
			double ry = 5 + SimpleNoise.hash2(cx, cz, seed + 66) * 7;
			double dx = (x - icx) / rx, dy = (y - icy) / ry, dz = (z - icz) / rx;
			if (dx * dx + dy * dy + dz * dz < 1) return true;
		}
		// organic blobs
		double density = SimpleNoise.fbm3(x, y, z, 0.016, 3, seed + 21);
		return density > 0.70;
	}

	private BlockState stateAt(int x, int y, int z) {
		switch (presetEnum) {
			case POCKET -> {
				if (inIsland(x, y, z)) {
					return ModBlocks.POCKET_ROCK.getDefaultState();
				}
				return null;
			}
			case CITADEL -> {
				if (y < 0 || y >= 64) return null;
				if (y < 2) return randomBedrock(y);
				if (y < 60) return Blocks.STONE.getDefaultState();
				return ModBlocks.CITADEL_METAL.getDefaultState();
			}
			case ALIEN, CRONENBERG -> {
				int minY = getMinimumY();
				if (y < minY) return null;
				if (y < minY + 4) return randomBedrock(y - minY);
				int h = heightAt(x, z);
				if (y > h) return null;
				// caves
				double cave = SimpleNoise.fbm3(x, y, z, 0.045, 2, seed + 5);
				if (cave > 0.74 && y > minY + 5 && y < h - 4) return null;
				int depth = h - y;
				if (presetEnum == Preset.ALIEN) {
					return ModBlocks.ALIEN_ROCK.getDefaultState();
				} else {
					if (depth < 3) return Blocks.NETHERRACK.getDefaultState();
					return ModBlocks.ALIEN_ROCK.getDefaultState();
				}
			}
			default -> {
				return null;
			}
		}
	}

	private BlockState randomBedrock(int y) {
		if (y == 0) return Blocks.BEDROCK.getDefaultState();
		return SimpleNoise.hash2(7, y, seed) < (0.5 - y * 0.12) ? Blocks.BEDROCK.getDefaultState()
				: (presetEnum == Preset.CITADEL ? Blocks.STONE.getDefaultState() : ModBlocks.ALIEN_ROCK.getDefaultState());
	}

	// ---------------------------------------------------------------- required overrides

	@Override
	public void carve(ChunkRegion chunkRegion, long seed, NoiseConfig noiseConfig, BiomeAccess biomeAccess,
					  StructureAccessor structureAccessor, Chunk chunk) {
		// no carvers; caves are baked into the density function
	}

	@Override
	public void buildSurface(ChunkRegion region, StructureAccessor structures, NoiseConfig noiseConfig, Chunk chunk) {
		// surface states are produced directly by stateAt
	}

	@Override
	public CompletableFuture<Chunk> populateNoise(Executor executor, Blender blender, NoiseConfig noiseConfig,
												  StructureAccessor structureAccessor, Chunk chunk) {
		ChunkPos chunkPos = chunk.getPos();
		int startX = chunkPos.getStartX();
		int startZ = chunkPos.getStartZ();
		int bottom = getMinimumY();
		int top = bottom + getWorldHeight();
		BlockPos.Mutable pos = new BlockPos.Mutable();
		for (int lx = 0; lx < 16; lx++) {
			int wx = startX + lx;
			for (int lz = 0; lz < 16; lz++) {
				int wz = startZ + lz;
				for (int y = bottom; y < top; y++) {
					BlockState state = stateAt(wx, y, wz);
					if (state != null) {
						pos.set(wx, y, wz);
						chunk.setBlockState(pos, state, false);
					}
				}
			}
		}
		return CompletableFuture.completedFuture(chunk);
	}

	@Override
	public int getSeaLevel() {
		return 0;
	}

	@Override
	public int getMinimumY() {
		return switch (presetEnum) {
			case ALIEN, CRONENBERG -> -64;
			default -> 0;
		};
	}

	@Override
	public int getWorldHeight() {
		return switch (presetEnum) {
			case ALIEN, CRONENBERG -> 384;
			default -> 256;
		};
	}

	@Override
	public int getHeight(int x, int z, Heightmap.Type heightmap, HeightLimitView world, NoiseConfig noiseConfig) {
		int bottom = getMinimumY();
		int top = bottom + getWorldHeight();
		for (int y = top - 1; y >= bottom; y--) {
			if (stateAt(x, y, z) != null) return y + 1;
		}
		return bottom;
	}

	@Override
	public VerticalBlockSample getColumnSample(int x, int z, HeightLimitView world, NoiseConfig noiseConfig) {
		int bottom = getMinimumY();
		int height = getWorldHeight();
		BlockState[] states = new BlockState[height];
		for (int i = 0; i < height; i++) {
			BlockState s = stateAt(x, bottom + i, z);
			states[i] = s != null ? s : Blocks.AIR.getDefaultState();
		}
		return new VerticalBlockSample(bottom, states);
	}

	@Override
	public void appendDebugHudText(java.lang.StringBuilder text, NoiseConfig noiseConfig, BlockPos pos) {
		text.append("RickMorty[").append(preset).append(']');
	}

	@Override
	public void generateFeatures(StructureWorldAccess world, Chunk chunk, StructureAccessor structureAccessor) {
		RMFeatures.decorate(world, chunk, this);
	}
}
