package dev.benluvzbacon.rickmorty.entity.ai;

import dev.benluvzbacon.rickmorty.entity.RickEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.EnumSet;
import java.util.List;

/**
 * Rick's combat: engages real threats with portal bolts. Keeps his distance, strafes,
 * and prioritizes whoever attacked him. He'd rather not fight — he only picks fights
 * with monsters (or things dumb enough to swing first).
 */
public class RickCombatGoal extends Goal {
	private final RickEntity rick;
	private int scanCooldown;
	private int attackCooldown;
	private int strafeDir = 1;
	private int strafeCooldown;

	public RickCombatGoal(RickEntity rick) {
		this.rick = rick;
		this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
	}

	@Override
	public boolean canStart() {
		return findTarget() != null;
	}

	@Override
	public boolean shouldContinue() {
		LivingEntity target = rick.getTarget();
		return target != null && target.isAlive() && rick.distanceTo(target) < 32;
	}

	@Override
	public void start() {
		rick.setAttacking(true);
	}

	@Override
	public void stop() {
		rick.setAttacking(false);
		rick.getNavigation().stop();
	}

	private LivingEntity findTarget() {
		LivingEntity current = rick.getTarget();
		if (current != null && current.isAlive() && rick.distanceTo(current) < 32) return current;
		if (scanCooldown > 0) {
			scanCooldown--;
			return null;
		}
		scanCooldown = 20;
		Box box = rick.getBoundingBox().expand(24, 8, 24);
		// nearest hostile monster first
		List<HostileEntity> hostiles = rick.getWorld().getEntitiesByClass(HostileEntity.class, box,
				e -> e.isAlive() && rick.canSee(e));
		if (!hostiles.isEmpty()) {
			hostiles.sort((a, b) -> Double.compare(rick.squaredDistanceTo(a), rick.squaredDistanceTo(b)));
			rick.setTarget(hostiles.get(0));
			return hostiles.get(0);
		}
		// revenge on players handled by RevengeGoal upstream
		return null;
	}

	@Override
	public void tick() {
		LivingEntity target = findTarget();
		if (target == null) return;
		rick.getLookControl().lookAt(target, 30.0f, 30.0f);

		double dist = rick.distanceTo(target);
		boolean canSee = rick.canSee(target);

		// movement: stay 7-14 blocks away, strafe
		if (dist > 14 || !canSee) {
			rick.getNavigation().startMovingTo(target, 1.35);
		} else {
			rick.getNavigation().stop();
			if (dist < 7) {
				Vec3d away = rick.getPos().subtract(target.getPos()).normalize().multiply(1.5);
				rick.getMoveControl().moveTo(rick.getX() + away.x, rick.getY(), rick.getZ() + away.z, 1.2);
			} else if (--strafeCooldown <= 0) {
				strafeCooldown = 40 + rick.getRandom().nextInt(50);
				strafeDir = -strafeDir;
			}
			if (dist >= 7 && dist <= 14) {
				Vec3d toTarget = target.getPos().subtract(rick.getPos()).normalize();
				Vec3d strafe = new Vec3d(-toTarget.z * strafeDir, 0, toTarget.x * strafeDir);
				rick.getMoveControl().moveTo(rick.getX() + strafe.x * 2, rick.getY(), rick.getZ() + strafe.z * 2, 0.9);
			}
		}

		if (attackCooldown > 0) attackCooldown--;
		if (canSee && attackCooldown <= 0 && dist <= 22) {
			rick.shootAt(target, 0);
			attackCooldown = 25 + rick.getRandom().nextInt(30);
		}
	}
}
