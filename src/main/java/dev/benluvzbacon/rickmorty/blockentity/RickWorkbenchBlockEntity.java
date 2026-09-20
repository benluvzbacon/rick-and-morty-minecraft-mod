package dev.benluvzbacon.rickmorty.blockentity;

import dev.benluvzbacon.rickmorty.registry.ModBlockEntities;
import dev.benluvzbacon.rickmorty.registry.ModBlocks;
import dev.benluvzbacon.rickmorty.screen.RickWorkbenchScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

/**
 * Rick's Workbench: a 3x3 advanced crafting station. Its technology tier (1-5) is
 * driven by adjacent machines:
 *   T1 - the bench alone
 *   T2 - adjacent Quantum Computer
 *   T3 - + adjacent Portal Fluid Tank containing >= 1000 mB
 *   T4 - + adjacent Alien Reactor
 *   T5 - + adjacent Dark Matter Block
 */
public class RickWorkbenchBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory<BlockPos> {
	public static final int GRID_SIZE = 9;
	private final SimpleInventory inventory = new SimpleInventory(GRID_SIZE + 1); // 9 crafting + 1 result

	public RickWorkbenchBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.RICK_WORKBENCH_ENTITY, pos, state);
	}

	public SimpleInventory getInventory() {
		return inventory;
	}

	public int computeTier() {
		if (world == null) return 1;
		int tier = 1;
		boolean computer = false, tankFull = false, reactor = false, darkMatter = false;
		for (Direction dir : Direction.values()) {
			BlockPos p = pos.offset(dir);
			BlockState s = world.getBlockState(p);
			if (s.isOf(ModBlocks.QUANTUM_COMPUTER)) computer = true;
			if (s.isOf(ModBlocks.ALIEN_REACTOR)) reactor = true;
			if (s.isOf(ModBlocks.DARK_MATTER_BLOCK)) darkMatter = true;
			if (s.isOf(ModBlocks.PORTAL_FLUID_TANK)) {
				BlockEntity be = world.getBlockEntity(p);
				if (be instanceof FluidTankBlockEntity tank && tank.getFluidAmount() >= 1000) tankFull = true;
			}
		}
		if (computer) tier = 2;
		if (computer && tankFull) tier = 3;
		if (computer && tankFull && reactor) tier = 4;
		if (computer && tankFull && reactor && darkMatter) tier = 5;
		return tier;
	}

	@Override
	public Text getDisplayName() {
		return Text.translatable("block.rickmorty.rick_workbench");
	}

	@Override
	public BlockPos getScreenOpeningData(ServerPlayerEntity player) {
		return pos;
	}

	@Nullable
	@Override
	public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
		return new RickWorkbenchScreenHandler(syncId, playerInventory, this);
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
		super.readNbt(nbt, registries);
		DefaultedList<ItemStack> stacks = DefaultedList.ofSize(inventory.size(), ItemStack.EMPTY);
		Inventories.readNbt(nbt, stacks, registries);
		for (int i = 0; i < stacks.size(); i++) inventory.setStack(i, stacks.get(i));
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
		super.writeNbt(nbt, registries);
		DefaultedList<ItemStack> stacks = DefaultedList.ofSize(inventory.size(), ItemStack.EMPTY);
		for (int i = 0; i < inventory.size(); i++) stacks.set(i, inventory.getStack(i));
		Inventories.writeNbt(nbt, stacks, registries);
	}
}
