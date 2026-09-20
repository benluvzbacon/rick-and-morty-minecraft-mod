package dev.benluvzbacon.rickmorty.entity;

import dev.benluvzbacon.rickmorty.config.ModConfig;
import dev.benluvzbacon.rickmorty.entity.ai.MeeseeksTaskGoal;
import dev.benluvzbacon.rickmorty.registry.ModSounds;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Mr. Meeseeks. Existence is pain, but at least it's brief: he gets one task and a
 * limited lifespan, then poofs away (with relief).
 * Tasks: PROTECT (fight hostiles near you), GATHER (vacuum up dropped items and hand
 * them over), or HUNT (attack the nearest hostile).
 */
public class MeeseeksEntity extends PathAwareEntity {
	public enum Task { PROTECT, GATHER, HUNT }

	private Task task = Task.PROTECT;
	@Nullable
	private UUID ownerUuid;
	private int remainingLife;

	public MeeseeksEntity(EntityType<? extends PathAwareEntity> type, World world) {
		super(type, world);
		this.experiencePoints = 0;
		this.remainingLife = ModConfig.get().meeseeksLifetimeSeconds * 20;
	}

	public static DefaultAttributeContainer.Builder createAttributes() {
		return MobEntity.createMobAttributes()
				.add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0)
				.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.4)
				.add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 6.0)
				.add(EntityAttributes.GENERIC_FOLLOW_RANGE, 24.0);
	}

	@Override
	protected void initGoals() {
		this.goalSelector.add(0, new SwimGoal(this));
		this.goalSelector.add(3, new MeeseeksTaskGoal(this));
		this.goalSelector.add(6, new WanderAroundFarGoal(this, 0.9));
		this.goalSelector.add(7, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
		this.goalSelector.add(8, new LookAroundGoal(this));
		this.targetSelector.add(1, new RevengeGoal(this));
	}

	public void assignRandomTask(@Nullable PlayerEntity owner) {
		this.ownerUuid = owner != null ? owner.getUuid() : null;
		this.task = Task.values()[random.nextInt(Task.values().length)];
	}

	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;
	}

	@Nullable
	public PlayerEntity getOwnerPlayer() {
		if (ownerUuid == null) return null;
		return getWorld().getPlayerByUuid(ownerUuid);
	}

	@Override
	public void onDeath(net.minecraft.entity.damage.DamageSource source) {
		super.onDeath(source);
		poof();
	}

	@Override
	public boolean canImmediatelyDespawn(double distanceSquared) {
		return false;
	}

	@Override
	protected void mobTick() {
		super.mobTick();
		if (getWorld().isClient) return;
		remainingLife--;
		if (remainingLife <= 0) {
			poof();
			discard();
			return;
		}
		// GATHER: hoover nearby dropped items into the owner's arms
		if (task == Task.GATHER && this.age % 12 == 0) {
			var items = getWorld().getEntitiesByClass(ItemEntity.class, getBoundingBox().expand(8), i -> i.isAlive());
			for (ItemEntity item : items) {
				ItemStack stack = item.getStack();
				PlayerEntity owner = getOwnerPlayer();
				if (owner != null && owner.isAlive() && distanceTo(owner) < 64) {
					owner.getInventory().offerOrDrop(stack.copy());
					item.discard();
					getWorld().playSound(null, item.getBlockPos(), net.minecraft.sound.SoundEvents.ENTITY_ITEM_PICKUP,
							SoundCategory.NEUTRAL, 0.4f, 1.2f);
				}
			}
		}
	}

	private void poof() {
		if (getWorld() instanceof ServerWorld sw) {
			sw.spawnParticles(ParticleTypes.CLOUD, getX(), getY() + 1, getZ(), 30, 0.4, 0.8, 0.4, 0.05);
			sw.playSound(null, getBlockPos(), ModSounds.MEESEEKS_POOF, SoundCategory.NEUTRAL, 1.0f, 1.0f);
		}
	}

	@Override
	public ActionResult interactMob(PlayerEntity player, Hand hand) {
		if (player instanceof ServerPlayerEntity sp && hand == Hand.MAIN_HAND) {
			if (ownerUuid == null) ownerUuid = player.getUuid();
			int secs = remainingLife / 20;
			sp.sendMessage(Text.translatable("msg.rickmorty.meeseeks.task",
					Text.translatable("msg.rickmorty.meeseeks.task." + task.name().toLowerCase()), secs)
					.formatted(Formatting.AQUA), false);
			return ActionResult.SUCCESS;
		}
		return super.interactMob(player, hand);
	}

	@Override
	public void writeCustomDataToNbt(NbtCompound nbt) {
		super.writeCustomDataToNbt(nbt);
		nbt.putString("task", task.name());
		nbt.putInt("remainingLife", remainingLife);
		if (ownerUuid != null) nbt.putUuid("owner", ownerUuid);
	}

	@Override
	public void readCustomDataFromNbt(NbtCompound nbt) {
		super.readCustomDataFromNbt(nbt);
		if (nbt.contains("task")) {
			try {
				task = Task.valueOf(nbt.getString("task"));
			} catch (IllegalArgumentException ignored) {
				task = Task.PROTECT;
			}
		}
		remainingLife = nbt.contains("remainingLife") ? nbt.getInt("remainingLife")
				: ModConfig.get().meeseeksLifetimeSeconds * 20;
		if (nbt.containsUuid("owner")) ownerUuid = nbt.getUuid("owner");
	}
}
