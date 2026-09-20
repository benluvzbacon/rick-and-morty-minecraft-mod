package dev.benluvzbacon.rickmorty.item;

import dev.benluvzbacon.rickmorty.network.JetpackServer;
import dev.benluvzbacon.rickmorty.registry.ModItems;
import dev.benluvzbacon.rickmorty.registry.ModSounds;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;

/**
 * Jetpack chestpiece. Toggle with the jetpack key (default J). Fuel: portal fluid —
 * refuel in-hand by spending a Filled Portal Fluid Canister. Softens falls while worn.
 */
public class JetpackItem extends Item {
	public static final int MAX_FUEL = 800;

	public JetpackItem(Settings settings) {
		super(settings.maxCount(1));
	}

	private static NbtCompound tag(ItemStack stack) {
		NbtComponent comp = stack.get(DataComponentTypes.CUSTOM_DATA);
		return comp == null ? new NbtCompound() : comp.copyNbt();
	}

	private static void tag(ItemStack stack, NbtCompound nbt) {
		stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
	}

	public static int getFuel(ItemStack stack) {
		return tag(stack).getInt("fuel");
	}

	public static void setFuel(ItemStack stack, int fuel) {
		NbtCompound nbt = tag(stack);
		nbt.putInt("fuel", Math.max(0, Math.min(MAX_FUEL, fuel)));
		tag(stack, nbt);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);
		if (!world.isClient && getFuel(stack) <= MAX_FUEL - 400) {
			// fuel up with a canister
			var inv = user.getInventory();
			for (int i = 0; i < inv.size(); i++) {
				ItemStack s = inv.getStack(i);
				if (s.isOf(ModItems.FILLED_PORTAL_FLUID_CANISTER)) {
					s.decrement(1);
					ItemStack empty = new ItemStack(ModItems.PORTAL_FLUID_CANISTER);
					if (!inv.insertStack(empty)) user.dropItem(empty, false);
					setFuel(stack, getFuel(stack) + 400);
					user.sendMessage(Text.translatable("msg.rickmorty.jetpack.refueled",
							getFuel(stack), MAX_FUEL).formatted(Formatting.GREEN), true);
					world.playSound(null, user.getBlockPos(), ModSounds.PORTAL_OPEN, SoundCategory.PLAYERS, 0.4f, 1.7f);
					return TypedActionResult.success(stack);
				}
			}
			user.sendMessage(Text.translatable("msg.rickmorty.jetpack.need_canister").formatted(Formatting.YELLOW), true);
		}
		return TypedActionResult.success(stack);
	}

	@Override
	public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
		super.inventoryTick(stack, world, entity, slot, selected);
		if (!(entity instanceof ServerPlayerEntity player) || world.isClient) return;
		// flight happens only while equipped on the chest
		ItemStack chest = player.getEquippedStack(EquipmentSlot.CHEST);
		if (chest != stack) return;

		boolean active = JetpackServer.isActive(player);
		int fuel = getFuel(stack);
		boolean rising = false;
		if (active && fuel > 0 && !player.isOnGround()) {
			// thrust
			if (player.getVelocity().y < 0.55) {
				player.addVelocity(0, 0.115, 0);
				player.velocityModified = true;
				rising = true;
			}
			if (world.getTime() % 2 == 0) setFuel(stack, fuel - 1);
			if (world.getTime() % 3 == 0 && world instanceof ServerWorld sw) {
				sw.spawnParticles(ParticleTypes.FLAME, player.getX(), player.getY() + 0.1, player.getZ(),
						2, 0.15, 0.1, 0.15, 0.01);
			}
			if (world.getTime() % 14 == 0) {
				world.playSound(null, player.getBlockPos(), ModSounds.REACTOR_HUM, SoundCategory.PLAYERS, 0.25f, 2.0f);
			}
		}
		// jetpack-assisted landing: soften falls while fueled and active
		if (active && getFuel(stack) > 0) {
			player.fallDistance = Math.min(player.fallDistance, 2.0f);
		}
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		int fuel = getFuel(stack);
		tooltip.add(Text.translatable("tooltip.rickmorty.jetpack.fuel",
				Text.literal(fuel + " / " + MAX_FUEL).formatted(fuel > 0 ? Formatting.GREEN : Formatting.RED))
				.formatted(Formatting.GRAY));
		tooltip.add(Text.translatable("tooltip.rickmorty.jetpack.1").formatted(Formatting.DARK_GRAY));
		tooltip.add(Text.translatable("tooltip.rickmorty.jetpack.2").formatted(Formatting.DARK_GRAY));
	}
}
