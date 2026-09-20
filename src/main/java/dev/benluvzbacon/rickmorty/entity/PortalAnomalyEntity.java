package dev.benluvzbacon.rickmorty.entity;

import dev.benluvzbacon.rickmorty.entity.projectile.EnergyBoltEntity;
import dev.benluvzbacon.rickmorty.registry.ModSounds;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.RangedAttackMob;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * A knot of unstable portal energy drifting through the pocket dimension.
 * Floats around, hums, fires slow anomaly bolts, and flickers when hit.
 */
public class PortalAnomalyEntity extends MobEntity implements RangedAttackMob {
	private int driftCooldown;
	private int attackCooldown;

	public PortalAnomalyEntity(EntityType<? extends MobEntity> type, World world) {
		super(type, world);
		this.experiencePoints = 12;
		this.setNoGravity(true);
	}

	public static DefaultAttributeContainer.Builder createAttributes() {
		return MobEntity.createMobAttributes()
				.add(EntityAttributes.GENERIC_MAX_HEALTH, 16.0)
				.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.0)
				.add(EntityAttributes.GENERIC_FLYING_SPEED, 0.25)
				.add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 4.0)
				.add(EntityAttributes.GENERIC_FOLLOW_RANGE, 24.0);
	}

	@Override
	protected void initGoals() {
		this.targetSelector.add(1, new RevengeGoal(this));
		this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
	}

	@Override
	protected void mobTick() {
		super.mobTick();
		LivingEntity target = getTarget();
		Vec3d vel = getVelocity();

		if (target != null && target.isAlive()) {
			getLookControl().lookAt(target, 30f, 30f);
			// hover toward/away from target to hold ~7 blocks
			double dist = distanceTo(target);
			Vec3d dir = target.getPos().add(0, 1.0, 0).subtract(getPos()).normalize();
			double wanted = dist > 9 ? 0.18 : (dist < 5 ? -0.15 : 0.04);
			setVelocity(vel.x * 0.5 + dir.x * wanted,
					vel.y * 0.5 + dir.y * wanted * 0.6,
					vel.z * 0.5 + dir.z * wanted);
			if (attackCooldown > 0) attackCooldown--;
			if (attackCooldown <= 0 && canSee(target) && dist < 18) {
				shootAt(target, 0);
				attackCooldown = 50 + random.nextInt(40);
			}
		} else {
			// lazy drifting
			if (driftCooldown <= 0) {
				driftCooldown = 40 + random.nextInt(60);
				setVelocity((random.nextDouble() - 0.5) * 0.12,
						(random.nextDouble() - 0.5) * 0.06,
						(random.nextDouble() - 0.5) * 0.12);
			} else {
				driftCooldown--;
			}
		}
		if (this.age % 80 == 0 && !getWorld().isClient) {
			playSound(ModSounds.ANOMALY, 0.6f, 0.8f + random.nextFloat() * 0.5f);
		}
	}

	@Override
	public void shootAt(LivingEntity target, float pullProgress) {
		EnergyBoltEntity bolt = EnergyBoltEntity.anomalyBolt(this, target, 5.0f);
		getWorld().spawnEntity(bolt);
		playSound(ModSounds.ANOMALY, 1.0f, 1.5f);
	}

	@Override
	public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
		return false;
	}

	@Override
	protected net.minecraft.util.math.Vec3d applyEffectiveMovementFactors(net.minecraft.util.math.Vec3d movementInput) {
		return movementInput.multiply(0.98);
	}
}
