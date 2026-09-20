package dev.benluvzbacon.rickmorty.screen;

import dev.benluvzbacon.rickmorty.blockentity.RickWorkbenchBlockEntity;
import dev.benluvzbacon.rickmorty.recipe.WorkbenchRecipe;
import dev.benluvzbacon.rickmorty.registry.ModBlocks;
import dev.benluvzbacon.rickmorty.registry.ModRecipes;
import dev.benluvzbacon.rickmorty.registry.ModScreenHandlers;
import dev.benluvzbacon.rickmorty.registry.ModSounds;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Optional;

public class RickWorkbenchScreenHandler extends ScreenHandler {
	private final RickWorkbenchBlockEntity bench;
	private final Inventory inventory;
	private final PropertyDelegate properties;
	private WorkbenchRecipe activeRecipe;

	// server-side ctor
	public RickWorkbenchScreenHandler(int syncId, PlayerInventory playerInventory, RickWorkbenchBlockEntity bench) {
		super(ModScreenHandlers.RICK_WORKBENCH, syncId);
		this.bench = bench;
		this.inventory = bench.getInventory();
		this.properties = new PropertyDelegate() {
			@Override
			public int get(int index) {
				return bench.computeTier();
			}

			@Override
			public void set(int index, int value) {
			}

			@Override
			public int size() {
				return 1;
			}
		};

		// crafting grid 0-8
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 3; col++) {
				this.addSlot(new Slot(inventory, row * 3 + col, 30 + col * 18, 17 + row * 18));
			}
		}
		// result slot 9
		this.addSlot(new ResultSlot(inventory, 9, 124, 35));

		// player inventory
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 9; col++) {
				this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
			}
		}
		for (int col = 0; col < 9; col++) {
			this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
		}

		this.addProperties(properties);

		inventory.addListener(inv -> updateResult());
		updateResult();
	}

	// client-side ctor
	public RickWorkbenchScreenHandler(int syncId, PlayerInventory playerInventory, BlockPos pos) {
		this(syncId, playerInventory, resolveBench(playerInventory, pos));
	}

	private static RickWorkbenchBlockEntity resolveBench(PlayerInventory playerInventory, BlockPos pos) {
		World world = playerInventory.player.getWorld();
		if (world.getBlockEntity(pos) instanceof RickWorkbenchBlockEntity bench) {
			return bench;
		}
		return new RickWorkbenchBlockEntity(pos, ModBlocks.RICK_WORKBENCH.getDefaultState());
	}

	public int getTier() {
		return properties.get(0);
	}

	public void updateResult() {
		World world = bench.getWorld();
		if (world == null) return;
		Optional<RecipeEntry<WorkbenchRecipe>> match = world.getRecipeManager()
				.listAllOfType(ModRecipes.WORKBENCH_TYPE).stream()
				.filter(entry -> entry.value().matches(craftGrid(), world))
				.findFirst();
		if (match.isPresent()) {
			WorkbenchRecipe recipe = match.get().value();
			if (recipe.getTier() <= bench.computeTier()) {
				this.activeRecipe = recipe;
				inventory.setStack(9, recipe.getResult().copy());
			} else {
				this.activeRecipe = recipe; // recipe visible but tier-blocked (GUI shows it)
				inventory.setStack(9, ItemStack.EMPTY);
			}
		} else {
			this.activeRecipe = null;
			inventory.setStack(9, ItemStack.EMPTY);
		}
	}

	/** the 3x3 crafting portion of the inventory */
	private Inventory craftGrid() {
		return inventory; // grid occupies slots 0-8; result slot 9 is ignored by recipes
	}

	public WorkbenchRecipe getActiveRecipe() {
		return activeRecipe;
	}

	public RickWorkbenchBlockEntity getBench() {
		return bench;
	}

	@Override
	public boolean canUse(PlayerEntity player) {
		return this.inventory.canPlayerUse(player);
	}

	@Override
	public ItemStack quickMove(PlayerEntity player, int slot) {
		ItemStack stack = ItemStack.EMPTY;
		Slot s = this.slots.get(slot);
		if (s.hasStack()) {
			ItemStack inSlot = s.getStack();
			stack = inSlot.copy();
			if (slot < 10) { // from grid/result into player inventory
				if (!this.insertItem(inSlot, 10, 46, true)) return ItemStack.EMPTY;
			} else { // from player inventory into grid
				if (!this.insertItem(inSlot, 0, 9, false)) return ItemStack.EMPTY;
			}
			if (inSlot.isEmpty()) {
				s.setStack(ItemStack.EMPTY);
			} else {
				s.markDirty();
			}
		}
		return stack;
	}

	class ResultSlot extends Slot {
		ResultSlot(Inventory inventory, int index, int x, int y) {
			super(inventory, index, x, y);
		}

		@Override
		public boolean canInsert(ItemStack stack) {
			return false;
		}

		@Override
		public void onTakeItem(PlayerEntity player, ItemStack stack) {
			if (activeRecipe == null) return;
			World world = bench.getWorld();
			if (world != null && !world.isClient) {
				for (int i = 0; i < 9; i++) {
					ItemStack g = inventory.getStack(i);
					if (!g.isEmpty()) {
						g.decrement(1);
					}
				}
				world.playSound(null, bench.getPos(), ModSounds.WORKBENCH_CRAFT, SoundCategory.BLOCKS, 0.8f, 1.0f);
			}
			updateResult();
			super.onTakeItem(player, stack);
		}
	}
}
