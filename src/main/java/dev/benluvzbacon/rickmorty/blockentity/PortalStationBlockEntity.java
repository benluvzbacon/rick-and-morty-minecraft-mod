package dev.benluvzbacon.rickmorty.blockentity;

import dev.benluvzbacon.rickmorty.registry.ModBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;

/**
 * Portal Machine / Portal Station. A fixed teleporter powered by portal fluid canisters.
 * Overworld station -> Citadel station; Citadel station -> Overworld spawn.
 */
public class PortalStationBlockEntity extends BlockEntity {
	public static final int MAX_FLUID = 2000;
	public static final int COST_PER_TRIP = 500;

	private int fluidAmount;
	private long cooldownUntil;

	public PortalStationBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.PORTAL_STATION_ENTITY, pos, state);
	}

	public int getFluidAmount() {
		return fluidAmount;
	}

	public int addFluid(int amount) {
		int accept = Math.max(0, Math.min(amount, MAX_FLUID - fluidAmount));
		fluidAmount += accept;
		if (accept > 0) markDirty();
		return accept;
	}

	public boolean tryConsume(int amount) {
		if (fluidAmount < amount) return false;
		fluidAmount -= amount;
		markDirty();
		return true;
	}

	public boolean isCoolingDown(long worldTime) {
		return worldTime < cooldownUntil;
	}

	public void setCooldown(long worldTime, int ticks) {
		cooldownUntil = worldTime + ticks;
		markDirty();
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
		super.readNbt(nbt, registries);
		fluidAmount = nbt.getInt("fluid");
		cooldownUntil = nbt.getLong("cooldown");
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
		super.writeNbt(nbt, registries);
		nbt.putInt("fluid", fluidAmount);
		nbt.putLong("cooldown", cooldownUntil);
	}
}
