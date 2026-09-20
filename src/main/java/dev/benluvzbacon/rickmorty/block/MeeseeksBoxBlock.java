package dev.benluvzbacon.rickmorty.block;

import com.mojang.serialization.MapCodec;
import dev.benluvzbacon.rickmorty.blockentity.MeeseeksBoxBlockEntity;
import dev.benluvzbacon.rickmorty.registry.ModSounds;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class MeeseeksBoxBlock extends BlockWithEntity {
	public static final MapCodec<MeeseeksBoxBlock> CODEC = createCodec(MeeseeksBoxBlock::new);

	public MeeseeksBoxBlock(Settings settings) {
		super(settings);
	}

	@Override
	protected MapCodec<? extends BlockWithEntity> getCodec() {
		return CODEC;
	}

	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new MeeseeksBoxBlockEntity(pos, state);
	}

	@Override
	protected BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}

	@Override
	protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		if (world.isClient) return ActionResult.SUCCESS;
		BlockEntity be = world.getBlockEntity(pos);
		if (!(be instanceof MeeseeksBoxBlockEntity box)) return ActionResult.PASS;
		if (box.summon((ServerWorld) world, pos, player)) {
			world.playSound(null, pos, ModSounds.MEESEEKS_SPAWN, SoundCategory.BLOCKS, 1.0f, 1.0f);
			player.sendMessage(Text.translatable("msg.rickmorty.meeseeks.summoned").formatted(Formatting.AQUA), true);
		} else {
			player.sendMessage(Text.translatable("msg.rickmorty.meeseeks.cooldown",
					box.getCooldownSeconds(world.getTime())).formatted(Formatting.YELLOW), true);
		}
		return ActionResult.CONSUME;
	}

	@Override
	public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
		if (random.nextInt(20) == 0) {
			double dx = pos.getX() + random.nextDouble();
			double dz = pos.getZ() + random.nextDouble();
			world.addParticle(net.minecraft.particle.ParticleTypes.END_ROD, dx, pos.getY() + 1.0, dz,
					0, 0.05, 0);
		}
	}
}
