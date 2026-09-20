package dev.benluvzbacon.rickmorty.registry;

import dev.benluvzbacon.rickmorty.RickMortyMod;
import dev.benluvzbacon.rickmorty.blockentity.*;
import dev.benluvzbacon.rickmorty.portal.PortalBlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ModBlockEntities {
	public static BlockEntityType<PortalBlockEntity> PORTAL_BLOCK_ENTITY;
	public static BlockEntityType<UnstablePortalBlockEntity> UNSTABLE_PORTAL_ENTITY;
	public static BlockEntityType<FluidTankBlockEntity> FLUID_TANK_ENTITY;
	public static BlockEntityType<AlienReactorBlockEntity> ALIEN_REACTOR_ENTITY;
	public static BlockEntityType<PlumbusMachineBlockEntity> PLUMBUS_MACHINE_ENTITY;
	public static BlockEntityType<MeeseeksBoxBlockEntity> MEESEEKS_BOX_ENTITY;
	public static BlockEntityType<RickWorkbenchBlockEntity> RICK_WORKBENCH_ENTITY;
	public static BlockEntityType<PortalStationBlockEntity> PORTAL_STATION_ENTITY;
	public static BlockEntityType<ContainmentCoreBlockEntity> CONTAINMENT_CORE_ENTITY;

	public static void register() {
		PORTAL_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, RickMortyMod.id("portal_block"),
				BlockEntityType.Builder.create(PortalBlockEntity::new, ModBlocks.PORTAL_BLOCK).build(null));
		UNSTABLE_PORTAL_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, RickMortyMod.id("unstable_portal"),
				BlockEntityType.Builder.create(UnstablePortalBlockEntity::new, ModBlocks.UNSTABLE_PORTAL).build(null));
		FLUID_TANK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, RickMortyMod.id("portal_fluid_tank"),
				BlockEntityType.Builder.create(FluidTankBlockEntity::new, ModBlocks.PORTAL_FLUID_TANK).build(null));
		ALIEN_REACTOR_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, RickMortyMod.id("alien_reactor"),
				BlockEntityType.Builder.create(AlienReactorBlockEntity::new, ModBlocks.ALIEN_REACTOR).build(null));
		PLUMBUS_MACHINE_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, RickMortyMod.id("plumbus_machine"),
				BlockEntityType.Builder.create(PlumbusMachineBlockEntity::new, ModBlocks.PLUMBUS_MACHINE).build(null));
		MEESEEKS_BOX_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, RickMortyMod.id("meeseeks_box"),
				BlockEntityType.Builder.create(MeeseeksBoxBlockEntity::new, ModBlocks.MEESEEKS_BOX).build(null));
		RICK_WORKBENCH_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, RickMortyMod.id("rick_workbench"),
				BlockEntityType.Builder.create(RickWorkbenchBlockEntity::new, ModBlocks.RICK_WORKBENCH).build(null));
		PORTAL_STATION_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, RickMortyMod.id("portal_machine"),
				BlockEntityType.Builder.create(PortalStationBlockEntity::new, ModBlocks.PORTAL_MACHINE).build(null));
		CONTAINMENT_CORE_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, RickMortyMod.id("containment_core"),
				BlockEntityType.Builder.create(ContainmentCoreBlockEntity::new, ModBlocks.CONTAINMENT_CORE).build(null));
	}
}
