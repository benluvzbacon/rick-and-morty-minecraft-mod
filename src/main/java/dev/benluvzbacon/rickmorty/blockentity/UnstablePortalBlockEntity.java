package dev.benluvzbacon.rickmorty.blockentity;

import dev.benluvzbacon.rickmorty.registry.ModBlockEntities;
import dev.benluvzbacon.rickmorty.registry.ModEntities;
import dev.benluvzbacon.rickmorty.registry.ModItems;
import dev.benluvzbacon.rickmorty.registry.ModSounds;
import dev.benluvzbacon.rickmorty.portal.TeleportUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Wild rift created during portal storms. Spits out the occasional alien creature,
 * small-teleports anything that touches it, then collapses into a Rift Shard.
 */
public class UnstablePortalBlockEntity extends BlockEntity {
	private int remainingTicks;
	private int spawnsLeft;

	public UnstablePortalBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.UNSTABLE_PORTAL_ENTITY, pos, state);
	}

	public void configure(int lifetimeTicks, int spawns) {
		this.remainingTicks = lifetimeTicks;
		this.spawnsLeft = spawns;
		markDirty();
	}

	public static void tick(World world, BlockPos pos, BlockState state, UnstablePortalBlockEntity be) {
		if (!(world instanceof ServerWorld sw)) return;
		be.remainingTicks--;
		if (be.remainingTicks <= 0) {
			collapse(sw, pos, be);
			return;
		}
		if (be.spawnsLeft > 0 && world.getTime() % 50 == 0 && world.random.nextInt(3) != 0) {
			be.trySpawnMob(sw, pos);
		}
	}

	private void trySpawnMob(ServerWorld world, BlockPos pos) {
		// cap: max 4 hostiles nearby to avoid runaway spawns
		int nearby = world.getEntitiesByClass(net.minecraft.entity.mob.HostileEntity.class,
				net.minecraft.util.math.Box.of(pos.toCenterPos(), 32, 16, 32), e -> true).size();
		if (nearby >= 4) return;
		EntityType<?> type = switch (world.random.nextInt(3)) {
			case 0 -> ModEntities.ALIEN_CRAWLER;
			case 1 -> ModEntities.PARASITE;
			default -> ModEntities.CRONENBERG_MUTANT;
		};
		Entity entity = type.spawn(world, pos.up(), SpawnReason.EVENT);
		if (entity != null) {
			spawnsLeft--;
			markDirty();
			world.playSound(null, pos, ModSounds.ANOMALY, SoundCategory.BLOCKS, 0.6f, 1.3f);
		}
	}

	private static void collapse(ServerWorld world, BlockPos pos, UnstablePortalBlockEntity be) {
		world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
		world.playSound(null, pos, ModSounds.PORTAL_CLOSE, SoundCategory.BLOCKS, 0.7f, 0.8f);
		world.spawnParticles(ParticleTypes.WITCH, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
				30, 0.5, 1.0, 0.5, 0.05);
		if (world.random.nextBoolean()) {
			ItemEntity item = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
					new ItemStack(ModItems.RIFT_SHARD, 1 + world.random.nextInt(2)));
			world.spawnEntity(item);
		}
	}

	/** Random short-range teleport for anything that steps inside. Spicy but safe. */
	public void touchEntity(Entity entity) {
		if (!(entity.getWorld() instanceof ServerWorld sw)) return;
		double dx = (sw.random.nextDouble() - 0.5) * 24;
		double dz = (sw.random.nextDouble() - 0.5) * 24;
		TeleportUtil.teleportSafe(entity, sw, entity.getX() + dx, entity.getZ() + dz, entity.getYaw());
		sw.playSound(null, entity.getBlockPos(), ModSounds.TELEPORT, SoundCategory.PLAYERS, 0.7f, 1.4f);
		if (entity instanceof LivingEntity living) {
			living.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
					net.minecraft.entity.effect.StatusEffects.NAUSEA, 100, 0, true, false));
		}
		if (entity instanceof PlayerEntity) {
			// stepping into a rift scares the box open: small chance to collapse early
			remainingTicks = Math.min(remainingTicks, remainingTicks - 20);
			markDirty();
		}
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
		super.readNbt(nbt, registries);
		remainingTicks = nbt.getInt("remaining");
		spawnsLeft = nbt.getInt("spawnsLeft");
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
		super.writeNbt(nbt, registries);
		nbt.putInt("remaining", remainingTicks);
		nbt.putInt("spawnsLeft", spawnsLeft);
	}
}
