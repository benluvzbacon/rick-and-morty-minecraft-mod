package dev.benluvzbacon.rickmorty.portal;

import com.mojang.serialization.MapCodec;
import dev.benluvzbacon.rickmorty.registry.ModBlockEntities;
import dev.benluvzbacon.rickmorty.registry.ModSounds;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.state.StateManager;
import net.minecraft.block.Block;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * A swirling interdimensional portal. Walking into it teleports you safely.
 * Color variants: green (primary) and blue (secondary) for linked portal pairs,
 * plus purple for dimension portals.
 */
public class PortalBlock extends BlockWithEntity {
	public static final MapCodec<PortalBlock> CODEC = createCodec(PortalBlock::new);
	public static final EnumProperty<PortalColor> COLOR = EnumProperty.of("color", PortalColor.class);

	public enum PortalColor implements net.minecraft.util.StringIdentifiable {
		GREEN("green"), BLUE("blue"), PURPLE("purple");
		private final String name;
		PortalColor(String name) { this.name = name; }
		@Override public String asString() { return name; }
	}

	public PortalBlock(Settings settings) {
		super(settings);
		setDefaultState(getStateManager().getDefaultState().with(COLOR, PortalColor.GREEN));
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(COLOR);
	}

	@Override
	protected MapCodec<? extends BlockWithEntity> getCodec() {
		return CODEC;
	}

	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new PortalBlockEntity(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
		return net.minecraft.block.BlockWithEntity.checkType(type, ModBlockEntities.PORTAL_BLOCK_ENTITY, PortalBlockEntity::tick);
	}

	@Override
	protected BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}

	@Override
	protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
		if (world.isClient || !(entity instanceof LivingEntity)) return;
		BlockEntity be = world.getBlockEntity(pos);
		if (be instanceof PortalBlockEntity portal) {
			PortalLogic.tryUsePortal(portal, entity);
		}
	}

	@Override
	public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
		// Swirling particles (client side).
		for (int i = 0; i < 3; i++) {
			double dx = pos.getX() + random.nextDouble();
			double dy = pos.getY() + random.nextDouble() * 1.6;
			double dz = pos.getZ() + random.nextDouble();
			world.addParticle(net.minecraft.particle.ParticleTypes.PORTAL, dx, dy, dz,
					(random.nextDouble() - 0.5) * 0.4, 0.1, (random.nextDouble() - 0.5) * 0.4);
		}
		if (random.nextInt(200) == 0) {
			world.playSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
					ModSounds.PORTAL_OPEN, SoundCategory.BLOCKS, 0.25f, 1.6f + random.nextFloat() * 0.4f, false);
		}
	}

	/** Places (or replaces) a portal block pair head + feet handled as single block. */
	public static PortalBlockEntity place(net.minecraft.world.StructureWorldAccess world, BlockPos pos, PortalColor color) {
		BlockState state = dev.benluvzbacon.rickmorty.registry.ModBlocks.PORTAL_BLOCK.getDefaultState().with(COLOR, color);
		world.setBlockState(pos, state, Block.NOTIFY_ALL);
		BlockEntity be = world.getBlockEntity(pos);
		return be instanceof PortalBlockEntity p ? p : null;
	}
}
