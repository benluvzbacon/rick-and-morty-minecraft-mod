package dev.benluvzbacon.rickmorty.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.benluvzbacon.rickmorty.RickMortyMod;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Simple JSON configuration, no external dependencies.
 */
public class ModConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static ModConfig INSTANCE;

	// Portal technology
	public int portalFluidPerPortal = 100;
	public int portalGunCapacity = 1000;
	public int portalCooldownTicks = 30;
	public int portalLifetimeTicks = 1200; // 60 s
	public int tankCapacity = 8000;
	// Spawning
	public double mobSpawnMultiplier = 1.0;
	public int rickSpawnWeightOverworld = 1;
	// Events
	public boolean enableEvents = true;
	public double eventFrequencyMultiplier = 1.0;
	// Entities
	public int meeseeksLifetimeSeconds = 90;
	public double rickHealth = 60.0;
	// Experimental
	public boolean enableDimensionEraser = true;

	public static ModConfig get() {
		if (INSTANCE == null) {
			load();
		}
		return INSTANCE;
	}

	public static synchronized void load() {
		Path path = FabricLoader.getInstance().getConfigDir().resolve("rickmorty.json");
		if (Files.exists(path)) {
			try {
				INSTANCE = GSON.fromJson(Files.readString(path), ModConfig.class);
				if (INSTANCE == null) INSTANCE = new ModConfig();
			} catch (Exception e) {
				RickMortyMod.LOGGER.warn("[RickMorty] Failed to read config, using defaults: {}", e.getMessage());
				INSTANCE = new ModConfig();
			}
		} else {
			INSTANCE = new ModConfig();
			save();
		}
	}

	public static synchronized void save() {
		Path path = FabricLoader.getInstance().getConfigDir().resolve("rickmorty.json");
		try {
			Files.createDirectories(path.getParent());
			Files.writeString(path, GSON.toJson(INSTANCE == null ? new ModConfig() : INSTANCE));
		} catch (IOException e) {
			RickMortyMod.LOGGER.warn("[RickMorty] Failed to save config: {}", e.getMessage());
		}
	}
}
