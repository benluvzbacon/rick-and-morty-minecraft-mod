package dev.benluvzbacon.rickmorty.entity.projectile;

import dev.benluvzbacon.rickmorty.registry.ModSounds;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Grappling hook. Fired from the Grappling Device; once it bites into a block it reels
 * its owner in. Right-click again (or sneak) to let go.
 */
public class GrapnelEntity extends ProjectileEntity {
	private boolean latched;
	private static final double REEL_SPEED = 0.55;
	private static final double MAX_RANGE = 40.0;

	public GrapnelEntity(EntityType<? extends GrapnelEntity> type, World world) {
		super(type, world);
		this.setNoGravity(true);
	}

	@Override
	protected void initDataTracker(DataTracker.Builder builder) {
	}

	public boolean isLatched() {
		return latched;
	}

	public void release() {
		discard();
	}

	@Override
	public void tick() {
		super.tick();
		Entity owner = getOwner();
		if (owner == null || !owner.isAlive()) {
			discard();
			return;
		}
		if (age > 400) {
			discard();
			return;
		}
		double dist = distanceTo(owner);
		if (dist > MAX_RANGE) {
			discard();
			return;
		}
		if (owner instanceof PlayerEntity player && player.isSneaking()) {
			discard();
			return;
		}
		if (latched && !getWorld().isClient) {
			// reel the owner in
			Vec3d pull = getPos().subtract(owner.getPos());
			double len = pull.length();
			if (len > 2.0) {
				pull = pull.normalize().multiply(Math.min(REEL_SPEED, len * 0.3));
				owner.setVelocity(pull.x, pull.y * 1.15 + 0.02, pull.z);
				if (owner instanceof ServerPlayerEntity sp) sp.velocityModified = true;
			} else {
				discard();
			}
		}
		// rope particles (client-side looks fine server-driven via entity positions)
		if (getWorld().isClient) {
			Vec3d from = owner.getPos().add(0, owner.getHeight() * 0.7, 0);
			Vec3d to = getPos();
			Vec3d step = to.subtract(from);
			int segments = Math.min(16, (int) (step.length() * 2));
			for (int i = 1; i < segments; i += 2) {
				Vec3d p = from.add(step.multiply((double) i / segments));
				getWorld().addParticle(net.minecraft.particle.ParticleTypes.CRIT, p.x, p.y, p.z, 0, 0, 0);
			}
		}
	}

	@Override
	protected void onBlockHit(BlockHitResult hit) {
		super.onBlockHit(hit);
		if (latched) return;
		latched = true;
		setVelocity(Vec3d.ZERO);
		setPosition(hit.getPos());
		if (!getWorld().isClient) {
			getWorld().playSound(null, hit.getBlockPos(), ModSounds.GRAPPLE_HIT, SoundCategory.PLAYERS, 1.0f, 1.0f);
		}
	}

	@Override
	protected void onEntityHit(EntityHitResult hit) {
		super.onEntityHit(hit);
		if (getWorld().isClient) return;
		latched = true;
		setVelocity(Vec3d.ZERO);
		Entity target = hit.getEntity();
		// yank smaller things toward the player instead
		Entity owner = getOwner();
		if (owner instanceof LivingEntity livingOwner && target instanceof LivingEntity livingTarget
				&& livingTarget.getMaxHealth() < livingOwner.getMaxHealth() * 1.5) {
			Vec3d pull = livingOwner.getPos().subtract(livingTarget.getPos()).normalize().multiply(0.9);
			livingTarget.addVelocity(pull.x, pull.y * 0.4 + 0.2, pull.z);
			discard();
		}
	}
}
