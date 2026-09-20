package dev.benluvzbacon.rickmorty.entity.boss;

import dev.benluvzbacon.rickmorty.entity.CronenbergEntity;
import dev.benluvzbacon.rickmorty.entity.MortyEntity;
import dev.benluvzbacon.rickmorty.entity.projectile.EnergyBoltEntity;
import dev.benluvzbacon.rickmorty.portal.TeleportUtil;
import dev.benluvzbacon.rickmorty.registry.ModEntities;
import dev.benluvzbacon.rickmorty.registry.ModSounds;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * The Interdimensional Abomination: the thing Rick's experiment became.
 * Three phases: ranged barrage -> summons minions -> teleports + frenzy.
 */
public class AbominationEntity extends HostileEntity {
	private final ServerBossBar bossBar = new ServerBossBar(
			Text.translatable("entity.rickmorty.interdimensional_abomination"),
			BossBar.Color.PURPLE, BossBar.Style.PROGRESS);
	@Nullable
	private BlockPos bossHome;
	private int attackCooldown = 80;
	private int summonCooldown = 200;
	private int teleportCooldown = 160;

	public AbominationEntity(EntityType<? extends HostileEntity> type, World world) {
		super(type, world);
		this.experiencePoints = 250;
		bossBar.setDarkenSky(false);
	}

	public static DefaultAttributeContainer.Builder createAttributes() {
		return HostileEntity.createHostileAttributes()
				.add(EntityAttributes.GENERIC_MAX_HEALTH, 300.0)
				.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.24)
				.add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 14.0)
				.add(EntityAttributes.GENERIC_ARMOR, 10.0)
				.add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1.0)
				.add(EntityAttributes.GENERIC_FOLLOW_RANGE, 48.0);
	}

	@Override
	protected void initGoals() {
		this.goalSelector.add(0, new SwimGoal(this));
		this.goalSelector.add(2, new MeleeAttackGoal(this, 1.0, false));
		this.goalSelector.add(5, new WanderAroundFarGoal(this, 0.7));
		this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 24.0f));
		this.goalSelector.add(7, new LookAroundGoal(this));
		this.targetSelector.add(1, new RevengeGoal(this));
		this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
		this.targetSelector.add(3, new ActiveTargetGoal<>(this, MortyEntity.class, true));
	}

	public void setBossHome(BlockPos pos) {
		this.bossHome = pos;
	}

	public int getPhase() {
		float frac = getHealth() / getMaxHealth();
		if (frac > 0.66f) return 1;
		if (frac > 0.33f) return 2;
		return 3;
	}

	@Override
	protected void mobTick() {
		super.mobTick();
		if (!(getWorld() instanceof ServerWorld world)) return;

		bossBar.setPercent(getHealth() / getMaxHealth());
		for (ServerPlayerEntity player : world.getPlayers(p -> p.distanceTo(this) < 64)) {
			bossBar.addPlayer(player);
		}

		int phase = getPhase();
		if (attackCooldown > 0) attackCooldown--;
		if (summonCooldown > 0) summonCooldown--;
		if (teleportCooldown > 0) teleportCooldown--;

		LivingEntity target = getTarget();
		if (target == null || !target.isAlive()) return;

		// Phase 1 & 3: ranged barrage
		if ((phase == 1 || phase == 3) && attackCooldown <= 0 && canSee(target) && distanceTo(target) < 32) {
			int shots = phase == 3 ? 5 : 3;
			for (int i = 0; i < shots; i++) {
				EnergyBoltEntity bolt = EnergyBoltEntity.plasmaBarrage(this, target, 7.0f);
				getWorld().spawnEntity(bolt);
			}
			playSound(ModSounds.BOSS_HURT, 1.2f, 1.2f);
			attackCooldown = phase == 3 ? 50 : 80;
		}

		// Phase 2: summon minions
		if (phase >= 2 && summonCooldown <= 0) {
			int existing = world.getEntitiesByClass(CronenbergEntity.class,
					getBoundingBox().expand(32), e -> true).size();
			if (existing < 6) {
				for (int i = 0; i < 2 + world.random.nextInt(2); i++) {
					CronenbergEntity minion = new CronenbergEntity(ModEntities.CRONENBERG_MUTANT, world);
					double a = world.random.nextDouble() * Math.PI * 2;
					minion.refreshPositionAndAngles(getX() + Math.cos(a) * 3, getY(), getZ() + Math.sin(a) * 3,
							world.random.nextFloat() * 360, 0);
					world.spawnEntity(minion);
					minion.setTarget(target);
				}
				world.spawnParticles(ParticleTypes.EXPLOSION_EMITTER, getX(), getY() + 2, getZ(), 1, 0, 0, 0, 0);
				playSound(ModSounds.BOSS_ROAR, 2.0f, 0.7f);
			}
			summonCooldown = 300;
		}

		// Phase 3: flicker teleports to stay close and terrifying
		if (phase == 3 && teleportCooldown <= 0) {
			double a = world.random.nextDouble() * Math.PI * 2;
			double tx = target.getX() + Math.cos(a) * 6;
			double tz = target.getZ() + Math.sin(a) * 6;
			world.spawnParticles(ParticleTypes.PORTAL, getX(), getY() + 1.5, getZ(), 80, 0.6, 1.2, 0.6, 0.4);
			boolean ok = TeleportUtil.teleportSafe(this, world, tx, tz, getYaw());
			if (ok) {
				world.spawnParticles(ParticleTypes.PORTAL, getX(), getY() + 1.5, getZ(), 80, 0.6, 1.2, 0.6, 0.4);
				playSound(ModSounds.RICK_TELEPORT, 1.5f, 0.5f);
			}
			teleportCooldown = 140;
		}
	}

	@Override
	public void onStartedTrackingBy(ServerPlayerEntity player) {
		super.onStartedTrackingBy(player);
		bossBar.addPlayer(player);
	}

	@Override
	public void onStoppedTrackingBy(ServerPlayerEntity player) {
		super.onStoppedTrackingBy(player);
		bossBar.removePlayer(player);
	}

	@Override
	public void onDeath(DamageSource source) {
		bossBar.setPercent(0f);
		bossBar.clearPlayers();
		super.onDeath(source);
		if (getWorld() instanceof ServerWorld world) {
			world.spawnParticles(ParticleTypes.EXPLOSION_EMITTER, getX(), getY() + 2, getZ(), 3, 1, 1, 1, 0);
			world.playSound(null, getBlockPos(), ModSounds.BOSS_ROAR,
					net.minecraft.sound.SoundCategory.HOSTILE, 2.0f, 0.4f);
			for (ServerPlayerEntity player : world.getPlayers(p -> p.distanceTo(this) < 96)) {
				player.sendMessage(Text.translatable("msg.rickmorty.boss.defeated")
						.formatted(Formatting.GOLD), false);
			}
		}
	}

	@Override
	public void checkDespawn() {
		if (getWorld().isClient) return;
		// bosses don't despawn
	}

	@Override
	public boolean isPersistent() {
		return true;
	}

	@Override
	public void writeCustomDataToNbt(net.minecraft.nbt.NbtCompound nbt) {
		super.writeCustomDataToNbt(nbt);
		if (bossHome != null) nbt.putLong("bossHome", bossHome.asLong());
	}

	@Override
	public void readCustomDataFromNbt(net.minecraft.nbt.NbtCompound nbt) {
		super.readCustomDataFromNbt(nbt);
		if (nbt.contains("bossHome")) bossHome = BlockPos.fromLong(nbt.getLong("bossHome"));
	}
}
