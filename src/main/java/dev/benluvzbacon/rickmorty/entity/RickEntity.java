package dev.benluvzbacon.rickmorty.entity;

import dev.benluvzbacon.rickmorty.config.ModConfig;
import dev.benluvzbacon.rickmorty.dialogue.DialogueManager;
import dev.benluvzbacon.rickmorty.entity.ai.RickCombatGoal;
import dev.benluvzbacon.rickmorty.entity.ai.RickTeleportEscapeGoal;
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
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

/**
 * Rick Sanchez. The smartest man in the universe, now blockier.
 * Smarter than the average mob: ranged portal-bolt combat, strafing, teleports out of
 * danger, deploys security drones when a fight drags on, and never walks into lava.
 */
public class RickEntity extends PathAwareEntity {
	private int droneCooldown;

	public RickEntity(EntityType<? extends PathAwareEntity> type, World world) {
		super(type, world);
		this.experiencePoints = 50;
		this.setCanPickUpLoot(false);
	}

	public static DefaultAttributeContainer.Builder createAttributes() {
		return MobEntity.createMobAttributes()
				.add(EntityAttributes.GENERIC_MAX_HEALTH, ModConfig.get().rickHealth)
				.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.32)
				.add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 8.0)
				.add(EntityAttributes.GENERIC_ARMOR, 8.0)
				.add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.6)
				.add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32.0);
	}

	@Override
	protected void initGoals() {
		this.goalSelector.add(0, new SwimGoal(this));
		this.goalSelector.add(1, new RickTeleportEscapeGoal(this));
		this.goalSelector.add(2, new EscapeDangerGoal(this, 1.4));
		this.goalSelector.add(3, new RickCombatGoal(this));
		this.goalSelector.add(5, new WanderAroundFarGoal(this, 0.75));
		this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 10.0f));
		this.goalSelector.add(7, new LookAroundGoal(this));
		this.targetSelector.add(1, new RevengeGoal(this));
	}

	public void shootAt(LivingEntity target, float pullProgress) {
		EnergyBoltEntity bolt = EnergyBoltEntity.portalBolt(this, target, 6.0f);
		getWorld().spawnEntity(bolt);
		playSound(ModSounds.PORTAL_SHOOT, 1.0f, 0.9f + random.nextFloat() * 0.2f);
	}

	@Override
	protected void mobTick() {
		super.mobTick();
		if (getWorld().isClient) return;
		if (droneCooldown > 0) droneCooldown--;

		// deploy a security drone if a fight drags on
		LivingEntity target = getTarget();
		if (target != null && target.isAlive() && droneCooldown <= 0 && this.age % 200 == 0
				&& getWorld().getEntitiesByClass(SecurityBotEntity.class, getBoundingBox().expand(24),
						b -> b.getOwner() == this).size() < 2) {
			SecurityBotEntity drone = new SecurityBotEntity(dev.benluvzbacon.rickmorty.registry.ModEntities.SECURITY_BOT, getWorld());
			drone.refreshPositionAndAngles(getX() + 1.5, getY() + 0.5, getZ() + 1.5, random.nextFloat() * 360, 0);
			drone.setOwner(this);
			drone.setLifespan(1200);
			drone.setTarget(target);
			getWorld().spawnEntity(drone);
			droneCooldown = 1800;
			playSound(ModSounds.RICK_TELEPORT, 0.7f, 1.4f);
			bark("drone", 2);
		}

		// contextual dialogue
		if (this.age % 40 == 0) {
			maybeDialogue();
		}
	}

	private void maybeDialogue() {
		if (getWorld().isClient) return;
		var players = getWorld().getEntitiesByClass(ServerPlayerEntity.class, getBoundingBox().expand(12), p -> true);
		if (players.isEmpty()) return;
		ServerPlayerEntity player = players.get(0);
		var random = getWorld().random;

		if (getHealth() < getMaxHealth() * 0.35f) {
			DialogueManager.say(player, "rick", Formatting.AQUA, "low_health", 2, 30 * 20, random);
			return;
		}
		if (getTarget() != null) {
			DialogueManager.say(player, "rick", Formatting.AQUA, "combat", 3, 45 * 20, random);
			return;
		}
		var dimKey = getWorld().getRegistryKey();
		if (dimKey == dev.benluvzbacon.rickmorty.registry.ModDimensions.ALIEN_WORLD) {
			DialogueManager.say(player, "rick", Formatting.AQUA, "alien_dim", 2, 120 * 20, random);
			return;
		}
		if (dimKey == dev.benluvzbacon.rickmorty.registry.ModDimensions.CRONENBERG_WORLD) {
			DialogueManager.say(player, "rick", Formatting.AQUA, "cronenberg_dim", 2, 120 * 20, random);
			return;
		}
		if (dimKey == dev.benluvzbacon.rickmorty.registry.ModDimensions.POCKET_WORLD) {
			DialogueManager.say(player, "rick", Formatting.AQUA, "pocket_dim", 2, 120 * 20, random);
			return;
		}
		// ambient (also serves as "first meeting")
		DialogueManager.say(player, "rick", Formatting.AQUA, "intro", 3, 240 * 20, random);
	}

	public void bark(String trigger, int variants) {
		if (getWorld().isClient) return;
		var players = getWorld().getEntitiesByClass(ServerPlayerEntity.class, getBoundingBox().expand(14), p -> true);
		if (!players.isEmpty()) {
			DialogueManager.say(players.get(0), "rick", Formatting.AQUA, trigger, variants, 20 * 20, random);
		}
	}

	@Override
	public boolean isPushable() {
		return true;
	}

	@Override
	public boolean isPersistent() {
		return true; // there's only so many Ricks you'd want
	}

	@Override
	public ActionResult interactMob(PlayerEntity player, Hand hand) {
		if (player instanceof ServerPlayerEntity sp && hand == Hand.MAIN_HAND) {
			bark("talk", 3);
			return ActionResult.SUCCESS;
		}
		return super.interactMob(player, hand);
	}
}
