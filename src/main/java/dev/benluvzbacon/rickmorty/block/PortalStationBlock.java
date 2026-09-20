package dev.benluvzbacon.rickmorty.block;

import com.mojang.serialization.MapCodec;
import dev.benluvzbacon.rickmorty.blockentity.PortalStationBlockEntity;
import dev.benluvzbacon.rickmorty.portal.TeleportUtil;
import dev.benluvzbacon.rickmorty.portal.PortalLogic;
import dev.benluvzbacon.rickmorty.registry.ModBlocks;
import dev.benluvzbacon.rickmorty.registry.ModDimensions;
import dev.benluvzbacon.rickmorty.registry.ModItems;
import dev.benluvzbacon.rickmorty.registry.ModSounds;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * Portal Machine: fixed portal station. Load with portal fluid canisters; step in with
 * an empty hand use to jump to the Citadel (and back home to spawn from the Citadel).
 */
public class PortalStationBlock extends BlockWithEntity {
	public static final MapCodec<PortalStationBlock> CODEC = createCodec(PortalStationBlock::new);

	// Deterministic home for the Citadel-end station.
	public static final BlockPos CITADEL_STATION_POS = new BlockPos(8, 65, 8);

	public PortalStationBlock(Settings settings) {
		super(settings);
	}

	@Override
	protected MapCodec<? extends BlockWithEntity> getCodec() {
		return CODEC;
	}

	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new PortalStationBlockEntity(pos, state);
	}

	@Override
	protected BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}

	@Override
	protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos,
											 PlayerEntity player, Hand hand, BlockHitResult hit) {
		BlockEntity be = world.getBlockEntity(pos);
		if (!(be instanceof PortalStationBlockEntity station)) return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		if (!stack.isOf(ModItems.FILLED_PORTAL_FLUID_CANISTER)) return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		if (world.isClient) return ItemActionResult.SUCCESS;
		int accepted = station.addFluid(500);
		if (accepted > 0) {
			stack.decrement(1);
			ItemStack empty = new ItemStack(ModItems.PORTAL_FLUID_CANISTER);
			if (!player.getInventory().insertStack(empty)) player.dropItem(empty, false);
			world.playSound(null, pos, ModSounds.PORTAL_OPEN, SoundCategory.BLOCKS, 0.6f, 1.3f);
			player.sendMessage(Text.translatable("msg.rickmorty.station.charged",
					station.getFluidAmount(), PortalStationBlockEntity.MAX_FLUID).formatted(Formatting.GREEN), true);
		} else {
			player.sendMessage(Text.translatable("msg.rickmorty.station.full").formatted(Formatting.YELLOW), true);
		}
		return ItemActionResult.CONSUME;
	}

	@Override
	protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		if (world.isClient) return ActionResult.SUCCESS;
		BlockEntity be = world.getBlockEntity(pos);
		if (!(be instanceof PortalStationBlockEntity station)) return ActionResult.PASS;
		MinecraftServer server = world.getServer();
		if (server == null) return ActionResult.PASS;

		if (station.isCoolingDown(world.getTime())) {
			player.sendMessage(Text.translatable("msg.rickmorty.station.cooldown").formatted(Formatting.YELLOW), true);
			return ActionResult.CONSUME;
		}
		boolean inCitadel = world.getRegistryKey() == ModDimensions.CITADEL_WORLD;
		ServerWorld dest = inCitadel ? server.getOverworld() : server.getWorld(ModDimensions.CITADEL_WORLD);
		if (dest == null) return ActionResult.PASS;

		if (!station.tryConsume(PortalStationBlockEntity.COST_PER_TRIP)) {
			player.sendMessage(Text.translatable("msg.rickmorty.station.needs_fluid",
					station.getFluidAmount(), PortalStationBlockEntity.COST_PER_TRIP).formatted(Formatting.RED), true);
			return ActionResult.CONSUME;
		}

		double tx, ty, tz;
		if (inCitadel) {
			BlockPos spawn = server.getOverworld().getSpawnPos();
			tx = spawn.getX();
			ty = spawn.getY();
			tz = spawn.getZ();
		} else {
			ensureCitadelStation(dest);
			tx = CITADEL_STATION_POS.getX() + 0.5;
			ty = CITADEL_STATION_POS.getY();
			tz = CITADEL_STATION_POS.getZ() + 1.5;
		}
		PortalLogic.spawnFx((ServerWorld) world, pos);
		boolean ok = TeleportUtil.teleportSafe(player, dest, tx, ty, tz, player.getYaw());
		if (ok) {
			station.setCooldown(world.getTime(), 100);
			PortalLogic.spawnFx(dest, player.getBlockPos());
			PortalLogic.playTeleportSound(dest, player.getBlockPos());
			player.sendMessage(Text.translatable(inCitadel
					? "msg.rickmorty.station.home"
					: "msg.rickmorty.station.citadel").formatted(Formatting.AQUA), true);
		} else {
			// refund on failure
			station.addFluid(PortalStationBlockEntity.COST_PER_TRIP);
			player.sendMessage(Text.translatable("msg.rickmorty.portal.no_safe_spot").formatted(Formatting.RED), true);
		}
		return ActionResult.CONSUME;
	}

	/** Makes sure the Citadel side has a station, a floor, and breathable space. */
	private static void ensureCitadelStation(ServerWorld citadel) {
		citadel.getChunk(CITADEL_STATION_POS.getX() >> 4, CITADEL_STATION_POS.getZ() >> 4);
		for (int dx = -1; dx <= 1; dx++) {
			for (int dz = -1; dz <= 1; dz++) {
				citadel.setBlockState(CITADEL_STATION_POS.down().add(dx, 0, dz),
						ModBlocks.CITADEL_METAL.getDefaultState());
				for (int dy = 1; dy <= 3; dy++) {
					BlockPos clear = CITADEL_STATION_POS.add(dx, dy, dz);
					if (!citadel.getBlockState(clear).isAir()) citadel.breakBlock(clear, false);
				}
			}
		}
		BlockState existing = citadel.getBlockState(CITADEL_STATION_POS);
		if (!existing.isOf(ModBlocks.PORTAL_MACHINE)) {
			citadel.setBlockState(CITADEL_STATION_POS, ModBlocks.PORTAL_MACHINE.getDefaultState());
			BlockEntity be = citadel.getBlockEntity(CITADEL_STATION_POS);
			if (be instanceof PortalStationBlockEntity station) {
				station.addFluid(PortalStationBlockEntity.MAX_FLUID); // courtesy fill so you can get home
			}
		}
	}
}
