package dev.benluvzbacon.rickmorty.block;

import com.mojang.serialization.MapCodec;
import dev.benluvzbacon.rickmorty.blockentity.ContainmentCoreBlockEntity;
import dev.benluvzbacon.rickmorty.registry.ModItems;
import dev.benluvzbacon.rickmorty.registry.ModSounds;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ContainmentCoreBlock extends BlockWithEntity {
	public static final MapCodec<ContainmentCoreBlock> CODEC = createCodec(ContainmentCoreBlock::new);

	public ContainmentCoreBlock(Settings settings) {
		super(settings);
	}

	@Override
	protected MapCodec<? extends BlockWithEntity> getCodec() {
		return CODEC;
	}

	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new ContainmentCoreBlockEntity(pos, state);
	}

	@Override
	protected BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}

	@Override
	protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos,
											 PlayerEntity player, Hand hand, BlockHitResult hit) {
		if (!stack.isOf(ModItems.INTERDIMENSIONAL_CRYSTAL)) return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		if (world.isClient) return ItemActionResult.SUCCESS;
		BlockEntity be = world.getBlockEntity(pos);
		if (!(be instanceof ContainmentCoreBlockEntity core)) return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		ServerWorld sw = (ServerWorld) world;

		if (!core.canRelease(sw)) {
			long remain = core.getCooldownRemaining(sw.getTime());
			player.sendMessage(Text.translatable(remain > 0
							? "msg.rickmorty.core.cooldown"
							: "msg.rickmorty.core.occupied").formatted(Formatting.YELLOW), true);
			return ItemActionResult.CONSUME;
		}
		stack.decrement(1);
		core.release(sw, pos);
		sw.playSound(null, pos, ModSounds.BOSS_ROAR, SoundCategory.HOSTILE, 2.0f, 0.6f);
		sw.playSound(null, pos, SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.HOSTILE, 1.0f, 0.5f);
		sw.spawnParticles(ParticleTypes.EXPLOSION_EMITTER, pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5,
				1, 0, 0, 0, 0);
		player.sendMessage(Text.translatable("msg.rickmorty.core.released").formatted(Formatting.DARK_RED), false);
		return ItemActionResult.CONSUME;
	}
}
