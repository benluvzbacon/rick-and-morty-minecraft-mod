package dev.benluvzbacon.rickmorty.blockentity;

import dev.benluvzbacon.rickmorty.block.AlienReactorBlock;
import dev.benluvzbacon.rickmorty.registry.ModBlockEntities;
import dev.benluvzbacon.rickmorty.registry.ModItems;
import dev.benluvzbacon.rickmorty.registry.ModSounds;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

/**
 * Burns Alien Crystals and distills portal fluid into an adjacent Portal Fluid Tank.
 */
public class AlienReactorBlockEntity extends BlockEntity {
	private final SimpleInventory inventory = new SimpleInventory(1);
	private int progress;

	public AlienReactorBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.ALIEN_REACTOR_ENTITY, pos, state);
	}

	public SimpleInventory getInventory() {
		return inventory;
	}

	/** Insert crystals from the player's held stack. Returns true if something was inserted. */
	public boolean insertCrystals(PlayerEntity player, ItemStack stack) {
		if (!stack.isOf(ModItems.ALIEN_CRYSTAL)) return false;
		ItemStack slot = inventory.getStack(0);
		if (!slot.isEmpty() && !ItemStack.areItemsAndComponentsEqual(slot, stack)) return false;
		int move = Math.min(stack.getCount(), slot.getMaxCount() - slot.getCount());
		if (move <= 0) return false;
		if (slot.isEmpty()) {
			inventory.setStack(0, stack.copyWithCount(move));
		} else {
			slot.increment(move);
		}
		inventory.markDirty();
		markDirty();
		if (!player.getAbilities().creativeMode) stack.decrement(move);
		return true;
	}

	public void extractAll(PlayerEntity player) {
		for (int i = 0; i < inventory.size(); i++) {
			ItemStack s = inventory.getStack(i);
			if (!s.isEmpty()) {
				player.getInventory().offerOrDrop(s.copy());
				inventory.setStack(i, ItemStack.EMPTY);
			}
		}
		inventory.markDirty();
		markDirty();
	}

	public static void tick(World world, BlockPos pos, BlockState state, AlienReactorBlockEntity be) {
		if (world.isClient) return;
		ItemStack fuel = be.inventory.getStack(0);
		if (fuel.isEmpty() || !fuel.isOf(ModItems.ALIEN_CRYSTAL)) {
			be.progress = 0;
			return;
		}
		FluidTankBlockEntity tank = be.findTank(world, pos);
		if (tank == null || tank.getFluidAmount() >= tank.getCapacity()) {
			be.progress = 0;
			return;
		}
		be.progress++;
		if (be.progress % 40 == 0 && world instanceof ServerWorld sw) {
			sw.spawnParticles(ParticleTypes.WITCH, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
					3, 0.3, 0.2, 0.3, 0.01);
			world.playSound(null, pos, ModSounds.REACTOR_HUM, SoundCategory.BLOCKS, 0.5f, 1.0f);
		}
		if (be.progress >= 200) {
			be.progress = 0;
			fuel.decrement(1);
			tank.addFluid(250);
			be.inventory.markDirty();
			be.markDirty();
		}
		if (be.progress > 0 && world.getTime() % 20 == 0 && state.contains(AlienReactorBlock.ACTIVE)) {
			if (!state.get(AlienReactorBlock.ACTIVE)) {
				world.setBlockState(pos, state.with(AlienReactorBlock.ACTIVE, true), BlockState.NOTIFY_ALL);
			}
		}
	}

	private FluidTankBlockEntity findTank(World world, BlockPos pos) {
		for (Direction dir : Direction.values()) {
			BlockEntity other = world.getBlockEntity(pos.offset(dir));
			if (other instanceof FluidTankBlockEntity tank) return tank;
		}
		return null;
	}

	public Text getStatusText() {
		ItemStack fuel = inventory.getStack(0);
		return Text.translatable("msg.rickmorty.reactor.status", fuel.getCount(), progress / 10)
				.formatted(Formatting.GREEN);
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
		super.readNbt(nbt, registries);
		DefaultedList<ItemStack> stacks = DefaultedList.ofSize(inventory.size(), ItemStack.EMPTY);
		Inventories.readNbt(nbt, stacks, registries);
		for (int i = 0; i < stacks.size(); i++) inventory.setStack(i, stacks.get(i));
		progress = nbt.getInt("progress");
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
		super.writeNbt(nbt, registries);
		DefaultedList<ItemStack> stacks = DefaultedList.ofSize(inventory.size(), ItemStack.EMPTY);
		for (int i = 0; i < inventory.size(); i++) stacks.set(i, inventory.getStack(i));
		Inventories.writeNbt(nbt, stacks, registries);
		nbt.putInt("progress", progress);
	}
}
