package dev.benluvzbacon.rickmorty;

import net.fabricmc.api.ModInitializer;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RickMortyMod implements ModInitializer {
	public static final String MOD_ID = "rickmorty";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}

	public static final Item TEST_ITEM = new Item(new Item.Settings());

	@Override
	public void onInitialize() {
		Registry.register(Registries.ITEM, id("test_item"), TEST_ITEM);
		LOGGER.info("[RickMorty] Smoke test item registered. Wubba lubba dub dub!");
	}
}
