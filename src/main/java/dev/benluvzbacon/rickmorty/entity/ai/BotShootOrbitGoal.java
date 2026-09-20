package dev.benluvzbacon.rickmorty.entity.ai;

import dev.benluvzbacon.rickmorty.entity.SecurityBotEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.Vec3d;

import java.util.EnumSet;

/**
 * Citadel security drone fire pattern: keep a cautious distance, strafe sideways,
 * fire laser bolts in short disciplined bursts. Replaces vanilla ProjectileAttackGoal
 * wiring (which needs the RangedAttackMob contract).
 */
public class BotShootOrbitGoal extends Goal {
	private final SecurityBotEntity bot;
	private int burstTicks;
	private final int burstCooldown = 40;

	public BotShootOrbitGoal(SecurityBotEntity bot) {
		this.bot = bot;
		this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
	}

	@Override
	public boolean canStart() {
		return bot.getTarget() != null && bot.getTarget().isAlive();
	}

	@Override
	public boolean shouldContinue() {
		return canStart();
	}

	@Override
	public void tick() {
		LivingEntity target = bot.getTarget();
		if (target == null) return;
		Vec3d toTarget = target.getPos().subtract(bot.getPos());
		double dist = toTarget.length();
		bot.getLookControl().lookAt(target, 30.0f, 30.0f);
		if (dist > 14) {
			bot.getNavigation().startMovingTo(target, 1.1);
		} else if (dist < 8) {
			bot.getNavigation().stop();
			bot.setVelocity(bot.getVelocity().multiply(0.6).add(toTarget.normalize().multiply(-0.04)));
		} else {
			// strafe
			Vec3d side = new Vec3d(-toTarget.z, 0, toTarget.x).normalize().multiply(0.03);
			bot.addVelocity(side.x, 0, side.z);
		}
		if (bot.canSee(target)) {
			if (burstTicks <= 0) burstTicks = burstCooldown;
			burstTicks--;
			if (burstTicks % 10 == 5) {
				bot.shootAt(target, 1.0f);
			}
		} else {
			burstTicks = 0;
			bot.getNavigation().startMovingTo(target, 1.2);
		}
	}
}
