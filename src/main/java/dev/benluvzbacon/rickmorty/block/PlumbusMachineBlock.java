package dev.benluvzbacon.rickmorty.block;

import com.mojang.serialization.MapCodec;
import dev.benluvzbacon.rickmorty.blockentity.PlumbusMachineBlockEntity;
import dev.benluvzbacon.rickmorty.registry.ModBlockEntities;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class PlumbusMachineBlock extends BlockWithEntity {
	public static final MapCodec<PlumbusMachineBlock> CODEC = createCodec(PlumbusMachineBlock::new);

	public PlumbusMachineBlock(Settings settings) {
		super(settings);
	}

	@Override
	protected MapCodec<? extends BlockWithEntity> getCodec() {
		return CODEC;
	}

	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new PlumbusMachineBlockEntity(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
		return (w, p2, st, be) -> { if (be instanceof PlumbusMachineBlockEntity pbe) PlumbusMachineBlockEntity.tick(w, p2, st, pbe); };
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
		if (be instanceof PlumbusMachineBlockEntity machine) {
			if (player.isSneaking()) {
				machine.extractAll(player);
				return ItemActionResult.CONSUME;
			}
			int accepted = machine.insert(player, stack, 1);
			if (accepted > 0) {
				return ItemActionResult.CONSUME;
			}
		}
		return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}

	@Override
	protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		if (world.isClient) return ActionResult.SUCCESS;
		BlockEntity be = world.getBlockEntity(pos);
		if (be instanceof PlumbusMachineBlockEntity machine) {
			if (player.isSneaking()) {
				machine.extractAll(player);
			} else {
				StringBuilder sb = new StringBuilder();
				var inv = machine.getInventory();
				for (int i = 0; i < 3; i++) {
					var s = inv.getStack(i);
					sb.append(s.isEmpty() ? "-" : s.getCount() + "x " + s.getName().getString());
					if (i < 2) sb.append(" | ");
				}
				var out = inv.getStack(3);
				sb.append(out.isEmpty() ? "" : "  [" + out.getCount() + "x " + out.getName().getString() + " ready]");
				player.sendMessage(Text.translatable("msg.rickmorty.plumbus.status", sb.toString())
						.formatted(Formatting.LIGHT_PURPLE), true);
			}
		}
		return ActionResult.CONSUME;
	}
}
