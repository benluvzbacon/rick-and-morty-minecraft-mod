package dev.benluvzbacon.rickmorty.registry;

import dev.benluvzbacon.rickmorty.RickMortyMod;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;

public class ModCreativeTab {
	public static void register() {
		ItemGroup group = FabricItemGroup.builder()
				.icon(() -> new ItemStack(ModItems.PORTAL_GUN))
				.displayName(Text.translatable("itemGroup.rickmorty"))
				.entries((context, entries) -> {
					// headline tech
					entries.add(ModItems.PORTAL_GUN);
					entries.add(ModBlocks.PORTAL_MACHINE);
					entries.add(ModBlocks.PORTAL_FLUID_TANK);
					entries.add(ModItems.PORTAL_FLUID_CANISTER);
					entries.add(ModItems.FILLED_PORTAL_FLUID_CANISTER);
					entries.add(ModItems.PORTAL_STABILIZER);
					entries.add(ModBlocks.RICK_WORKBENCH);
					entries.add(ModBlocks.QUANTUM_COMPUTER);
					entries.add(ModBlocks.ALIEN_REACTOR);
					entries.add(ModBlocks.PLUMBUS_MACHINE);
					entries.add(ModBlocks.MEESEEKS_BOX);
					entries.add(ModBlocks.INTERDIMENSIONAL_STORAGE);
					entries.add(ModBlocks.CONTAINMENT_CORE);
					// weapons
					entries.add(ModItems.PORTAL_BLASTER);
					entries.add(ModItems.LASER_GUN);
					entries.add(ModItems.PLASMA_LAUNCHER);
					entries.add(ModItems.DIMENSION_ERASER);
					entries.add(ModItems.ENERGY_CELL);
					entries.add(ModItems.PLASMA_CELL);
					// gadgets
					entries.add(ModItems.SHRINK_RAY);
					entries.add(ModItems.GRAPPLING_HOOK);
					entries.add(ModItems.FORCE_FIELD_GENERATOR);
					entries.add(ModItems.INTERDIMENSIONAL_SCANNER);
					entries.add(ModItems.JETPACK);
					// materials
					entries.add(ModItems.ALIEN_CRYSTAL);
					entries.add(ModItems.POCKET_CRYSTAL);
					entries.add(ModItems.ALIEN_ALLOY_INGOT);
					entries.add(ModItems.QUANTUM_CIRCUIT);
					entries.add(ModItems.ADVANCED_CIRCUITRY);
					entries.add(ModItems.PORTAL_GUN_FRAME);
					entries.add(ModItems.DARK_MATTER);
					entries.add(ModItems.DARK_MATTER_COMPONENT);
					entries.add(ModItems.INTERDIMENSIONAL_CRYSTAL);
					entries.add(ModItems.PLUMBUS);
					entries.add(ModItems.RIFT_SHARD);
					entries.add(ModItems.MUTANT_TISSUE);
					entries.add(ModItems.CHITIN);
					entries.add(ModItems.ROBOT_PLATING);
					// blocks & ores
					entries.add(ModBlocks.SCI_FI_METAL);
					entries.add(ModBlocks.CITADEL_METAL);
					entries.add(ModBlocks.LAB_GLASS);
					entries.add(ModBlocks.ALIEN_ROCK);
					entries.add(ModBlocks.POCKET_ROCK);
					entries.add(ModBlocks.CRYSTAL_CLUSTER);
					entries.add(ModBlocks.ALIEN_CRYSTAL_ORE);
					entries.add(ModBlocks.POCKET_CRYSTAL_ORE);
					entries.add(ModBlocks.PORTAL_ENERGY_BLOCK);
					entries.add(ModBlocks.DARK_MATTER_BLOCK);
					// consumables
					entries.add(ModItems.SIMPLE_WAFER);
					entries.add(ModItems.RICK_FLASK);
					entries.add(ModItems.CRONENBERG_STEW);
					// spawn eggs
					entries.add(ModItems.RICK_SPAWN_EGG);
					entries.add(ModItems.MORTY_SPAWN_EGG);
					entries.add(ModItems.MEESEEKS_SPAWN_EGG);
					entries.add(ModItems.CRONENBERG_SPAWN_EGG);
					entries.add(ModItems.ALIEN_CRAWLER_SPAWN_EGG);
					entries.add(ModItems.PARASITE_SPAWN_EGG);
					entries.add(ModItems.GAZORPIAN_SPAWN_EGG);
					entries.add(ModItems.SECURITY_BOT_SPAWN_EGG);
					entries.add(ModItems.PORTAL_ANOMALY_SPAWN_EGG);
				})
				.build();
		Registry.register(Registries.ITEM_GROUP, RickMortyMod.id("rickmorty"), group);
	}
}
