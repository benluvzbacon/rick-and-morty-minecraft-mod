package dev.benluvzbacon.rickmorty.block;

import com.mojang.serialization.MapCodec;
import dev.benluvzbacon.rickmorty.blockentity.FluidTankBlockEntity;
import dev.benluvzbacon.rickmorty.item.PortalGunItem;
import dev.benluvzbacon.rickmorty.registry.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class FluidTankBlock extends BlockWithEntity {
	public static final MapCodec<FluidTankBlock> CODEC = createCodec(FluidTankBlock::new);
	public static final IntProperty LEVEL = IntProperty.of("level", 0, 8);

	public FluidTankBlock(Settings settings) {
		super(settings);
		setDefaultState(getStateManager().getDefaultState().with(LEVEL, 0));
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(LEVEL);
	}

	@Override
	protected MapCodec<? extends BlockWithEntity> getCodec() {
		return CODEC;
	}

	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new FluidTankBlockEntity(pos, state);
	}

	@Override
	protected BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}

	@Override
	protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos,
											 PlayerEntity player, Hand hand, BlockHitResult hit) {
		BlockEntity be = world.getBlockEntity(pos);
		if (!(be instanceof FluidTankBlockEntity tank)) return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		if (world.isClient) return ItemActionResult.SUCCESS;

		if (stack.getItem() instanceof PortalGunItem) {
			int moved = tank.drain(500);
			int added = PortalGunItem.addFluid(stack, moved);
			if (added < moved) tank.addFluid(moved - added);
			if (added > 0) {
				player.sendMessage(Text.translatable("msg.rickmorty.tank.fill_gun", added)
						.formatted(Formatting.GREEN), true);
				world.playSound(null, pos, SoundEvents.ITEM_BUCKET_FILL, SoundCategory.BLOCKS, 1.0f, 1.4f);
				return ItemActionResult.CONSUME;
			}
			player.sendMessage(Text.translatable("msg.rickmorty.tank.gun_full").formatted(Formatting.YELLOW), true);
			return ItemActionResult.CONSUME;
		}
		if (stack.isOf(ModItems.PORTAL_FLUID_CANISTER)) {
			int moved = tank.drain(500);
			if (moved >= 500) {
				stack.decrement(1);
				ItemStack filled = new ItemStack(dev.benluvzbacon.rickmorty.registry.ModItems.FILLED_PORTAL_FLUID_CANISTER);
				if (!player.getInventory().insertStack(filled)) player.dropItem(filled, false);
				world.playSound(null, pos, SoundEvents.ITEM_BUCKET_FILL, SoundCategory.BLOCKS, 1.0f, 1.1f);
				player.sendMessage(Text.translatable("msg.rickmorty.tank.fill_canister").formatted(Formatting.GREEN), true);
			} else {
				tank.addFluid(moved);
				player.sendMessage(Text.translatable("msg.rickmorty.tank.not_enough").formatted(Formatting.YELLOW), true);
			}
			return ItemActionResult.CONSUME;
		}
		if (stack.isOf(ModItems.FILLED_PORTAL_FLUID_CANISTER)) {
			int accepted = tank.addFluid(500);
			if (accepted >= 500) {
				stack.decrement(1);
				ItemStack empty = new ItemStack(ModItems.PORTAL_FLUID_CANISTER);
				if (!player.getInventory().insertStack(empty)) player.dropItem(empty, false);
				world.playSound(null, pos, SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 1.0f, 1.1f);
				player.sendMessage(Text.translatable("msg.rickmorty.tank.added", 500).formatted(Formatting.GREEN), true);
			} else {
				player.sendMessage(Text.translatable("msg.rickmorty.tank.full").formatted(Formatting.YELLOW), true);
			}
			return ItemActionResult.CONSUME;
		}
		return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}

	@Override
	protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		if (world.isClient) return ActionResult.SUCCESS;
		BlockEntity be = world.getBlockEntity(pos);
		if (be instanceof FluidTankBlockEntity tank) {
			player.sendMessage(Text.translatable("msg.rickmorty.tank.level",
					tank.getFluidAmount(), tank.getCapacity()).formatted(Formatting.AQUA), true);
		}
		return ActionResult.CONSUME;
	}
}
