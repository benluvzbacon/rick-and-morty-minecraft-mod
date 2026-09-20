package dev.benluvzbacon.rickmorty.item;

import dev.benluvzbacon.rickmorty.config.ModConfig;
import dev.benluvzbacon.rickmorty.portal.PortalBlock;
import dev.benluvzbacon.rickmorty.portal.PortalBlockEntity;
import dev.benluvzbacon.rickmorty.portal.PortalLogic;
import dev.benluvzbacon.rickmorty.portal.PortalWorldState;
import dev.benluvzbacon.rickmorty.registry.ModBlocks;
import dev.benluvzbacon.rickmorty.registry.ModDimensions;
import dev.benluvzbacon.rickmorty.registry.ModItems;
import dev.benluvzbacon.rickmorty.registry.ModSounds;
import net.minecraft.block.BlockState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * Rick's Portal Gun. The crown jewel of the mod.
 *
 *  - Right-click a block: fire a portal. Alternate shots alternate green/blue linked
 *    portals (walk in one, come out the other, momentum included).
 *  - Sneak + right-click: cycle the selected dimension for dimension portals.
 *  - Sneak + fire at a block: place a DIMENSION portal to the selected dimension.
 *  - Fluid-powered: fill it at a Portal Fluid Tank or combine it with a canister.
 *  - A Portal Stabilizer in the offhand reduces fluid cost and doubles portal life.
 */
public class PortalGunItem extends Item {
	public PortalGunItem(Settings settings) {
		super(settings.maxCount(1));
	}

	// ------------------------------------------------------------ NBT helpers

	private static NbtCompound tag(ItemStack stack) {
		NbtComponent comp = stack.get(DataComponentTypes.CUSTOM_DATA);
		return comp == null ? new NbtCompound() : comp.copyNbt();
	}

	private static void tag(ItemStack stack, NbtCompound nbt) {
		stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
	}

	public static int getFluid(ItemStack stack) {
		return tag(stack).getInt("fluid");
	}

	public static int addFluid(ItemStack stack, int amount) {
		NbtCompound nbt = tag(stack);
		int cur = nbt.getInt("fluid");
		int cap = ModConfig.get().portalGunCapacity;
		int add = Math.max(0, Math.min(amount, cap - cur));
		nbt.putInt("fluid", cur + add);
		tag(stack, nbt);
		return add;
	}

	public static int getDimIndex(ItemStack stack) {
		return tag(stack).getInt("dim");
	}

	public static RegistryKey<World> getSelectedDimension(ItemStack stack) {
		int idx = getDimIndex(stack);
		List<RegistryKey<World>> list = ModDimensions.TRAVELABLE;
		return list.get(Math.floorMod(idx, list.size()));
	}

	public static UUID getGunId(ItemStack stack) {
		NbtCompound nbt = tag(stack);
		if (!nbt.containsUuid("gunId")) {
			nbt.putUuid("gunId", UUID.randomUUID());
			tag(stack, nbt);
		}
		UUID id;
		if (nbt.containsUuid("gunId")) {
			id = nbt.getUuid("gunId");
		} else {
			id = UUID.randomUUID();
			nbt.putUuid("gunId", id);
			tag(stack, nbt);
		}
		return id;
	}

	public static boolean isStabilized(PlayerEntity player) {
		return player.getOffHandStack().isOf(ModItems.PORTAL_STABILIZER);
	}

	// ------------------------------------------------------------ behavior

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);
		if (world.isClient) return TypedActionResult.success(stack);

		getGunId(stack); // ensure identity
		if (user.getItemCooldownManager().isCoolingDown(stack.getItem())) {
			return TypedActionResult.fail(stack);
		}

		BlockHitResult hit = user.raycast(16, 0, false) instanceof BlockHitResult b ? b : null;
		boolean hasBlock = hit.getType() == HitResult.Type.BLOCK;
		boolean sneaking = user.isSneaking();

		if (sneaking && !hasBlock) {
			// cycle dimension selection
			NbtCompound nbt = tag(stack);
			int next = Math.floorMod(nbt.getInt("dim") + 1, ModDimensions.TRAVELABLE.size());
			nbt.putInt("dim", next);
			tag(stack, nbt);
			RegistryKey<World> key = ModDimensions.TRAVELABLE.get(next);
			user.sendMessage(Text.translatable("msg.rickmorty.gun.target",
					Text.translatable("msg.rickmorty.dim." + key.getValue().getPath()).formatted(Formatting.GREEN))
					.formatted(Formatting.AQUA), true);
			world.playSound(null, user.getBlockPos(), ModSounds.PORTAL_SHOOT, SoundCategory.PLAYERS, 0.4f, 2.0f);
			setCooldown(user, stack, 6);
			return TypedActionResult.success(stack);
		}

		if (!hasBlock) {
			world.playSound(null, user.getBlockPos(), ModSounds.PORTAL_SHOOT, SoundCategory.PLAYERS, 0.4f, 1.6f);
			return TypedActionResult.success(stack);
		}

		// firing at a block: need fluid
		boolean stabilized = isStabilized(user);
		int cost = ModConfig.get().portalFluidPerPortal;
		if (stabilized) cost = (int) Math.ceil(cost * 0.75);
		if (!user.getAbilities().creativeMode) {
			if (getFluid(stack) < cost) {
				user.sendMessage(Text.translatable("msg.rickmorty.gun.no_fluid",
						getFluid(stack), cost).formatted(Formatting.RED), true);
				world.playSound(null, user.getBlockPos(), ModSounds.PORTAL_SHOOT, SoundCategory.PLAYERS, 0.4f, 0.6f);
				setCooldown(user, stack, 10);
				return TypedActionResult.fail(stack);
			}
		}

		BlockPos portalPos = hit.getBlockPos().offset(hit.getSide());
		// find a free spot for the portal
		portalPos = findFreeSpot(world, portalPos, hit);
		if (portalPos == null) {
			user.sendMessage(Text.translatable("msg.rickmorty.gun.no_space").formatted(Formatting.RED), true);
			return TypedActionResult.fail(stack);
		}

		if (sneaking) {
			fireDimensionPortal(world, user, stack, portalPos, stabilized);
		} else {
			firePairPortal(world, user, stack, portalPos, stabilized);
		}

		if (!user.getAbilities().creativeMode) addFluid(stack, -cost);
		setCooldown(user, stack, ModConfig.get().portalCooldownTicks);
		world.playSound(null, user.getBlockPos(), ModSounds.PORTAL_SHOOT, SoundCategory.PLAYERS, 0.9f, 1.0f);
		return TypedActionResult.success(stack);
	}

	private void setCooldown(PlayerEntity user, ItemStack stack, int ticks) {
		user.getItemCooldownManager().set(stack.getItem(), ticks);
	}

	@Nullable
	private BlockPos findFreeSpot(World world, BlockPos pos, BlockHitResult hit) {
		if (world.getBlockState(pos).isAir() && world.getBlockState(pos.up()).isAir()) return pos;
		if (world.getBlockState(pos).isAir()) return pos;
		BlockPos up = pos.up();
		if (world.getBlockState(up).isAir()) return up;
		BlockPos down = pos.down();
		if (!world.getBlockState(down).isAir() && world.getBlockState(pos).isAir()) return pos;
		return null;
	}

	private void firePairPortal(World world, PlayerEntity user, ItemStack stack, BlockPos pos, boolean stabilized) {
		UUID gunId = getGunId(stack);
		NbtCompound nbt = tag(stack);
		boolean nextIsA = !nbt.contains("lastA") || !nbt.getBoolean("lastA");
		nbt.putBoolean("lastA", nextIsA);
		tag(stack, nbt);

		PortalWorldState state = PortalWorldState.get(world.getServer());
		PortalWorldState.Link link = state.getOrCreate(gunId);

		// remove the old portal of this color
		RegistryKey<World> here = world.getRegistryKey();
		removeOldPortal(world, link, nextIsA);

		PortalBlock.PortalColor color = nextIsA ? PortalBlock.PortalColor.GREEN : PortalBlock.PortalColor.BLUE;
		PortalBlockEntity portal = PortalBlock.place(world, pos, color);
		if (portal == null) return;
		int lifetime = ModConfig.get().portalLifetimeTicks * (stabilized ? 2 : 1);
		portal.configure(gunId, null, lifetime);

		if (nextIsA) {
			state.recordA(gunId, here, pos);
		} else {
			state.recordB(gunId, here, pos);
		}
		world.playSound(null, pos, ModSounds.PORTAL_OPEN, SoundCategory.BLOCKS, 0.9f, nextIsA ? 1.0f : 1.2f);
		PortalLogic.spawnFx((ServerWorld) world, pos);
		boolean hasPartner = nextIsA ? link.hasB : link.hasA;
		user.sendMessage(Text.translatable(hasPartner
						? "msg.rickmorty.gun.linked"
						: "msg.rickmorty.gun.half_link",
				Text.translatable(nextIsA ? "msg.rickmorty.gun.green" : "msg.rickmorty.gun.blue")
						.formatted(nextIsA ? Formatting.GREEN : Formatting.BLUE)).formatted(Formatting.AQUA), true);
	}

	private void removeOldPortal(World world, PortalWorldState.Link link, boolean isA) {
		var server = world.getServer();
		if (server == null) return;
		var dim = isA ? link.dimA : link.dimB;
		var pos = isA ? link.posA : link.posB;
		if (dim == null || pos == null) return;
		ServerWorld oldWorld = server.getWorld(dim);
		if (oldWorld == null) return;
		oldWorld.getChunk(pos.getX() >> 4, pos.getZ() >> 4);
		BlockState existing = oldWorld.getBlockState(pos);
		if (existing.isOf(ModBlocks.PORTAL_BLOCK)) {
			oldWorld.setBlockState(pos, net.minecraft.block.Blocks.AIR.getDefaultState());
			oldWorld.playSound(null, pos, ModSounds.PORTAL_CLOSE, SoundCategory.BLOCKS, 0.5f, 1.0f);
		}
	}

	private void fireDimensionPortal(World world, PlayerEntity user, ItemStack stack, BlockPos pos, boolean stabilized) {
		RegistryKey<World> target = getSelectedDimension(stack);
		PortalBlockEntity portal = PortalBlock.place(world, pos, PortalBlock.PortalColor.PURPLE);
		if (portal == null) return;
		int lifetime = ModConfig.get().portalLifetimeTicks * (stabilized ? 2 : 1);
		portal.configure(null, target, lifetime);
		world.playSound(null, pos, ModSounds.PORTAL_OPEN, SoundCategory.BLOCKS, 1.0f, 0.8f);
		PortalLogic.spawnFx((ServerWorld) world, pos);
		user.sendMessage(Text.translatable("msg.rickmorty.gun.dim_portal",
				Text.translatable("msg.rickmorty.dim." + target.getValue().getPath()).formatted(Formatting.GREEN))
				.formatted(Formatting.AQUA), true);
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		int fluid = getFluid(stack);
		int cap = ModConfig.get().portalGunCapacity;
		tooltip.add(Text.translatable("tooltip.rickmorty.portal_gun.fluid",
				Text.literal(fluid + " / " + cap + " mB").formatted(fluid > 0 ? Formatting.GREEN : Formatting.RED))
				.formatted(Formatting.GRAY));
		RegistryKey<World> key = getSelectedDimension(stack);
		tooltip.add(Text.translatable("tooltip.rickmorty.portal_gun.dim",
				Text.translatable("msg.rickmorty.dim." + key.getValue().getPath()).formatted(Formatting.GREEN))
				.formatted(Formatting.GRAY));
		tooltip.add(Text.translatable("tooltip.rickmorty.portal_gun.1").formatted(Formatting.DARK_GRAY));
		tooltip.add(Text.translatable("tooltip.rickmorty.portal_gun.2").formatted(Formatting.DARK_GRAY));
		tooltip.add(Text.translatable("tooltip.rickmorty.portal_gun.3").formatted(Formatting.DARK_GRAY));
	}
}
