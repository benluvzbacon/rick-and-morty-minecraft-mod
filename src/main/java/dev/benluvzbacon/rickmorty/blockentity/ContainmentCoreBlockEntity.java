package dev.benluvzbacon.rickmorty.blockentity;

import dev.benluvzbacon.rickmorty.entity.boss.AbominationEntity;
import dev.benluvzbacon.rickmorty.registry.ModBlockEntities;
import dev.benluvzbacon.rickmorty.registry.ModEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

/**
 * The containment core found inside experimental facilities. Feeding it an
 * Interdimensional Crystal wakes up what Rick left behind.
 */
public class ContainmentCoreBlockEntity extends BlockEntity {
	public static final long COOLDOWN_TICKS = 20 * 60 * 10; // 10 minutes
	private long cooldownUntil;

	public ContainmentCoreBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.CONTAINMENT_CORE_ENTITY, pos, state);
	}

	public boolean canRelease(ServerWorld world) {
		if (world.getTime() < cooldownUntil) return false;
		// only one abomination at a time in the area
		return world.getEntitiesByClass(AbominationEntity.class,
				net.minecraft.util.math.Box.of(pos.toCenterPos(), 96, 96, 96), e -> true).isEmpty();
	}

	public AbominationEntity release(ServerWorld world, BlockPos pos) {
		AbominationEntity boss = new AbominationEntity(ModEntities.INTERDIMENSIONAL_ABOMINATION, world);
		boss.refreshPositionAndAngles(pos.up(2), world.random.nextFloat() * 360f, 0);
		boss.setBossHome(pos.toImmutable());
		world.spawnEntity(boss);
		cooldownUntil = world.getTime() + COOLDOWN_TICKS;
		markDirty();
		return boss;
	}

	public long getCooldownRemaining(long worldTime) {
		return Math.max(0, cooldownUntil - worldTime);
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
		super.readNbt(nbt, registries);
		cooldownUntil = nbt.getLong("cooldown");
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
		super.writeNbt(nbt, registries);
		nbt.putLong("cooldown", cooldownUntil);
	}
}
