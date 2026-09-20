package dev.benluvzbacon.rickmorty.blockentity;

import dev.benluvzbacon.rickmorty.config.ModConfig;
import dev.benluvzbacon.rickmorty.entity.MeeseeksEntity;
import dev.benluvzbacon.rickmorty.registry.ModBlockEntities;
import dev.benluvzbacon.rickmorty.registry.ModEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class MeeseeksBoxBlockEntity extends BlockEntity {
	private long cooldownUntil;

	public MeeseeksBoxBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.MEESEEKS_BOX_ENTITY, pos, state);
	}

	public boolean isCharged(long worldTime) {
		return worldTime >= cooldownUntil;
	}

	/** Spawns a Meeseeks next to the box. False while recharging. */
	public boolean summon(ServerWorld world, BlockPos pos, net.minecraft.entity.player.PlayerEntity player) {
		if (!isCharged(world.getTime())) return false;
		MeeseeksEntity meeseeks = ModEntities.MEESEEKS.create(world, null, null, pos.up(), SpawnReason.TRIGGERED, true, false);
		if (meeseeks == null) {
			meeseeks = new MeeseeksEntity(ModEntities.MEESEEKS, world);
			meeseeks.refreshPositionAndAngles(pos.up(), world.random.nextFloat() * 360f, 0);
			world.spawnEntity(meeseeks);
		}
		meeseeks.assignRandomTask(player);
		cooldownUntil = world.getTime() + (long) ModConfig.get().meeseeksLifetimeSeconds * 40;
		markDirty();
		return true;
	}

	public int getCooldownSeconds(long worldTime) {
		return (int) Math.max(0, (cooldownUntil - worldTime) / 20);
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
