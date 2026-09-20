package dev.benluvzbacon.rickmorty.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

/**
 * A hulking brute with anger-management improv skills. Slow, armored, and sends you
 * flying with every landed hit.
 */
public class GazorpianEntity extends HostileEntity {
	public GazorpianEntity(EntityType<? extends HostileEntity> type, World world) {
		super(type, world);
		this.experiencePoints = 14;
	}

	public static DefaultAttributeContainer.Builder createAttributes() {
		return HostileEntity.createHostileAttributes()
				.add(EntityAttributes.GENERIC_MAX_HEALTH, 45.0)
				.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.22)
				.add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 10.0)
				.add(EntityAttributes.GENERIC_ARMOR, 8.0)
				.add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.8)
				.add(EntityAttributes.GENERIC_FOLLOW_RANGE, 24.0);
	}

	@Override
	protected void initGoals() {
		this.goalSelector.add(0, new SwimGoal(this));
		this.goalSelector.add(2, new MeleeAttackGoal(this, 1.0, false));
		this.goalSelector.add(5, new WanderAroundFarGoal(this, 0.7));
		this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
		this.goalSelector.add(7, new LookAroundGoal(this));
		this.targetSelector.add(1, new RevengeGoal(this));
		this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
		this.targetSelector.add(3, new ActiveTargetGoal<>(this, MortyEntity.class, true));
	}

	@Override
	public boolean tryAttack(net.minecraft.entity.Entity target) {
		boolean hit = super.tryAttack(target);
		if (hit && target instanceof net.minecraft.entity.LivingEntity living) {
			// freight-train knockback
			double dx = target.getX() - getX();
			double dz = target.getZ() - getZ();
			double len = Math.max(0.01, Math.sqrt(dx * dx + dz * dz));
			target.addVelocity(dx / len * 1.4, 0.5, dz / len * 1.4);
			if (target instanceof net.minecraft.server.network.ServerPlayerEntity sp) {
				sp.velocityModified = true;
			}
		}
		return hit;
	}
}
