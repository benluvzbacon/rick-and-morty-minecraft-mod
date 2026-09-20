package dev.benluvzbacon.rickmorty.entity.ai;

import dev.benluvzbacon.rickmorty.entity.MortyEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.util.math.Box;

import java.util.EnumSet;
import java.util.List;

/**
 * Give Morty a blaster and he will (nervously) defend himself and whoever hurt
 * him... or his friends. No blaster means this goal does nothing and he panics instead.
 */
public class MortyWeaponGoal extends Goal {
	private final MortyEntity morty;
	private int scanCooldown;
	private int attackCooldown;

	public MortyWeaponGoal(MortyEntity morty) {
		this.morty = morty;
		this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
	}

	@Override
	public boolean canStart() {
		if (!morty.hasWeapon()) return false;
		return findTarget() != null;
	}

	@Override
	public boolean shouldContinue() {
		LivingEntity target = morty.getTarget();
		return morty.hasWeapon() && target != null && target.isAlive() && morty.distanceTo(target) < 24;
	}

	private LivingEntity findTarget() {
		LivingEntity current = morty.getTarget();
		if (current != null && current.isAlive() && morty.distanceTo(current) < 24) return current;
		if (scanCooldown > 0) {
			scanCooldown--;
			return null;
		}
		scanCooldown = 30;
		Box box = morty.getBoundingBox().expand(14, 6, 14);
		List<HostileEntity> hostiles = morty.getWorld().getEntitiesByClass(HostileEntity.class, box,
				e -> e.isAlive() && morty.canSee(e));
		// Morty only fights when something is threatening *him*.
		hostiles.removeIf(h -> h.getTarget() != morty && h.getAttacker() != morty);
		if (hostiles.isEmpty()) return null;
		hostiles.sort((a, b) -> Double.compare(morty.squaredDistanceTo(a), morty.squaredDistanceTo(b)));
		morty.setTarget(hostiles.get(0));
		return hostiles.get(0);
	}

	@Override
	public void tick() {
		LivingEntity target = findTarget();
		if (target == null) return;
		morty.getLookControl().lookAt(target, 30.0f, 30.0f);
		double dist = morty.distanceTo(target);
		if (dist > 10 || !morty.canSee(target)) {
			morty.getNavigation().startMovingTo(target, 1.25);
		} else {
			morty.getNavigation().stop();
		}
		if (attackCooldown > 0) attackCooldown--;
		if (morty.canSee(target) && attackCooldown <= 0 && dist <= 16) {
			morty.shootAt(target, 0);
			attackCooldown = 30 + morty.getRandom().nextInt(25);
		}
	}
}
