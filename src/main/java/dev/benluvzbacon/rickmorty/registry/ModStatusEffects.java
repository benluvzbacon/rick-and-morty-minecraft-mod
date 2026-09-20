package dev.benluvzbacon.rickmorty.registry;

import dev.benluvzbacon.rickmorty.RickMortyMod;
import dev.benluvzbacon.rickmorty.effect.DimensionalInstabilityEffect;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;

public class ModStatusEffects {
	public static RegistryEntry<StatusEffect> DIMENSIONAL_INSTABILITY_ENTRY;
	public static StatusEffect DIMENSIONAL_INSTABILITY;

	public static void register() {
		DIMENSIONAL_INSTABILITY_ENTRY = Registry.registerReference(Registries.STATUS_EFFECT,
				RickMortyMod.id("dimensional_instability"), new DimensionalInstabilityEffect());
		DIMENSIONAL_INSTABILITY = DIMENSIONAL_INSTABILITY_ENTRY.value();
	}
}
