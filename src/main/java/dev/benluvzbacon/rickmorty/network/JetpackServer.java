package dev.benluvzbacon.rickmorty.network;

import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Server-authoritative jetpack activation state (toggled via the jetpack keybind ->
 * C2S packet). Only an active, fueled, chest-equipped jetpack actually lifts you.
 */
public final class JetpackServer {
	private JetpackServer() {}

	private static final Set<UUID> ACTIVE = new HashSet<>();

	public static boolean isActive(ServerPlayerEntity player) {
		return ACTIVE.contains(player.getUuid());
	}

	public static void toggle(ServerPlayerEntity player) {
		UUID id = player.getUuid();
		if (!ACTIVE.remove(id)) {
			ACTIVE.add(id);
		}
	}

	public static void clear(ServerPlayerEntity player) {
		ACTIVE.remove(player.getUuid());
	}
}
