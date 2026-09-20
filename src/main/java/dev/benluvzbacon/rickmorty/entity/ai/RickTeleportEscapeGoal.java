package dev.benluvzbacon.rickmorty.entity.ai;

import dev.benluvzbacon.rickmorty.entity.RickEntity;
import dev.benluvzbacon.rickmorty.portal.TeleportUtil;
import dev.benluvzbacon.rickmorty.registry.ModSounds;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;

import java.util.EnumSet;

/**
 * When Rick gets cornered (low health and recently hurt), he blinks out of danger with
 * his portal gun, gains a brief window of nanobot repair, and taunts whoever almost
 * got him.
 */
public class RickTeleportEscapeGoal extends Goal {
	private final RickEntity rick;
	private int cooldown;
	private float lastHealth;

	public RickTeleportEscapeGoal(RickEntity rick) {
		this.rick = rick;
		this.lastHealth = rick.getMaxHealth();
		this.setControls(EnumSet.of(Control.MOVE));
	}

	@Override
	public boolean canStart() {
		if (cooldown > 0) {
			cooldown--;
			return false;
		}
		boolean hurtRecently = rick.getRecentDamageSource() != null
				&& rick.age - rick.getLastAttackedTime() < 60;
		return rick.getHealth() < rick.getMaxHealth() * 0.4f && hurtRecently;
	}

	@Override
	public void start() {
		if (!(rick.getWorld() instanceof ServerWorld world)) return;
		double tx = rick.getX() + (rick.getRandom().nextDouble() - 0.5) * 40;
		double tz = rick.getZ() + (rick.getRandom().nextDouble() - 0.5) * 40;
		world.spawnParticles(ParticleTypes.PORTAL, rick.getX(), rick.getY() + 1, rick.getZ(),
				60, 0.5, 1.0, 0.5, 0.3);
		boolean ok = TeleportUtil.teleportSafe(rick, world, tx, tz, rick.getYaw());
		if (ok) {
			world.playSound(null, rick.getBlockPos(), ModSounds.RICK_TELEPORT, net.minecraft.sound.SoundCategory.NEUTRAL, 1.0f, 1.0f);
			world.spawnParticles(ParticleTypes.PORTAL, rick.getX(), rick.getY() + 1, rick.getZ(),
					60, 0.5, 1.0, 0.5, 0.3);
			rick.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 60, 1, true, false));
			cooldown = 600; // 30 s
			rick.bark("teleport", 2);
			rick.getNavigation().stop();
		} else {
			cooldown = 100;
		}
	}

	@Override
	public boolean shouldContinue() {
		return false;
	}
}
