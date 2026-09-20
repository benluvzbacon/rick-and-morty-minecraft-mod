package dev.benluvzbacon.rickmorty.registry;

import dev.benluvzbacon.rickmorty.RickMortyMod;
import dev.benluvzbacon.rickmorty.block.*;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.block.TransparentBlock;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;

public class ModBlocks {
	public static Block SCI_FI_METAL;
	public static Block CITADEL_METAL;
	public static Block LAB_GLASS;
	public static Block ALIEN_ROCK;
	public static Block POCKET_ROCK;
	public static Block CRYSTAL_CLUSTER;
	public static Block ALIEN_CRYSTAL_ORE;
	public static Block POCKET_CRYSTAL_ORE;
	public static Block PORTAL_ENERGY_BLOCK;
	public static Block DARK_MATTER_BLOCK;
	public static Block RICK_WORKBENCH;
	public static Block PORTAL_MACHINE;
	public static Block PORTAL_FLUID_TANK;
	public static Block ALIEN_REACTOR;
	public static Block PLUMBUS_MACHINE;
	public static Block MEESEEKS_BOX;
	public static Block QUANTUM_COMPUTER;
	public static Block INTERDIMENSIONAL_STORAGE;
	public static Block PORTAL_BLOCK;
	public static Block UNSTABLE_PORTAL;
	public static Block CONTAINMENT_CORE;

	private static Block reg(String name, Block block, boolean withItem) {
		Registry.register(Registries.BLOCK, RickMortyMod.id(name), block);
		if (withItem) {
			Registry.register(Registries.ITEM, RickMortyMod.id(name), new BlockItem(block, new Item.Settings()));
		}
		return block;
	}

	private static AbstractBlock.Settings metal() {
		return AbstractBlock.Settings.create().instrument(NoteBlockInstrument.IRON_XYLOPHONE)
				.strength(4.0f, 8.0f).requiresTool().sounds(BlockSoundGroup.METAL).mapColor(MapColor.IRON_GRAY);
	}

	public static void register() {
		SCI_FI_METAL = reg("sci_fi_metal", new Block(metal()), true);
		CITADEL_METAL = reg("citadel_metal", new Block(metal().mapColor(MapColor.TERRACOTTA_CYAN)), true);
		LAB_GLASS = reg("lab_glass", new TransparentBlock(AbstractBlock.Settings.create()
				.strength(2.0f, 6.0f).sounds(BlockSoundGroup.GLASS).nonOpaque().mapColor(MapColor.WHITE)), true);
		ALIEN_ROCK = reg("alien_rock", new Block(AbstractBlock.Settings.create()
				.strength(3.0f, 9.0f).requiresTool().sounds(BlockSoundGroup.STONE).mapColor(MapColor.PURPLE)), true);
		POCKET_ROCK = reg("pocket_rock", new Block(AbstractBlock.Settings.create()
				.strength(3.0f, 9.0f).requiresTool().sounds(BlockSoundGroup.STONE).mapColor(MapColor.DULL_PINK)), true);
		CRYSTAL_CLUSTER = reg("crystal_cluster", new Block(AbstractBlock.Settings.create()
				.strength(1.5f, 4.0f).sounds(BlockSoundGroup.AMETHYST_BLOCK).luminance(s -> 11)
				.mapColor(MapColor.CYAN)), true);
		ALIEN_CRYSTAL_ORE = reg("alien_crystal_ore", new Block(AbstractBlock.Settings.copy(Blocks.DIAMOND_ORE)
				.mapColor(MapColor.PURPLE).luminance(s -> 5)), true);
		POCKET_CRYSTAL_ORE = reg("pocket_crystal_ore", new Block(AbstractBlock.Settings.copy(Blocks.DIAMOND_ORE)
				.mapColor(MapColor.DULL_PINK).luminance(s -> 7)), true);
		PORTAL_ENERGY_BLOCK = reg("portal_energy_block", new Block(AbstractBlock.Settings.create()
				.strength(2.0f, 6.0f).sounds(BlockSoundGroup.AMETHYST_BLOCK).luminance(s -> 13)
				.mapColor(MapColor.LIME)), true);
		DARK_MATTER_BLOCK = reg("dark_matter_block", new Block(AbstractBlock.Settings.create()
				.strength(6.0f, 1200.0f).requiresTool().sounds(BlockSoundGroup.STONE)
				.mapColor(MapColor.BLACK)), true);
		RICK_WORKBENCH = reg("rick_workbench", new RickWorkbenchBlock(metal().nonOpaque()), true);
		PORTAL_MACHINE = reg("portal_machine", new PortalStationBlock(metal().nonOpaque().luminance(s -> 8)), true);
		PORTAL_FLUID_TANK = reg("portal_fluid_tank", new FluidTankBlock(metal().nonOpaque()), true);
		ALIEN_REACTOR = reg("alien_reactor", new AlienReactorBlock(metal().nonOpaque().luminance(s -> 6)), true);
		PLUMBUS_MACHINE = reg("plumbus_machine", new PlumbusMachineBlock(metal().nonOpaque()), true);
		MEESEEKS_BOX = reg("meeseeks_box", new MeeseeksBoxBlock(metal().luminance(s -> 4)), true);
		QUANTUM_COMPUTER = reg("quantum_computer", new QuantumComputerBlock(metal().nonOpaque().luminance(s -> 7)), true);
		INTERDIMENSIONAL_STORAGE = reg("interdimensional_storage", new InterdimensionalStorageBlock(
				metal().mapColor(MapColor.MAGENTA)), true);
		PORTAL_BLOCK = reg("portal_block", new PortalBlock(AbstractBlock.Settings.create()
				.noCollision().nonOpaque().strength(-1.0f, 3600000.0f).dropsNothing()
				.luminance(s -> 12).sounds(BlockSoundGroup.AMETHYST_BLOCK)), false);
		UNSTABLE_PORTAL = reg("unstable_portal", new UnstablePortalBlock(AbstractBlock.Settings.create()
				.noCollision().nonOpaque().strength(-1.0f, 3600000.0f).dropsNothing()
				.luminance(s -> 10).sounds(BlockSoundGroup.AMETHYST_BLOCK)), false);
		CONTAINMENT_CORE = reg("containment_core", new ContainmentCoreBlock(metal().nonOpaque().luminance(s -> 9)), true);
	}
}
