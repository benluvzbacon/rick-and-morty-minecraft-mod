package dev.benluvzbacon.rickmorty.block;

import com.mojang.serialization.MapCodec;
import dev.benluvzbacon.rickmorty.blockentity.AlienReactorBlockEntity;
import dev.benluvzbacon.rickmorty.registry.ModBlockEntities;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class AlienReactorBlock extends BlockWithEntity {
	public static final MapCodec<AlienReactorBlock> CODEC = createCodec(AlienReactorBlock::new);
	public static final BooleanProperty ACTIVE = BooleanProperty.of("active");

	public AlienReactorBlock(Settings settings) {
		super(settings);
		setDefaultState(getStateManager().getDefaultState().with(ACTIVE, false));
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(ACTIVE);
	}

	@Override
	protected MapCodec<? extends BlockWithEntity> getCodec() {
		return CODEC;
	}

	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new AlienReactorBlockEntity(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
		return (w, p2, st, be) -> { if (be instanceof AlienReactorBlockEntity pbe) AlienReactorBlockEntity.tick(w, p2, st, pbe); };
	}

	@Override
	protected BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}

	@Override
	protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos,
											 PlayerEntity player, Hand hand, BlockHitResult hit) {
		if (world.isClient) return ItemActionResult.SUCCESS;
		BlockEntity be = world.getBlockEntity(pos);
		if (be instanceof AlienReactorBlockEntity reactor && reactor.insertCrystals(player, stack)) {
			return ItemActionResult.CONSUME;
		}
		if (player.isSneaking() && be instanceof AlienReactorBlockEntity reactor) {
			reactor.extractAll(player);
			return ItemActionResult.CONSUME;
		}
		return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}

	@Override
	protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		if (world.isClient) return ActionResult.SUCCESS;
		BlockEntity be = world.getBlockEntity(pos);
		if (be instanceof AlienReactorBlockEntity reactor) {
			if (player.isSneaking()) {
				reactor.extractAll(player);
			} else {
				player.sendMessage(reactor.getStatusText(), true);
			}
		}
		return ActionResult.CONSUME;
	}
}
