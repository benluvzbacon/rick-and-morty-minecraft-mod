package dev.benluvzbacon.rickmorty.registry;

import dev.benluvzbacon.rickmorty.RickMortyMod;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class ModDamageTypes {
	public static final RegistryKey<DamageType> PORTAL_BOLT = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, RickMortyMod.id("portal_bolt"));
	public static final RegistryKey<DamageType> LASER = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, RickMortyMod.id("laser"));
	public static final RegistryKey<DamageType> PLASMA = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, RickMortyMod.id("plasma"));
	public static final RegistryKey<DamageType> ANOMALY = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, RickMortyMod.id("anomaly"));
	public static final RegistryKey<DamageType> DIMENSION_ERASE = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, RickMortyMod.id("dimension_erase"));

	public static final Identifier SHRINK_MODIFIER_ID = RickMortyMod.id("shrink");
}
