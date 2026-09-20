package dev.benluvzbacon.rickmorty.registry;

import dev.benluvzbacon.rickmorty.RickMortyMod;
import dev.benluvzbacon.rickmorty.config.ModConfig;
import dev.benluvzbacon.rickmorty.entity.*;
import dev.benluvzbacon.rickmorty.entity.boss.AbominationEntity;
import dev.benluvzbacon.rickmorty.entity.projectile.EnergyBoltEntity;
import dev.benluvzbacon.rickmorty.entity.projectile.GrapnelEntity;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnLocationTypes;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.world.Heightmap;

public class ModEntities {
	public static EntityType<RickEntity> RICK;
	public static EntityType<MortyEntity> MORTY;
	public static EntityType<MeeseeksEntity> MEESEEKS;
	public static EntityType<CronenbergEntity> CRONENBERG_MUTANT;
	public static EntityType<AlienCrawlerEntity> ALIEN_CRAWLER;
	public static EntityType<ParasiteEntity> PARASITE;
	public static EntityType<GazorpianEntity> GAZORPIAN_BRUTE;
	public static EntityType<SecurityBotEntity> SECURITY_BOT;
	public static EntityType<PortalAnomalyEntity> PORTAL_ANOMALY;
	public static EntityType<AbominationEntity> INTERDIMENSIONAL_ABOMINATION;
	public static EntityType<EnergyBoltEntity> ENERGY_BOLT;
	public static EntityType<GrapnelEntity> GRAPNEL;

	private static <T extends Entity> EntityType<T> reg(String name, EntityType.Builder<T> builder) {
		Identifier id = RickMortyMod.id(name);
		return Registry.register(Registries.ENTITY_TYPE, id, builder.build(id.toString()));
	}

	public static void register() {
		RICK = reg("rick", EntityType.Builder.create(RickEntity::new, SpawnGroup.CREATURE)
				.dimensions(0.6f, 1.9f).maxTrackingRange(8));
		MORTY = reg("morty", EntityType.Builder.create(MortyEntity::new, SpawnGroup.CREATURE)
				.dimensions(0.55f, 1.5f).maxTrackingRange(8));
		MEESEEKS = reg("meeseeks", EntityType.Builder.create(MeeseeksEntity::new, SpawnGroup.MISC)
				.dimensions(0.6f, 2.0f).maxTrackingRange(8));
		CRONENBERG_MUTANT = reg("cronenberg_mutant", EntityType.Builder.create(CronenbergEntity::new, SpawnGroup.MONSTER)
				.dimensions(0.9f, 2.1f).maxTrackingRange(8));
		ALIEN_CRAWLER = reg("alien_crawler", EntityType.Builder.create(AlienCrawlerEntity::new, SpawnGroup.MONSTER)
				.dimensions(0.7f, 0.9f).maxTrackingRange(8));
		PARASITE = reg("parasite", EntityType.Builder.create(ParasiteEntity::new, SpawnGroup.MONSTER)
				.dimensions(0.45f, 0.45f).maxTrackingRange(8));
		GAZORPIAN_BRUTE = reg("gazorpian_brute", EntityType.Builder.create(GazorpianEntity::new, SpawnGroup.MONSTER)
				.dimensions(1.1f, 2.4f).maxTrackingRange(8));
		SECURITY_BOT = reg("security_bot", EntityType.Builder.create(SecurityBotEntity::new, SpawnGroup.CREATURE)
				.dimensions(0.7f, 1.6f).maxTrackingRange(8));
		PORTAL_ANOMALY = reg("portal_anomaly", EntityType.Builder.create(PortalAnomalyEntity::new, SpawnGroup.MONSTER)
				.dimensions(0.8f, 0.8f).maxTrackingRange(8));
		INTERDIMENSIONAL_ABOMINATION = reg("interdimensional_abomination",
				EntityType.Builder.create(AbominationEntity::new, SpawnGroup.MONSTER)
						.dimensions(1.8f, 3.4f).maxTrackingRange(10).makeFireImmune());
		ENERGY_BOLT = reg("energy_bolt", EntityType.Builder.<EnergyBoltEntity>create(EnergyBoltEntity::new, SpawnGroup.MISC)
				.dimensions(0.35f, 0.35f).maxTrackingRange(4).trackingTickInterval(10).disableSaving());
		GRAPNEL = reg("grapnel", EntityType.Builder.<GrapnelEntity>create(GrapnelEntity::new, SpawnGroup.MISC)
				.dimensions(0.4f, 0.4f).maxTrackingRange(4).trackingTickInterval(2).disableSaving());

		// attributes
		FabricDefaultAttributeRegistry.register(RICK, RickEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(MORTY, MortyEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(MEESEEKS, MeeseeksEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(CRONENBERG_MUTANT, CronenbergEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(ALIEN_CRAWLER, AlienCrawlerEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(PARASITE, ParasiteEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(GAZORPIAN_BRUTE, GazorpianEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(SECURITY_BOT, SecurityBotEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(PORTAL_ANOMALY, PortalAnomalyEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(INTERDIMENSIONAL_ABOMINATION, AbominationEntity.createAttributes());

		// spawn rules
		SpawnRestriction.register(RICK, SpawnLocationTypes.ON_GROUND,
				Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, PathAwareEntity::canMobSpawn);
		SpawnRestriction.register(MORTY, SpawnLocationTypes.ON_GROUND,
				Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, PathAwareEntity::canMobSpawn);
		SpawnRestriction.register(CRONENBERG_MUTANT, SpawnLocationTypes.ON_GROUND,
				Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, HostileEntity::canSpawnInDark);
		SpawnRestriction.register(ALIEN_CRAWLER, SpawnLocationTypes.ON_GROUND,
				Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, HostileEntity::canSpawnInDark);
		SpawnRestriction.register(PARASITE, SpawnLocationTypes.ON_GROUND,
				Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, HostileEntity::canSpawnInDark);
		SpawnRestriction.register(GAZORPIAN_BRUTE, SpawnLocationTypes.ON_GROUND,
				Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, HostileEntity::canSpawnInDark);
		SpawnRestriction.register(SECURITY_BOT, SpawnLocationTypes.ON_GROUND,
				Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, PathAwareEntity::canMobSpawn);
		SpawnRestriction.register(PORTAL_ANOMALY, SpawnLocationTypes.IN_AIR,
				Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, MobEntity::canMobSpawn);

		// biome placement (config-scaled weights)
		int rickWeight = Math.max(1, (int) (ModConfig.get().rickSpawnWeightOverworld * ModConfig.get().mobSpawnMultiplier));
		BiomeModifications.addSpawn(BiomeSelectors.foundInOverworld().and(BiomeSelectors.tag(
						net.minecraft.registry.tag.BiomeTags.IS_FOREST)),
				SpawnGroup.CREATURE, RICK, rickWeight, 1, 1);
		BiomeModifications.addSpawn(BiomeSelectors.foundInOverworld().and(BiomeSelectors.tag(
						net.minecraft.registry.tag.BiomeTags.IS_FOREST)),
				SpawnGroup.CREATURE, MORTY, Math.max(1, rickWeight * 2), 1, 1);

		addDimSpawn(ModDimensions.ALIEN_WORLD, SpawnGroup.MONSTER, ALIEN_CRAWLER, 60, 1, 3);
		addDimSpawn(ModDimensions.ALIEN_WORLD, SpawnGroup.MONSTER, PARASITE, 40, 1, 4);
		addDimSpawn(ModDimensions.ALIEN_WORLD, SpawnGroup.MONSTER, GAZORPIAN_BRUTE, 12, 1, 1);
		addDimSpawn(ModDimensions.ALIEN_WORLD, SpawnGroup.MONSTER, PORTAL_ANOMALY, 6, 1, 1);
		addDimSpawn(ModDimensions.CITADEL_WORLD, SpawnGroup.CREATURE, RICK, 10, 1, 2);
		addDimSpawn(ModDimensions.CITADEL_WORLD, SpawnGroup.CREATURE, MORTY, 14, 1, 3);
		addDimSpawn(ModDimensions.CITADEL_WORLD, SpawnGroup.CREATURE, SECURITY_BOT, 10, 1, 2);
		addDimSpawn(ModDimensions.CRONENBERG_WORLD, SpawnGroup.MONSTER, CRONENBERG_MUTANT, 55, 1, 3);
		addDimSpawn(ModDimensions.CRONENBERG_WORLD, SpawnGroup.MONSTER, PARASITE, 35, 1, 3);
		addDimSpawn(ModDimensions.POCKET_WORLD, SpawnGroup.MONSTER, PORTAL_ANOMALY, 70, 1, 2);
		addDimSpawn(ModDimensions.POCKET_WORLD, SpawnGroup.MONSTER, CRONENBERG_MUTANT, 10, 1, 1);
	}

	private static <T extends MobEntity> void addDimSpawn(net.minecraft.registry.RegistryKey<net.minecraft.world.World> dimKey,
														  SpawnGroup group, EntityType<T> type, int weight, int min, int max) {
		int scaled = Math.max(1, (int) (weight * ModConfig.get().mobSpawnMultiplier));
		String tagPath = dimKey.getValue().getPath() + "_spawnable";
		BiomeModifications.addSpawn(BiomeSelectors.includeForKey(
						net.minecraft.registry.RegistryKey.of(net.minecraft.registry.RegistryKeys.BIOME,
								biomeFor(dimKey))),
				group, type, scaled, min, max);
	}

	private static Identifier biomeFor(net.minecraft.registry.RegistryKey<net.minecraft.world.World> dimKey) {
		return switch (dimKey.getValue().getPath()) {
			case "alien_planet" -> RickMortyMod.id("alien_fields");
			case "citadel" -> RickMortyMod.id("citadel_plaza");
			case "cronenberg_world" -> RickMortyMod.id("cronenberg_wastes");
			default -> RickMortyMod.id("pocket_void");
		};
	}
}
