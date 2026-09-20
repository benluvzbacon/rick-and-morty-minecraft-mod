package dev.benluvzbacon.rickmorty.entity.ai;

import dev.benluvzbacon.rickmorty.entity.MortyEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.player.PlayerEntity;

import java.util.EnumSet;

/**
 * A befriended Morty tags along with his trusted player (like a nervous tamed wolf
 * without the sitting mechanic — he can't sit, he's too anxious).
 */
public class MortyFollowPlayerGoal extends Goal {
	private final MortyEntity morty;
	private final double speed;
	private PlayerEntity player;
	private int scanCooldown;

	public MortyFollowPlayerGoal(MortyEntity morty, double speed) {
		this.morty = morty;
		this.speed = speed;
		this.setControls(EnumSet.of(Control.MOVE));
	}

	@Override
	public boolean canStart() {
		if (morty.getTrustedPlayer() == null) return false;
		if (scanCooldown > 0) {
			scanCooldown--;
			return false;
		}
		scanCooldown = 20;
		PlayerEntity p = morty.getWorld().getPlayerByUuid(morty.getTrustedPlayer());
		if (p == null || !p.isAlive()) return false;
		this.player = p;
		return morty.distanceTo(p) > 4.0f;
	}

	@Override
	public boolean shouldContinue() {
		if (morty.getTrustedPlayer() == null || player == null || !player.isAlive()) return false;
		return morty.distanceTo(player) < 40 && !morty.getNavigation().isIdle();
	}

	@Override
	public void tick() {
		if (player == null) return;
		if (morty.distanceTo(player) > 4.0f) {
			morty.getNavigation().startMovingTo(player, speed);
		} else {
			morty.getNavigation().stop();
		}
	}
}
