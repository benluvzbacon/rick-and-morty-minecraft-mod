package dev.benluvzbacon.rickmorty.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

/**
 * Interdimensional Storage: a pocket-space locker that keeps its contents linked to
 * you, no matter which dimension you open it from. (Shares your ender-chest space.)
 */
public class InterdimensionalStorageBlock extends Block {
	private static final Text TITLE = Text.translatable("block.rickmorty.interdimensional_storage");

	public InterdimensionalStorageBlock(Settings settings) {
		super(settings);
	}

	@Override
	protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		if (!world.isClient) {
			EnderChestInventory ender = player.getEnderChestInventory();
			player.openHandledScreen(new SimpleNamedScreenHandlerFactory(
					(syncId, inventory, p) -> GenericContainerScreenHandler.createGeneric9x3(syncId, inventory, ender),
					TITLE));
			player.incrementStat(Stats.OPEN_ENDERCHEST);
		}
		return ActionResult.SUCCESS;
	}

	@Override
	public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
		if (random.nextInt(4) == 0) {
			world.addParticle(net.minecraft.particle.ParticleTypes.REVERSE_PORTAL,
					pos.getX() + random.nextDouble(), pos.getY() + random.nextDouble(),
					pos.getZ() + random.nextDouble(), 0, 0.02, 0);
		}
	}
}
