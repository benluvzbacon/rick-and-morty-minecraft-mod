package dev.benluvzbacon.rickmorty.entity.ai;

import dev.benluvzbacon.rickmorty.entity.MeeseeksEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;

import java.util.EnumSet;
import java.util.List;

/**
 * Executes the Meeseeks' one true purpose: PROTECT follows the owner and engages
 * nearby hostiles; HUNT seeks and destroys the nearest monster; GATHER simply trails
 * the owner (the actual hoovering happens server-side in MeeseeksEntity).
 */
public class MeeseeksTaskGoal extends Goal {
	private final MeeseeksEntity meeseeks;
	private int scanCooldown;
	private int attackCooldown;

	public MeeseeksTaskGoal(MeeseeksEntity meeseeks) {
		this.meeseeks = meeseeks;
		this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
	}

	@Override
	public boolean canStart() {
		return true; // a Meeseeks always has a purpose
	}

	@Override
	public boolean shouldContinue() {
		return true;
	}

	@Override
	public void tick() {
		switch (meeseeks.getTask()) {
			case PROTECT -> tickProtect();
			case HUNT -> tickHunt();
			case GATHER -> tickGather();
		}
	}

	private void tickProtect() {
		PlayerEntity owner = meeseeks.getOwnerPlayer();
		if (owner != null && owner.isAlive() && meeseeks.distanceTo(owner) > 4.0f) {
			meeseeks.getNavigation().startMovingTo(owner, 1.2);
		}
		if (scanCooldown > 0) scanCooldown--;
		LivingEntity target = meeseeks.getTarget();
		if ((target == null || !target.isAlive()) && scanCooldown <= 0) {
			scanCooldown = 20;
			if (owner != null) {
				Box box = owner.getBoundingBox().expand(14, 6, 14);
				List<HostileEntity> hostiles = meeseeks.getWorld().getEntitiesByClass(HostileEntity.class, box,
						e -> e.isAlive() && e.getTarget() == owner);
				if (!hostiles.isEmpty()) {
					meeseeks.setTarget(hostiles.get(0));
					target = hostiles.get(0);
				}
			}
		}
		fightTarget();
	}

	private void tickHunt() {
		if (scanCooldown > 0) scanCooldown--;
		LivingEntity target = meeseeks.getTarget();
		if (target == null || !target.isAlive()) {
			if (scanCooldown <= 0) {
				scanCooldown = 25;
				List<HostileEntity> hostiles = meeseeks.getWorld().getEntitiesByClass(HostileEntity.class,
						meeseeks.getBoundingBox().expand(24, 8, 24), LivingEntity::isAlive);
				if (!hostiles.isEmpty()) {
					hostiles.sort((a, b) -> Double.compare(meeseeks.squaredDistanceTo(a), meeseeks.squaredDistanceTo(b)));
					meeseeks.setTarget(hostiles.get(0));
					target = hostiles.get(0);
				}
			}
		}
		fightTarget();
	}

	private void tickGather() {
		PlayerEntity owner = meeseeks.getOwnerPlayer();
		if (owner != null && owner.isAlive() && meeseeks.distanceTo(owner) > 5.0f) {
			meeseeks.getNavigation().startMovingTo(owner, 1.2);
		} else if (owner == null) {
			meeseeks.getNavigation().stop();
		}
	}

	private void fightTarget() {
		LivingEntity target = meeseeks.getTarget();
		if (target == null || !target.isAlive()) {
			if (attackCooldown > 0) attackCooldown--;
			return;
		}
		meeseeks.getLookControl().lookAt(target, 30f, 30f);
		if (meeseeks.distanceTo(target) > 2.2) {
			meeseeks.getNavigation().startMovingTo(target, 1.3);
		} else {
			meeseeks.getNavigation().stop();
			if (attackCooldown <= 0) {
				if (meeseeks.getWorld() instanceof net.minecraft.server.world.ServerWorld sw) {
					target.damage(sw, meeseeks.getDamageSources().mobAttack(meeseeks), 6.0f);
					meeseeks.swingHand(net.minecraft.util.Hand.MAIN_HAND);
				}
				attackCooldown = 20;
			}
		}
		if (attackCooldown > 0) attackCooldown--;
	}
}
