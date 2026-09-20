package dev.benluvzbacon.rickmorty.dialogue;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Contextual NPC dialogue with per-player, per-trigger cooldowns so nobody gets spammed.
 */
public final class DialogueManager {
	private DialogueManager() {}

	private static final Map<String, Long> LAST_SAID = new HashMap<>();

	public static void say(ServerPlayerEntity player, String speaker, Formatting color,
						   String triggerKey, int variantCount, int cooldownTicks, net.minecraft.util.math.random.Random random) {
		String key = player.getUuid() + ":" + speaker + ":" + triggerKey;
		long now = player.getWorld().getTime();
		Long last = LAST_SAID.get(key);
		if (last != null && now - last < cooldownTicks) return;
		LAST_SAID.put(key, now);
		int variant = variantCount <= 1 ? 1 : 1 + random.nextInt(variantCount);
		Text line = Text.translatable("dialogue.rickmorty." + speaker + "." + triggerKey + "." + variant);
		player.sendMessage(Text.literal("<").formatted(Formatting.GRAY)
				.append(Text.translatable("entity.rickmorty." + speaker).formatted(color))
				.append(Text.literal("> ").formatted(Formatting.GRAY))
				.append(line.copy().formatted(Formatting.WHITE)), false);
	}

	/** Clears a player's memory (on logout), keeping the map small. */
	public static void evict(UUID playerId) {
		LAST_SAID.keySet().removeIf(k -> k.startsWith(playerId.toString()));
	}
}
