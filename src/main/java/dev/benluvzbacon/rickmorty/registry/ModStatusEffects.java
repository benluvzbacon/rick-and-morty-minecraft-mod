package dev.benluvzbacon.rickmorty.registry;

import dev.benluvzbacon.rickmorty.RickMortyMod;
import dev.benluvzbacon.rickmorty.effect.DimensionalInstabilityEffect;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ModStatusEffects {
	public static StatusEffect DIMENSIONAL_INSTABILITY;

	public static void register() {
		DIMENSIONAL_INSTABILITY = Registry.register(Registries.STATUS_EFFECT,
				RickMortyMod.id("dimensional_instability"), new DimensionalInstabilityEffect());
	}
}
