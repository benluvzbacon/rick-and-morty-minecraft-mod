package dev.benluvzbacon.rickmorty.blockentity;

import dev.benluvzbacon.rickmorty.registry.ModBlockEntities;
import dev.benluvzbacon.rickmorty.registry.ModItems;
import dev.benluvzbacon.rickmorty.registry.ModSounds;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Combines a Quantum Circuit + Alien Crystal + Slime Ball into one (1) genuine Plumbus.
 * Everyone has a Plumbus in their home.
 */
public class PlumbusMachineBlockEntity extends BlockEntity {
	private final SimpleInventory inventory = new SimpleInventory(4); // 0-2 inputs, 3 output
	private int progress;

	public PlumbusMachineBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.PLUMBUS_MACHINE_ENTITY, pos, state);
	}

	public SimpleInventory getInventory() {
		return inventory;
	}

	private static boolean isIngredient(ItemStack stack) {
		return stack.isOf(ModItems.QUANTUM_CIRCUIT) || stack.isOf(ModItems.ALIEN_CRYSTAL) || stack.isOf(Items.SLIME_BALL);
	}

	/** Insert held ingredients; returns count accepted. */
	public int insert(PlayerEntity player, ItemStack stack, int count) {
		if (!isIngredient(stack)) return 0;
		int accepted = 0;
		for (int i = 0; i < 3 && count > 0; i++) {
			ItemStack slot = inventory.getStack(i);
			if (slot.isEmpty()) {
				int move = Math.min(count, stack.getMaxCount());
				inventory.setStack(i, stack.copyWithCount(move));
				accepted += move;
				count -= move;
			} else if (ItemStack.areItemsAndComponentsEqual(slot, stack) && slot.getCount() < slot.getMaxCount()) {
				int move = Math.min(count, slot.getMaxCount() - slot.getCount());
				slot.increment(move);
				accepted += move;
				count -= move;
			}
		}
		if (accepted > 0) {
			inventory.markDirty();
			markDirty();
			if (!player.getAbilities().creativeMode) stack.decrement(accepted);
		}
		return accepted;
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

	private static boolean hasType(SimpleInventory inv, net.minecraft.item.Item item, int slots) {
		for (int i = 0; i < slots; i++) {
			if (inv.getStack(i).isOf(item)) return true;
		}
		return false;
	}

	private static void consumeOne(SimpleInventory inv, net.minecraft.item.Item item, int slots) {
		for (int i = 0; i < slots; i++) {
			if (inv.getStack(i).isOf(item)) {
				inv.getStack(i).decrement(1);
				return;
			}
		}
	}

	public static void tick(World world, BlockPos pos, BlockState state, PlumbusMachineBlockEntity be) {
		if (world.isClient) return;
		if (be.progress >= 200) {
			be.progress = 0;
			consumeOne(be.inventory, ModItems.QUANTUM_CIRCUIT, 3);
			consumeOne(be.inventory, ModItems.ALIEN_CRYSTAL, 3);
			consumeOne(be.inventory, Items.SLIME_BALL, 3);
			ItemStack plumbus = new ItemStack(ModItems.PLUMBUS);
			ItemStack out = be.inventory.getStack(3);
			if (out.isEmpty()) {
				be.inventory.setStack(3, plumbus);
			} else {
				out.increment(1);
			}
			be.inventory.markDirty();
			be.markDirty();
			world.playSound(null, pos, ModSounds.WORKBENCH_CRAFT, SoundCategory.BLOCKS, 0.9f, 1.2f);
			if (world instanceof ServerWorld sw) {
				sw.spawnParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
						12, 0.4, 0.4, 0.4, 0.02);
			}
		} else if (be.progress == 0 && canStart(be)) {
			ItemStack out = be.inventory.getStack(3);
			if (out.isEmpty() || (out.isOf(ModItems.PLUMBUS) && out.getCount() < out.getMaxCount())) {
				be.progress = 1;
				be.markDirty();
			}
		} else if (be.progress > 0) {
			if (!canStart(be)) {
				be.progress = 0;
			} else {
				be.progress++;
			}
		}
	}

	private static boolean canStart(PlumbusMachineBlockEntity be) {
		return hasType(be.inventory, ModItems.QUANTUM_CIRCUIT, 3)
				&& hasType(be.inventory, ModItems.ALIEN_CRYSTAL, 3)
				&& hasType(be.inventory, Items.SLIME_BALL, 3);
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
