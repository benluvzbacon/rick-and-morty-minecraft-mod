package dev.benluvzbacon.rickmorty.effect;

import dev.benluvzbacon.rickmorty.portal.TeleportUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.Difficulty;

/**
 * Standing in a dimensional anomaly too long makes reality... negotiable.
 * Occasionally reshuffles you a few meters. Harmless on Peaceful.
 */
public class DimensionalInstabilityEffect extends StatusEffect {
	public DimensionalInstabilityEffect() {
		super(StatusEffectCategory.NEUTRAL, 0x39ff88);
	}

	@Override
	public boolean canApplyUpdateEffect(int duration, int amplifier) {
		return duration % 60 == 0;
	}

	@Override
	public boolean applyUpdateEffect(ServerWorld world, LivingEntity entity, int amplifier) {
		if (world.getDifficulty() == Difficulty.PEACEFUL) return true;
		if (world.random.nextInt(3) != 0) return true;
		double dx = (world.random.nextDouble() - 0.5) * 6;
		double dz = (world.random.nextDouble() - 0.5) * 6;
		world.spawnParticles(ParticleTypes.PORTAL, entity.getX(), entity.getY() + 1, entity.getZ(),
				20, 0.4, 0.8, 0.4, 0.1);
		TeleportUtil.teleportSafe(entity, world, entity.getX() + dx, entity.getZ() + dz, entity.getYaw());
		return true;
	}
}
