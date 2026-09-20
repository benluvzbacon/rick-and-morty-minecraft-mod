package dev.benluvzbacon.rickmorty.entity;

import dev.benluvzbacon.rickmorty.dialogue.DialogueManager;
import dev.benluvzbacon.rickmorty.entity.ai.FollowRickGoal;
import dev.benluvzbacon.rickmorty.entity.ai.MortyFollowPlayerGoal;
import dev.benluvzbacon.rickmorty.entity.ai.MortyWeaponGoal;
import dev.benluvzbacon.rickmorty.entity.projectile.EnergyBoltEntity;
import dev.benluvzbacon.rickmorty.item.PortalBlasterItem;
import dev.benluvzbacon.rickmorty.registry.ModItems;
import dev.benluvzbacon.rickmorty.registry.ModSounds;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Morty Smith. Nervous, good-hearted, way out of his depth.
 * Flees from monsters unless he's holding a blaster (then he actually fights — badly).
 * Follows Rick around. Can be befriended with a Simple Wafer, after which he tags
 * along with you instead.
 */
public class MortyEntity extends PathAwareEntity {
	@Nullable
	private UUID trustedPlayer;
	private int panicSoundCooldown;

	public MortyEntity(EntityType<? extends PathAwareEntity> type, World world) {
		super(type, world);
		this.experiencePoints = 10;
		this.setCanPickUpLoot(true);
	}

	public static DefaultAttributeContainer.Builder createAttributes() {
		return MobEntity.createMobAttributes()
				.add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0)
				.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.34)
				.add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 3.0)
				.add(EntityAttributes.GENERIC_FOLLOW_RANGE, 24.0);
	}

	@Override
	protected void initGoals() {
		this.goalSelector.add(0, new SwimGoal(this));
		this.goalSelector.add(1, new EscapeDangerGoal(this, 1.6));
		this.goalSelector.add(2, new FleeEntityGoal<>(this, HostileEntity.class, 12.0f, 1.2, 1.5,
				e -> !hasWeapon()));
		this.goalSelector.add(3, new MortyWeaponGoal(this));
		this.goalSelector.add(4, new MortyFollowPlayerGoal(this, 1.0));
		this.goalSelector.add(5, new FollowRickGoal(this, 1.0));
		this.goalSelector.add(7, new WanderAroundFarGoal(this, 0.8));
		this.goalSelector.add(8, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
		this.goalSelector.add(9, new LookAroundGoal(this));
		this.targetSelector.add(1, new RevengeGoal(this));
	}

	public boolean hasWeapon() {
		return getMainHandStack().getItem() instanceof PortalBlasterItem;
	}

	public boolean isTrustedBy(PlayerEntity player) {
		return trustedPlayer != null && trustedPlayer.equals(player.getUuid());
	}

	public void trust(PlayerEntity player) {
		this.trustedPlayer = player.getUuid();
	}

	@Nullable
	public UUID getTrustedPlayer() {
		return trustedPlayer;
	}

	public void shootAt(LivingEntity target, float pullProgress) {
		if (!hasWeapon()) return;
		EnergyBoltEntity bolt = EnergyBoltEntity.portalBolt(this, target, 4.0f);
		// Morty's aim is not great.
		bolt.setVelocity(bolt.getVelocity().x + (random.nextDouble() - 0.5) * 0.12,
				bolt.getVelocity().y + (random.nextDouble() - 0.5) * 0.08,
				bolt.getVelocity().z + (random.nextDouble() - 0.5) * 0.12);
		getWorld().spawnEntity(bolt);
		playSound(ModSounds.BLASTER_SHOT, 1.0f, 1.1f + random.nextFloat() * 0.2f);
	}

	@Override
	protected void mobTick() {
		super.mobTick();
		if (getWorld().isClient) return;
		if (this.age % 30 == 0) {
			maybeDialogue();
		}
	}

	private void maybeDialogue() {
		if (!(getWorld() instanceof ServerWorld world)) return;
		var players = world.getEntitiesByClass(ServerPlayerEntity.class, getBoundingBox().expand(10), p -> true);
		if (players.isEmpty()) return;
		ServerPlayerEntity player = players.get(0);
		var random = world.random;

		var hostiles = world.getEntitiesByClass(HostileEntity.class, getBoundingBox().expand(10), LivingEntity::isAlive);
		if (!hostiles.isEmpty() && !hasWeapon()) {
			if (panicSoundCooldown <= 0) {
				playSound(ModSounds.MORTY_PANIC, 1.0f, 1.0f + random.nextFloat() * 0.3f);
				panicSoundCooldown = 50;
			}
			DialogueManager.say(player, "morty", Formatting.YELLOW, "panic", 3, 25 * 20, random);
			return;
		}
		if (panicSoundCooldown > 0) panicSoundCooldown--;

		if (hasWeapon()) {
			DialogueManager.say(player, "morty", Formatting.YELLOW, "armed", 2, 90 * 20, random);
			return;
		}
		var dimKey = world.getRegistryKey();
		if (dimKey != World.OVERWORLD && dimKey != dev.benluvzbacon.rickmorty.registry.ModDimensions.CITADEL_WORLD) {
			DialogueManager.say(player, "morty", Formatting.YELLOW, "weird_place", 2, 100 * 20, random);
			return;
		}
		DialogueManager.say(player, "morty", Formatting.YELLOW, "intro", 3, 200 * 20, random);
	}

	@Override
	public ActionResult interactMob(PlayerEntity player, Hand hand) {
		ItemStack stack = player.getStackInHand(hand);
		if (stack.isOf(ModItems.SIMPLE_WAFER) && !getWorld().isClient) {
			if (!isTrustedBy(player)) {
				trust(player);
				if (!player.getAbilities().creativeMode) stack.decrement(1);
				if (player instanceof ServerPlayerEntity sp) {
					DialogueManager.say(sp, "morty", Formatting.YELLOW, "befriended", 2, 1, random);
				}
				getWorld().sendEntityStatus(this, (byte) 7);
				return ActionResult.SUCCESS;
			}
		}
		if (player instanceof ServerPlayerEntity sp && hand == Hand.MAIN_HAND) {
			maybeDialogue();
			DialogueManager.say(sp, "morty", Formatting.YELLOW, "talk", 3, 10 * 20, random);
			return ActionResult.SUCCESS;
		}
		return super.interactMob(player, hand);
	}

	@Override
	public ItemStack tryEquip(ItemStack stack) {
		if (stack.getItem() instanceof PortalBlasterItem) {
			equipStack(EquipmentSlot.MAINHAND, stack.copyWithCount(1));
			setEquipmentDropChance(EquipmentSlot.MAINHAND, 1.0f);
			stack.decrement(1);
			setPersistent();
			return stack;
		}
		return super.tryEquip(stack);
	}

	@Override
	public boolean isPersistent() {
		return super.isPersistent() || trustedPlayer != null;
	}

	@Override
	public void writeCustomDataToNbt(NbtCompound nbt) {
		super.writeCustomDataToNbt(nbt);
		if (trustedPlayer != null) nbt.putUuid("trustedPlayer", trustedPlayer);
	}

	@Override
	public void readCustomDataFromNbt(NbtCompound nbt) {
		super.readCustomDataFromNbt(nbt);
		if (nbt.containsUuid("trustedPlayer")) trustedPlayer = nbt.getUuid("trustedPlayer");
	}
}
