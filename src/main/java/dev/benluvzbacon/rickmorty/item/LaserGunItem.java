package dev.benluvzbacon.rickmorty.item;

import dev.benluvzbacon.rickmorty.entity.projectile.EnergyBoltEntity;
import dev.benluvzbacon.rickmorty.registry.ModItems;
import dev.benluvzbacon.rickmorty.registry.ModSounds;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;

/**
 * Laser Gun: hard-hitting precision shots, but every trigger pull consumes an
 * Energy Cell from your inventory.
 */
public class LaserGunItem extends Item {
	public LaserGunItem(Settings settings) {
		super(settings.maxCount(1).maxDamage(200));
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);
		if (user.getItemCooldownManager().isCoolingDown(this)) return TypedActionResult.fail(stack);
		boolean creative = user.getAbilities().creativeMode;
		if (!creative && !hasAmmo(user)) {
			user.sendMessage(Text.translatable("msg.rickmorty.gun.no_cells").formatted(Formatting.RED), true);
			return TypedActionResult.fail(stack);
		}
		if (!world.isClient) {
			if (!creative) consumeAmmo(user);
			EnergyBoltEntity bolt = EnergyBoltEntity.firedBy(user, EnergyBoltEntity.Kind.LASER, 9.0f, 0.1f);
			world.spawnEntity(bolt);
			world.playSound(null, user.getBlockPos(), ModSounds.LASER_SHOT, SoundCategory.PLAYERS, 0.9f, 1.0f);
			if (!creative) stack.damage(1, user, net.minecraft.entity.EquipmentSlot.MAINHAND);
			user.getItemCooldownManager().set(this, 14);
		}
		user.setCurrentHand(hand);
		return TypedActionResult.success(stack);
	}

	private boolean hasAmmo(PlayerEntity user) {
		return user.getInventory().contains(new ItemStack(ModItems.ENERGY_CELL));
	}

	private void consumeAmmo(PlayerEntity user) {
		var inv = user.getInventory();
		for (int i = 0; i < inv.size(); i++) {
			ItemStack s = inv.getStack(i);
			if (s.isOf(ModItems.ENERGY_CELL)) {
				s.decrement(1);
				return;
			}
		}
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.add(Text.translatable("tooltip.rickmorty.laser_gun").formatted(Formatting.DARK_GRAY));
	}
}
