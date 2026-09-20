package dev.benluvzbacon.rickmorty.item;

import dev.benluvzbacon.rickmorty.blockentity.FluidTankBlockEntity;
import dev.benluvzbacon.rickmorty.network.ModNetworking;
import dev.benluvzbacon.rickmorty.network.ScannerDataPayload;
import dev.benluvzbacon.rickmorty.portal.PortalBlockEntity;
import dev.benluvzbacon.rickmorty.registry.ModBlocks;
import dev.benluvzbacon.rickmorty.registry.ModSounds;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Interdimensional Scanner: sweeps the area for lifeforms, portal energy, rick-tech
 * and alien crystals, then reports (and caches the results for the GUI, default key V).
 */
public class ScannerItem extends Item {
	public ScannerItem(Settings settings) {
		super(settings.maxCount(1));
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);
		if (user.getItemCooldownManager().isCoolingDown(this)) return TypedActionResult.fail(stack);
		if (world instanceof ServerWorld) {
			performScan((ServerPlayerEntity) user);
			user.getItemCooldownManager().set(this, 40);
		}
		return TypedActionResult.success(stack);
	}

	public void performScan(ServerPlayerEntity player) {
		World world = player.getWorld();
		List<String> lines = new ArrayList<>();
		lines.add("=== INTERDIMENSIONAL SCAN ===");
		lines.add("Location: " + world.getRegistryKey().getValue() + " @ "
				+ player.getBlockX() + "," + player.getBlockY() + "," + player.getBlockZ());

		// lifeforms
		Map<String, Integer> census = new HashMap<>();
		for (Entity e : world.getOtherEntities(player, player.getBoundingBox().expand(32))) {
			if (e instanceof LivingEntity) {
				String name = e.getType().getName().getString();
				census.merge(name, 1, Integer::sum);
			}
		}
		if (census.isEmpty()) {
			lines.add("Lifeforms: none within 32m");
		} else {
			lines.add("Lifeforms (32m):");
			census.entrySet().stream()
					.sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
					.limit(6)
					.forEach(e -> lines.add("  - " + e.getKey() + " x" + e.getValue()));
		}

		// blocks of interest
		BlockPos crystal = scanForBlock(world, player, ModBlocks.ALIEN_CRYSTAL_ORE, 24);
		BlockPos pocketCrystal = scanForBlock(world, player, ModBlocks.POCKET_CRYSTAL_ORE, 24);
		int portals = 0;
		int tankFluid = 0;
		for (int dx = -16; dx <= 16; dx += 2) {
			for (int dy = -8; dy <= 8; dy += 2) {
				for (int dz = -16; dz <= 16; dz += 2) {
					BlockEntity be = world.getBlockEntity(player.getBlockPos().add(dx, dy, dz));
					if (be instanceof PortalBlockEntity) portals++;
					if (be instanceof FluidTankBlockEntity tank) tankFluid += tank.getFluidAmount();
				}
			}
		}
		lines.add("Portal signatures (16m): " + (portals > 0 ? portals + " active" : "none"));
		if (tankFluid > 0) lines.add("Portal fluid in nearby tanks: " + tankFluid + " mB");
		if (crystal != null) lines.add("Alien crystal: " + (int) Math.sqrt(player.getBlockPos().getSquaredDistance(crystal)) + "m");
		if (pocketCrystal != null) lines.add("Pocket crystal: " + (int) Math.sqrt(player.getBlockPos().getSquaredDistance(pocketCrystal)) + "m");
		lines.add("Threat level: " + threatName(census));

		world.playSound(null, player.getBlockPos(), ModSounds.SCANNER_PING, SoundCategory.PLAYERS, 0.9f, 1.3f);
		// actionbar summary + full data to the GUI cache
		String summary = census.isEmpty() ? "no lifeforms nearby" :
				census.entrySet().stream().max(Map.Entry.comparingByValue())
						.map(e -> e.getKey() + " x" + e.getValue()).orElse("?");
		player.sendMessage(Text.translatable("msg.rickmorty.scanner.short", summary)
				.formatted(Formatting.YELLOW), true);

		ServerPlayNetworking.send(player, new ScannerDataPayload(String.join("\n", lines)));
	}

	private static String threatName(Map<String, Integer> census) {
		if (census.isEmpty()) return "GREEN - calm (suspiciously so)";
		int hostiles = 0;
		for (var e : census.entrySet()) {
			if (e.getKey().toLowerCase().contains("cronenberg") || e.getKey().toLowerCase().contains("crawler")
					|| e.getKey().toLowerCase().contains("parasite") || e.getKey().toLowerCase().contains("brute")
					|| e.getKey().toLowerCase().contains("anomaly") || e.getKey().toLowerCase().contains("abomination")) {
				hostiles += e.getValue();
			}
		}
		if (hostiles >= 4) return "RED - oh geez oh man";
		if (hostiles >= 1) return "YELLOW - hostile signatures";
		return "GREEN - tolerable";
	}

	private BlockPos scanForBlock(World world, PlayerEntity player, net.minecraft.block.Block block, int radius) {
		BlockPos center = player.getBlockPos();
		ChunkPos start = new ChunkPos(center.add(-radius, 0, -radius));
		ChunkPos end = new ChunkPos(center.add(radius, 0, radius));
		BlockPos.Mutable p = new BlockPos.Mutable();
		for (int cx = start.x; cx <= end.x; cx++) {
			for (int cz = start.z; cz <= end.z; cz++) {
				for (int x = 0; x < 16; x++) {
					for (int z = 0; z < 16; z++) {
						int wx = cx * 16 + x;
						int wz = cz * 16 + z;
						if (Math.abs(wx - center.getX()) > radius || Math.abs(wz - center.getZ()) > radius) continue;
						for (int y = Math.max(world.getBottomY(), center.getY() - 24);
							 y < Math.min(world.getTopY(), center.getY() + 24); y++) {
							p.set(wx, y, wz);
							if (world.getBlockState(p).isOf(block)) return p.toImmutable();
						}
					}
				}
			}
		}
		return null;
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.add(Text.translatable("tooltip.rickmorty.scanner.1").formatted(Formatting.DARK_GRAY));
		tooltip.add(Text.translatable("tooltip.rickmorty.scanner.2").formatted(Formatting.DARK_GRAY));
	}
}
