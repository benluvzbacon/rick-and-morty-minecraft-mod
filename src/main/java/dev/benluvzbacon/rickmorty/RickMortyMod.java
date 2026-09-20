package dev.benluvzbacon.rickmorty;

import dev.benluvzbacon.rickmorty.config.ModConfig;
import dev.benluvzbacon.rickmorty.network.ModNetworking;
import dev.benluvzbacon.rickmorty.registry.*;
import dev.benluvzbacon.rickmorty.event.ModEvents;
import dev.benluvzbacon.rickmorty.test.SelfTest;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RickMortyMod implements ModInitializer {
	public static final String MOD_ID = "rickmorty";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}

	@Override
	public void onInitialize() {
		ModConfig.load();
		ModSounds.register();
		ModBlocks.register();
		ModBlockEntities.register();
		ModEntities.register(); // entity types must exist before spawn eggs
		ModItems.register();
		ModStatusEffects.register();
		ModRecipes.register();
		ModScreenHandlers.register();
		ModCreativeTab.register();
		ModDimensions.register();
		ModNetworking.register();
		ModCommands.register();
		ModEvents.register();
		ModFeatures.register();
		SelfTest.register();
		LOGGER.info("[RickMorty] Initialized. Wubba lubba dub dub!");
	}
}
