package dev.benluvzbacon.rickmorty.blockentity;

import dev.benluvzbacon.rickmorty.config.ModConfig;
import dev.benluvzbacon.rickmorty.registry.ModBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;

public class FluidTankBlockEntity extends BlockEntity {
	private int fluidAmount; // in mB

	public FluidTankBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.FLUID_TANK_ENTITY, pos, state);
	}

	public int getFluidAmount() {
		return fluidAmount;
	}

	public int getCapacity() {
		return ModConfig.get().tankCapacity;
	}

	public int getLevel() {
		int cap = getCapacity();
		if (cap <= 0 || fluidAmount <= 0) return 0;
		return 1 + (int) Math.floor(7.0 * fluidAmount / cap);
	}

	/** Adds fluid, returns the amount actually accepted. */
	public int addFluid(int amount) {
		int accept = Math.max(0, Math.min(amount, getCapacity() - fluidAmount));
		fluidAmount += accept;
		if (accept > 0) syncState();
		return accept;
	}

	/** Removes fluid, returns the amount actually removed. */
	public int drain(int amount) {
		int taken = Math.max(0, Math.min(amount, fluidAmount));
		fluidAmount -= taken;
		if (taken > 0) syncState();
		return taken;
	}

	private void syncState() {
		markDirty();
		if (world != null && !world.isClient) {
			BlockState state = getCachedState();
			int level = getLevel();
			if (state.get(dev.benluvzbacon.rickmorty.block.FluidTankBlock.LEVEL) != level) {
				world.setBlockState(pos, state.with(dev.benluvzbacon.rickmorty.block.FluidTankBlock.LEVEL, level), net.minecraft.block.Block.NOTIFY_ALL);
			}
		}
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
		super.readNbt(nbt, registries);
		fluidAmount = nbt.getInt("fluid");
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
		super.writeNbt(nbt, registries);
		nbt.putInt("fluid", fluidAmount);
	}
}
