package dev.benluvzbacon.rickmorty.registry;

import dev.benluvzbacon.rickmorty.RickMortyMod;
import dev.benluvzbacon.rickmorty.world.gen.RMChunkGenerator;
import net.minecraft.registry.Registry;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.World;

import java.util.List;

public class ModDimensions {
	public static final RegistryKey<World> ALIEN_WORLD = RegistryKey.of(RegistryKeys.WORLD, RickMortyMod.id("alien_planet"));
	public static final RegistryKey<World> CITADEL_WORLD = RegistryKey.of(RegistryKeys.WORLD, RickMortyMod.id("citadel"));
	public static final RegistryKey<World> CRONENBERG_WORLD = RegistryKey.of(RegistryKeys.WORLD, RickMortyMod.id("cronenberg_world"));
	public static final RegistryKey<World> POCKET_WORLD = RegistryKey.of(RegistryKeys.WORLD, RickMortyMod.id("pocket_dimension"));

	/** Order used by the portal gun dimension selector (overworld is index 0). */
	public static final List<RegistryKey<World>> TRAVELABLE = List.of(
			World.OVERWORLD, ALIEN_WORLD, CITADEL_WORLD, CRONENBERG_WORLD, POCKET_WORLD);

	public static void register() {
		Registry.register(Registries.CHUNK_GENERATOR, RickMortyMod.id("rm_generator"), RMChunkGenerator.CODEC);
	}
}
