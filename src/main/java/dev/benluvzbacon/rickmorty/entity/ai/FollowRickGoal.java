package dev.benluvzbacon.rickmorty.entity.ai;

import dev.benluvzbacon.rickmorty.entity.RickEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.PathAwareEntity;

import java.util.EnumSet;
import java.util.List;

/**
 * Morty follows the nearest Rick within 32 blocks — someone has to hold his stuff.
 */
public class FollowRickGoal extends Goal {
	private final PathAwareEntity follower;
	private final double speed;
	private RickEntity rick;
	private int scanCooldown;

	public FollowRickGoal(PathAwareEntity follower, double speed) {
		this.follower = follower;
		this.speed = speed;
		this.setControls(EnumSet.of(Control.MOVE));
	}

	@Override
	public boolean canStart() {
		if (follower instanceof dev.benluvzbacon.rickmorty.entity.MortyEntity morty && morty.getTrustedPlayer() != null) {
			return false; // befriended Mortys follow their player instead
		}
		List<RickEntity> ricks = follower.getWorld().getEntitiesByClass(RickEntity.class,
				follower.getBoundingBox().expand(32), e -> e.isAlive());
		if (ricks.isEmpty()) return false;
		ricks.sort((a, b) -> Double.compare(follower.squaredDistanceTo(a), follower.squaredDistanceTo(b)));
		this.rick = ricks.get(0);
		return follower.distanceTo(rick) > 5.0f;
	}

	@Override
	public boolean shouldContinue() {
		if (rick == null || !rick.isAlive()) return false;
		if (follower.getNavigation().isIdle() && follower.distanceTo(rick) < 5.0f) return false;
		return follower.distanceTo(rick) < 40;
	}

	@Override
	public void tick() {
		if (rick == null) return;
		if (follower.distanceTo(rick) > 5.0f) {
			follower.getNavigation().startMovingTo(rick, speed);
		} else {
			follower.getNavigation().stop();
			follower.getLookControl().lookAt(rick, 10.0f, 10.0f);
		}
	}

	private interface LivingEntityAlive extends net.minecraft.entity.EntityPredicate {
		EntityPredicate ALIVE = entity -> entity instanceof net.minecraft.entity.LivingEntity le && le.isAlive();
	}
}
