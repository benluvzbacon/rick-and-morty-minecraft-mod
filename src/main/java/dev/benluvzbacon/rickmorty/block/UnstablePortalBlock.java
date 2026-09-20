package dev.benluvzbacon.rickmorty.block;

import com.mojang.serialization.MapCodec;
import dev.benluvzbacon.rickmorty.blockentity.UnstablePortalBlockEntity;
import dev.benluvzbacon.rickmorty.registry.ModBlockEntities;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class UnstablePortalBlock extends BlockWithEntity {
	public static final MapCodec<UnstablePortalBlock> CODEC = createCodec(UnstablePortalBlock::new);

	public UnstablePortalBlock(Settings settings) {
		super(settings);
	}

	@Override
	protected MapCodec<? extends BlockWithEntity> getCodec() {
		return CODEC;
	}

	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new UnstablePortalBlockEntity(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
		return (w, p2, st, be) -> { if (be instanceof UnstablePortalBlockEntity pbe) UnstablePortalBlockEntity.tick(w, p2, st, pbe); };
	}

	@Override
	protected BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}

	@Override
	protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
		if (world.isClient) return;
		BlockEntity be = world.getBlockEntity(pos);
		if (be instanceof UnstablePortalBlockEntity rift) {
			rift.touchEntity(entity);
		}
	}

	@Override
	public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
		for (int i = 0; i < 4; i++) {
			double dx = pos.getX() + random.nextDouble();
			double dy = pos.getY() + random.nextDouble() * 1.8;
			double dz = pos.getZ() + random.nextDouble();
			world.addParticle(net.minecraft.particle.ParticleTypes.WITCH, dx, dy, dz,
					(random.nextDouble() - 0.5) * 0.3, (random.nextDouble() - 0.5) * 0.3, (random.nextDouble() - 0.5) * 0.3);
		}
	}
}
