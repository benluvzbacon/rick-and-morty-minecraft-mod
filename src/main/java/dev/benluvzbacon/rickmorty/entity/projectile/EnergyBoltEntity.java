package dev.benluvzbacon.rickmorty.entity.projectile;

import dev.benluvzbacon.rickmorty.registry.ModDamageTypes;
import dev.benluvzbacon.rickmorty.registry.ModEntities;
import dev.benluvzbacon.rickmorty.entity.boss.AbominationEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Vector3f;

/**
 * Generic energy projectile used by almost every gun and shooter in the mod.
 * Behavior depends on {@link Kind}; no persistence (short-lived by design).
 */
public class EnergyBoltEntity extends ProjectileEntity {
	public enum Kind {
		PORTAL(14.0, new Vector3f(0.25f, 1.0f, 0.55f)),
		LASER(22.0, new Vector3f(1.0f, 0.35f, 0.25f)),
		PLASMA(9.0, new Vector3f(0.55f, 0.35f, 1.0f)),
		SHRINK(16.0, new Vector3f(0.4f, 0.8f, 1.0f)),
		ANOMALY(6.0, new Vector3f(0.85f, 0.3f, 0.85f));

		public final double speed;
		public final Vector3f color;

		Kind(double speed, Vector3f color) {
			this.speed = speed;
			this.color = color;
		}
	}

	private Kind kind = Kind.PORTAL;
	private float damage = 6.0f;
	private int lifeTicks = 60;

	public EnergyBoltEntity(EntityType<? extends EnergyBoltEntity> type, World world) {
		super(type, world);
		this.setNoGravity(true);
	}

	private static void setVelocityFromAngles(EnergyBoltEntity bolt, float pitch, float yaw, float speed, float divergence) {
		float rad = (float) (Math.PI / 180.0);
		float x = -net.minecraft.util.math.MathHelper.sin(yaw * rad) * net.minecraft.util.math.MathHelper.cos(pitch * rad);
		float y = -net.minecraft.util.math.MathHelper.sin(pitch * rad);
		float z = net.minecraft.util.math.MathHelper.cos(yaw * rad) * net.minecraft.util.math.MathHelper.cos(pitch * rad);
		java.util.Random r = new java.util.Random();
		bolt.setVelocity(new Vec3d(x, y, z).normalize().multiply(speed)
				.add(r.nextGaussian() * 0.0075 * divergence, r.nextGaussian() * 0.0075 * divergence, r.nextGaussian() * 0.0075 * divergence));
	}

	private static EnergyBoltEntity create(LivingEntity shooter, LivingEntity target, Kind kind, float damage) {
		EnergyBoltEntity bolt = new EnergyBoltEntity(ModEntities.ENERGY_BOLT, shooter.getWorld());
		bolt.kind = kind;
		bolt.damage = damage;
		bolt.lifeTicks = kind == Kind.ANOMALY ? 90 : 60;
		bolt.setOwner(shooter);
		bolt.setPosition(shooter.getX(), shooter.getEyeY() - 0.15, shooter.getZ());
		setVelocityFromAngles(bolt, target.getPitch(), target.getYaw(), 1.5f * 20, 0.2f * 20);
		// aim directly at the target instead of where the shooter looks, for reliability
		Vec3d dir = target.getPos().add(0, target.getHeight() * 0.5, 0)
				.subtract(shooter.getX(), shooter.getEyeY() - 0.15, shooter.getZ()).normalize();
		bolt.setVelocity(dir.multiply(kind.speed * 0.08));
		return bolt;
	}

	public static EnergyBoltEntity portalBolt(LivingEntity shooter, LivingEntity target, float damage) {
		return create(shooter, target, Kind.PORTAL, damage);
	}

	public static EnergyBoltEntity laserBolt(LivingEntity shooter, LivingEntity target, float damage) {
		return create(shooter, target, Kind.LASER, damage);
	}

	/** Fired by players: uses the player's look vector. */
	public static EnergyBoltEntity firedBy(LivingEntity shooter, Kind kind, float damage, float divergence) {
		EnergyBoltEntity bolt = new EnergyBoltEntity(ModEntities.ENERGY_BOLT, shooter.getWorld());
		bolt.kind = kind;
		bolt.damage = damage;
		bolt.lifeTicks = 70;
		bolt.setOwner(shooter);
		bolt.setPosition(shooter.getX(), shooter.getEyeY() - 0.1, shooter.getZ());
		setVelocityFromAngles(bolt, shooter.getPitch(), shooter.getYaw(), (float) kind.speed, divergence / 20f);
		return bolt;
	}

	public static EnergyBoltEntity anomalyBolt(LivingEntity shooter, LivingEntity target, float damage) {
		return create(shooter, target, Kind.ANOMALY, damage);
	}

	/** Spread barrage used by the Interdimensional Abomination. */
	public static EnergyBoltEntity plasmaBarrage(LivingEntity shooter, LivingEntity target, float damage) {
		EnergyBoltEntity bolt = create(shooter, target, Kind.PLASMA, damage);
		Vec3d v = bolt.getVelocity();
		bolt.setVelocity(new Vec3d(v.x + (shooter.getRandom().nextDouble() - 0.5) * 0.22,
				v.y + (shooter.getRandom().nextDouble() - 0.5) * 0.16,
				v.z + (shooter.getRandom().nextDouble() - 0.5) * 0.22));
		return bolt;
	}

	public static EnergyBoltEntity shrinkBolt(LivingEntity shooter) {
		return firedBy(shooter, Kind.SHRINK, 0.0f, 0.0f);
	}

	@Override
	protected void initDataTracker(DataTracker.Builder builder) {
	}

	public Kind getKind() {
		return kind;
	}

	@Override
	public void tick() {
		super.tick();
		if (getWorld().isClient) {
			if (age % 1 == 0) {
				ParticleEffect dust = new DustParticleEffect(kind.color, 1.0f);
				getWorld().addParticle(dust, getX(), getY(), getZ(), 0, 0, 0);
			}
		} else {
			if (age > lifeTicks) {
				discard();
			}
		}
	}

	@Override
	protected void onEntityHit(EntityHitResult hit) {
		super.onEntityHit(hit);
		Entity target = hit.getEntity();
		if (target == getOwner() && age < 5) return; // don't instantly shoot yourself
		if (!(getWorld() instanceof ServerWorld sw)) return;

		switch (kind) {
			case SHRINK -> applyShrink(target);
			default -> {
				DamageSource source = getDamageSources().create(switch (kind) {
					case LASER -> ModDamageTypes.LASER;
					case PLASMA -> ModDamageTypes.PLASMA;
					case ANOMALY -> ModDamageTypes.ANOMALY;
					default -> ModDamageTypes.PORTAL_BOLT;
				}, this, getOwner());
				boolean hurt;
				if (kind == Kind.PLASMA) {
					hurt = target instanceof LivingEntity living && living.damage(source, damage);
					areaBlast(sw, target);
				} else {
					hurt = target.damage(source, damage);
				}
				if (hurt && target instanceof LivingEntity living) {
					float kb = 0.4f;
					living.addVelocity(getVelocity().normalize().multiply(kb).x, 0.15, getVelocity().normalize().multiply(kb).z);
				}
			}
		}
		discard();
	}

	private void areaBlast(ServerWorld world, Entity center) {
		world.spawnParticles(ParticleTypes.FLASH, center.getX(), center.getY() + 0.5, center.getZ(), 1, 0, 0, 0, 0);
		world.spawnParticles(ParticleTypes.WITCH, center.getX(), center.getY() + 0.5, center.getZ(), 25, 0.5, 0.5, 0.5, 0.05);
		var victims = world.getEntitiesByClass(LivingEntity.class,
				center.getBoundingBox().expand(2.5), e -> e != center && e != getOwner() && e.isAlive());
		DamageSource source = getDamageSources().create(ModDamageTypes.PLASMA, this, getOwner());
		for (LivingEntity victim : victims) {
			victim.damage(source, damage * 0.6f);
		}
	}

	private void applyShrink(Entity target) {
		if (!(target instanceof LivingEntity living)) return;
		if (target instanceof AbominationEntity) return; // bosses don't shrink
		if (target instanceof PlayerEntity) return; // players too (fair isn't fun here)
		dev.benluvzbacon.rickmorty.event.ShrinkTracker.shrink(living,
				target.getWorld() instanceof ServerWorld sw ? sw.getServer().getTicks() * 0L + sw.getTime() : 0);
		living.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 1200, 1, false, true), getOwner());
		if (getWorld() instanceof ServerWorld sw) {
			sw.spawnParticles(ParticleTypes.REVERSE_PORTAL, living.getX(), living.getY() + 0.5, living.getZ(),
					40, 0.4, 0.7, 0.4, 0.05);
		}
	}

	@Override
	protected void onBlockHit(BlockHitResult hit) {
		super.onBlockHit(hit);
		if (!getWorld().isClient) {
			if (kind == Kind.PLASMA) {
				((ServerWorld) getWorld()).spawnParticles(ParticleTypes.WITCH, getX(), getY(), getZ(),
						15, 0.3, 0.3, 0.3, 0.05);
			}
			discard();
		}
	}

	@Override
	protected void onCollision(HitResult hitResult) {
		super.onCollision(hitResult);
		if (!getWorld().isClient && hitResult.getType() != HitResult.Type.MISS && kind == Kind.PLASMA) {
			if (getWorld() instanceof ServerWorld sw) {
				sw.spawnParticles(ParticleTypes.FLASH, getX(), getY(), getZ(), 1, 0, 0, 0, 0);
			}
		}
	}

	@Override
	public boolean shouldRender(double distance) {
		return true;
	}
}
