package dev.benluvzbacon.rickmorty.entity;

import dev.benluvzbacon.rickmorty.entity.projectile.EnergyBoltEntity;
import dev.benluvzbacon.rickmorty.registry.ModSounds;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Citadel security drone. Keeps the peace by vaporizing monsters and anyone who
 * starts trouble. Rick can commandeer them mid-fight.
 */
public class SecurityBotEntity extends PathAwareEntity {
	@Nullable
	private UUID ownerUuid;
	private int lifespan = -1; // -1 = permanent citadel unit

	public SecurityBotEntity(EntityType<? extends PathAwareEntity> type, World world) {
		super(type, world);
		this.experiencePoints = 10;
	}

	public static DefaultAttributeContainer.Builder createAttributes() {
		return MobEntity.createMobAttributes()
				.add(EntityAttributes.GENERIC_MAX_HEALTH, 26.0)
				.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3)
				.add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 5.0)
				.add(EntityAttributes.GENERIC_ARMOR, 6.0)
				.add(EntityAttributes.GENERIC_FOLLOW_RANGE, 28.0);
	}

	@Override
	protected void initGoals() {
		this.goalSelector.add(0, new SwimGoal(this));
		this.goalSelector.add(2, new dev.benluvzbacon.rickmorty.entity.ai.BotShootOrbitGoal(this));
		this.goalSelector.add(5, new WanderAroundFarGoal(this, 0.7));
		this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
		this.goalSelector.add(7, new LookAroundGoal(this));
		this.targetSelector.add(1, new RevengeGoal(this));
		this.targetSelector.add(2, new ActiveTargetGoal<>(this, HostileEntity.class, true, false));
	}

	public void setOwner(@Nullable LivingEntity owner) {
		this.ownerUuid = owner != null ? owner.getUuid() : null;
	}

	@Nullable
	public LivingEntity getOwner() {
		if (ownerUuid == null) return null;
		return getWorld() instanceof net.minecraft.server.world.ServerWorld sw && sw.getEntity(ownerUuid) instanceof LivingEntity living ? living : null;
	}

	public void setLifespan(int ticks) {
		this.lifespan = ticks;
	}

	@Override
	public void shootAt(LivingEntity target, float pullProgress) {
		EnergyBoltEntity bolt = EnergyBoltEntity.laserBolt(this, target, 5.0f);
		getWorld().spawnEntity(bolt);
		playSound(ModSounds.LASER_SHOT, 1.0f, 1.0f + random.nextFloat() * 0.2f);
	}

	@Override
	protected void mobTick() {
		super.mobTick();
		if (getWorld().isClient) return;
		if (lifespan > 0) {
			lifespan--;
			if (lifespan == 0 && getWorld() instanceof ServerWorld sw) {
				sw.spawnParticles(ParticleTypes.CLOUD, getX(), getY() + 0.8, getZ(), 20, 0.3, 0.5, 0.3, 0.04);
				discard();
				return;
			}
		}
		// citadel units never attack players unless provoked; rick drones inherit his targets
		LivingEntity owner = getOwner();
		if (owner != null && owner instanceof RickEntity rick && rick.getTarget() != null && getTarget() == null) {
			setTarget(rick.getTarget());
		}
	}

	@Override
	public void writeCustomDataToNbt(NbtCompound nbt) {
		super.writeCustomDataToNbt(nbt);
		if (ownerUuid != null) nbt.putUuid("owner", ownerUuid);
		nbt.putInt("lifespan", lifespan);
	}

	@Override
	public void readCustomDataFromNbt(NbtCompound nbt) {
		super.readCustomDataFromNbt(nbt);
		if (nbt.containsUuid("owner")) ownerUuid = nbt.getUuid("owner");
		lifespan = nbt.contains("lifespan") ? nbt.getInt("lifespan") : -1;
	}
}
