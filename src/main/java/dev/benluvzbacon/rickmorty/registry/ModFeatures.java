package dev.benluvzbacon.rickmorty.registry;

import dev.benluvzbacon.rickmorty.RickMortyMod;
import dev.benluvzbacon.rickmorty.world.feature.OverworldCrystalFeature;
import dev.benluvzbacon.rickmorty.world.feature.RicksLabFeature;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.PlacedFeature;

public class ModFeatures {
	public static final RicksLabFeature RICKS_LAB = new RicksLabFeature();
	public static final OverworldCrystalFeature OVERWORLD_CRYSTAL = new OverworldCrystalFeature();

	public static final RegistryKey<PlacedFeature> RICKS_LAB_PLACED = RegistryKey.of(
			RegistryKeys.PLACED_FEATURE, RickMortyMod.id("ricks_lab"));
	public static final RegistryKey<PlacedFeature> OVERWORLD_CRYSTAL_PLACED = RegistryKey.of(
			RegistryKeys.PLACED_FEATURE, RickMortyMod.id("alien_crystal_vein"));

	public static void register() {
		Registry.register(Registries.FEATURE, RickMortyMod.id("ricks_lab"), RICKS_LAB);
		Registry.register(Registries.FEATURE, RickMortyMod.id("alien_crystal_vein"), OVERWORLD_CRYSTAL);

		BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(),
				GenerationStep.Feature.SURFACE_STRUCTURES, RICKS_LAB_PLACED);
		BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(),
				GenerationStep.Feature.UNDERGROUND_ORES, OVERWORLD_CRYSTAL_PLACED);
	}
}
