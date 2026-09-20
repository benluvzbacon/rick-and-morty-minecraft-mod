package dev.benluvzbacon.rickmorty.registry;

import dev.benluvzbacon.rickmorty.RickMortyMod;
import dev.benluvzbacon.rickmorty.item.*;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ModItems {
	// materials & components
	public static Item ALIEN_CRYSTAL;
	public static Item POCKET_CRYSTAL;
	public static Item ALIEN_ALLOY_INGOT;
	public static Item QUANTUM_CIRCUIT;
	public static Item ADVANCED_CIRCUITRY;
	public static Item PORTAL_GUN_FRAME;
	public static Item DARK_MATTER;
	public static Item DARK_MATTER_COMPONENT;
	public static Item INTERDIMENSIONAL_CRYSTAL;
	public static Item PLUMBUS;
	public static Item RIFT_SHARD;
	public static Item MUTANT_TISSUE;
	public static Item CHITIN;
	public static Item ROBOT_PLATING;
	public static Item ENERGY_CELL;
	public static Item PLASMA_CELL;
	public static Item PORTAL_FLUID_CANISTER;
	public static Item FILLED_PORTAL_FLUID_CANISTER;

	// weapons & gadgets
	public static Item PORTAL_GUN;
	public static Item PORTAL_BLASTER;
	public static Item LASER_GUN;
	public static Item PLASMA_LAUNCHER;
	public static Item DIMENSION_ERASER;
	public static Item SHRINK_RAY;
	public static Item GRAPPLING_HOOK;
	public static Item FORCE_FIELD_GENERATOR;
	public static Item INTERDIMENSIONAL_SCANNER;
	public static Item JETPACK;
	public static Item PORTAL_STABILIZER;

	// consumables
	public static Item SIMPLE_WAFER;
	public static Item RICK_FLASK;
	public static Item CRONENBERG_STEW;

	// spawn eggs
	public static Item RICK_SPAWN_EGG;
	public static Item MORTY_SPAWN_EGG;
	public static Item MEESEEKS_SPAWN_EGG;
	public static Item CRONENBERG_SPAWN_EGG;
	public static Item ALIEN_CRAWLER_SPAWN_EGG;
	public static Item PARASITE_SPAWN_EGG;
	public static Item GAZORPIAN_SPAWN_EGG;
	public static Item SECURITY_BOT_SPAWN_EGG;
	public static Item PORTAL_ANOMALY_SPAWN_EGG;

	private static Item reg(String name, Item item) {
		return Registry.register(Registries.ITEM, RickMortyMod.id(name), item);
	}

	public static void register() {
		ALIEN_CRYSTAL = reg("alien_crystal", new LoreItem(new Item.Settings(), 2));
		POCKET_CRYSTAL = reg("pocket_crystal", new LoreItem(new Item.Settings(), 2));
		ALIEN_ALLOY_INGOT = reg("alien_alloy_ingot", new LoreItem(new Item.Settings(), 2));
		QUANTUM_CIRCUIT = reg("quantum_circuit", new LoreItem(new Item.Settings(), 2));
		ADVANCED_CIRCUITRY = reg("advanced_circuitry", new LoreItem(new Item.Settings(), 2));
		PORTAL_GUN_FRAME = reg("portal_gun_frame", new LoreItem(new Item.Settings(), 2));
		DARK_MATTER = reg("dark_matter", new LoreItem(new Item.Settings(), 2));
		DARK_MATTER_COMPONENT = reg("dark_matter_component", new LoreItem(new Item.Settings(), 2));
		INTERDIMENSIONAL_CRYSTAL = reg("interdimensional_crystal", new LoreItem(new Item.Settings(), 3));
		PLUMBUS = reg("plumbus", new LoreItem(new Item.Settings(), 3));
		RIFT_SHARD = reg("rift_shard", new LoreItem(new Item.Settings(), 1));
		MUTANT_TISSUE = reg("mutant_tissue", new LoreItem(new Item.Settings(), 1));
		CHITIN = reg("chitin", new LoreItem(new Item.Settings(), 1));
		ROBOT_PLATING = reg("robot_plating", new LoreItem(new Item.Settings(), 1));
		ENERGY_CELL = reg("energy_cell", new LoreItem(new Item.Settings(), 1));
		PLASMA_CELL = reg("plasma_cell", new LoreItem(new Item.Settings(), 1));
		PORTAL_FLUID_CANISTER = reg("portal_fluid_canister", new LoreItem(new Item.Settings(), 1));
		FILLED_PORTAL_FLUID_CANISTER = reg("filled_portal_fluid_canister",
				new LoreItem(new Item.Settings().maxCount(16), 2));

		PORTAL_GUN = reg("portal_gun", new PortalGunItem(new Item.Settings()));
		PORTAL_BLASTER = reg("portal_blaster", new PortalBlasterItem(new Item.Settings()));
		LASER_GUN = reg("laser_gun", new LaserGunItem(new Item.Settings()));
		PLASMA_LAUNCHER = reg("plasma_launcher", new PlasmaLauncherItem(new Item.Settings()));
		DIMENSION_ERASER = reg("dimension_eraser", new DimensionEraserItem(new Item.Settings()));
		SHRINK_RAY = reg("shrink_ray", new ShrinkRayItem(new Item.Settings()));
		GRAPPLING_HOOK = reg("grappling_hook", new GrapplingHookItem(new Item.Settings()));
		FORCE_FIELD_GENERATOR = reg("force_field_generator", new ForceFieldItem(new Item.Settings()));
		INTERDIMENSIONAL_SCANNER = reg("interdimensional_scanner", new ScannerItem(new Item.Settings()));
		JETPACK = reg("jetpack", new JetpackItem(new Item.Settings()));
		PORTAL_STABILIZER = reg("portal_stabilizer", new LoreItem(new Item.Settings().maxCount(1), 3));

		SIMPLE_WAFER = reg("simple_wafer", new Item(new Item.Settings().food(
				new FoodComponent.Builder().nutrition(3).saturationModifier(0.4f).snack().build())));
		RICK_FLASK = reg("rick_flask", new RickFlaskItem(new Item.Settings().maxCount(16)));
		CRONENBERG_STEW = reg("cronenberg_stew", new Item(new Item.Settings().food(
				new FoodComponent.Builder().nutrition(8).saturationModifier(0.8f)
						.statusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 300, 0), 0.6f)
						.statusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 200, 0), 0.25f)
						.build())));

		RICK_SPAWN_EGG = reg("rick_spawn_egg", new SpawnEggItem(ModEntities.RICK, 0x9ad6ff, 0xf3f3f3, new Item.Settings()));
		MORTY_SPAWN_EGG = reg("morty_spawn_egg", new SpawnEggItem(ModEntities.MORTY, 0xffe08a, 0xffd23e, new Item.Settings()));
		MEESEEKS_SPAWN_EGG = reg("meeseeks_spawn_egg", new SpawnEggItem(ModEntities.MEESEEKS, 0x3fa9ff, 0x8fd4ff, new Item.Settings()));
		CRONENBERG_SPAWN_EGG = reg("cronenberg_spawn_egg", new SpawnEggItem(ModEntities.CRONENBERG_MUTANT, 0xb85c5c, 0x7a3b3b, new Item.Settings()));
		ALIEN_CRAWLER_SPAWN_EGG = reg("alien_crawler_spawn_egg", new SpawnEggItem(ModEntities.ALIEN_CRAWLER, 0x80c26a, 0x3e5a2e, new Item.Settings()));
		PARASITE_SPAWN_EGG = reg("parasite_spawn_egg", new SpawnEggItem(ModEntities.PARASITE, 0xc9a857, 0x6b5a2a, new Item.Settings()));
		GAZORPIAN_SPAWN_EGG = reg("gazorpian_spawn_egg", new SpawnEggItem(ModEntities.GAZORPIAN_BRUTE, 0x8a5b8a, 0x4a2a4a, new Item.Settings()));
		SECURITY_BOT_SPAWN_EGG = reg("security_bot_spawn_egg", new SpawnEggItem(ModEntities.SECURITY_BOT, 0x9aa5b1, 0x2f4858, new Item.Settings()));
		PORTAL_ANOMALY_SPAWN_EGG = reg("portal_anomaly_spawn_egg", new SpawnEggItem(ModEntities.PORTAL_ANOMALY, 0x7dffce, 0x1d5e4a, new Item.Settings()));
	}
}
