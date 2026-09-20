package dev.benluvzbacon.rickmorty.event;

import dev.benluvzbacon.rickmorty.dialogue.DialogueManager;
import dev.benluvzbacon.rickmorty.network.JetpackServer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public class ModEvents {
	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			DimensionalEventManager.tick(server);
			ShrinkTracker.tick(server);
		});
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			DialogueManager.evict(handler.player.getUuid());
			JetpackServer.clear(handler.player);
		});
	}
}
