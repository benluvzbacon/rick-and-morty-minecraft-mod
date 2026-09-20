package dev.benluvzbacon.rickmorty.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

/**
 * A fast, skittering six-limbed hunter from the alien planet. Low health, high speed,
 * swarms in small groups.
 */
public class AlienCrawlerEntity extends HostileEntity {
	public AlienCrawlerEntity(EntityType<? extends HostileEntity> type, World world) {
		super(type, world);
		this.experiencePoints = 6;
	}

	public static DefaultAttributeContainer.Builder createAttributes() {
		return HostileEntity.createHostileAttributes()
				.add(EntityAttributes.GENERIC_MAX_HEALTH, 12.0)
				.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.4)
				.add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 4.0)
				.add(EntityAttributes.GENERIC_FOLLOW_RANGE, 28.0);
	}

	@Override
	protected void initGoals() {
		this.goalSelector.add(0, new SwimGoal(this));
		this.goalSelector.add(1, new PounceAtTargetGoal(this, 0.4f));
		this.goalSelector.add(2, new MeleeAttackGoal(this, 1.4, false));
		this.goalSelector.add(5, new WanderAroundFarGoal(this, 1.0));
		this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
		this.goalSelector.add(7, new LookAroundGoal(this));
		this.targetSelector.add(1, new RevengeGoal(this));
		this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
		this.targetSelector.add(3, new ActiveTargetGoal<>(this, MortyEntity.class, true));
	}

	protected float getActiveEyeHeight(net.minecraft.entity.EntityPose pose, net.minecraft.entity.EntityDimensions dimensions) {
		return 0.45f;
	}
}
