package dev.benluvzbacon.rickmorty.event;

import dev.benluvzbacon.rickmorty.registry.ModDamageTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * Timed shrink effects. Attribute modifiers don't expire on their own, so the server
 * removes them when the shrink duration is up.
 */
public final class ShrinkTracker {
	private ShrinkTracker() {}

	private static final int SHRINK_DURATION_TICKS = 30 * 20;
	private static final Map<UUID, Long> EXPIRY = new HashMap<>();

	public static void shrink(LivingEntity entity, long now) {
		var attr = entity.getAttributeInstance(EntityAttributes.GENERIC_SCALE);
		if (attr == null) return;
		var id = ModDamageTypes.SHRINK_MODIFIER_ID;
		attr.removeModifier(id);
		attr.addTemporaryModifier(new EntityAttributeModifier(id, -0.5,
				EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
		EXPIRY.put(entity.getUuid(), now + SHRINK_DURATION_TICKS);
	}

	public static void tick(MinecraftServer server) {
		if (EXPIRY.isEmpty()) return;
		long now = 0;
		for (ServerWorld world : server.getWorlds()) {
			now = Math.max(now, world.getTime());
		}
		if (now == 0) return;
		long finalNow = now;
		Iterator<Map.Entry<UUID, Long>> it = EXPIRY.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<UUID, Long> entry = it.next();
			if (entry.getValue() > finalNow) continue;
			it.remove();
			for (ServerWorld world : server.getWorlds()) {
				if (world.getEntity(entry.getKey()) instanceof LivingEntity living) {
					var attr = living.getAttributeInstance(EntityAttributes.GENERIC_SCALE);
					if (attr != null) {
						attr.removeModifier(ModDamageTypes.SHRINK_MODIFIER_ID);
					}
					break;
				}
			}
		}
	}
}
