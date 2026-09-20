package dev.benluvzbacon.rickmorty.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

/**
 * Quantum Computer. Upgrades an adjacent Rick's Workbench to tier 2+ and otherwise
 * computes things beyond human understanding (prints a random qubit readout).
 */
public class QuantumComputerBlock extends Block {
	public QuantumComputerBlock(Settings settings) {
		super(settings);
	}

	@Override
	protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		if (!world.isClient) {
			String bits = Long.toBinaryString(world.random.nextLong());
			bits = bits.substring(0, Math.min(bits.length(), 16));
			player.sendMessage(Text.translatable("msg.rickmorty.quantum.readout", bits).formatted(Formatting.DARK_AQUA), true);
		}
		return ActionResult.SUCCESS;
	}

	@Override
	public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
		if (random.nextInt(8) == 0) {
			world.addParticle(net.minecraft.particle.ParticleTypes.PORTAL,
					pos.getX() + random.nextDouble(), pos.getY() + 1.1, pos.getZ() + random.nextDouble(),
					0, 0.1, 0);
		}
	}
}
