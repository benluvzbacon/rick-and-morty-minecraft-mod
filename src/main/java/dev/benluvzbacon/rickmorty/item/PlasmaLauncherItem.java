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
 * Plasma Launcher: slow heavy orbs with splash damage. Eats Plasma Cells.
 * Expensive to feed because it deletes crowds.
 */
public class PlasmaLauncherItem extends Item {
	public PlasmaLauncherItem(Settings settings) {
		super(settings.maxCount(1).maxDamage(80));
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);
		if (user.getItemCooldownManager().isCoolingDown(this)) return TypedActionResult.fail(stack);
		boolean creative = user.getAbilities().creativeMode;
		if (!creative && !hasAmmo(user)) {
			user.sendMessage(Text.translatable("msg.rickmorty.gun.no_plasma").formatted(Formatting.RED), true);
			return TypedActionResult.fail(stack);
		}
		if (!world.isClient) {
			if (!creative) consumeAmmo(user);
			EnergyBoltEntity orb = EnergyBoltEntity.firedBy(user, EnergyBoltEntity.Kind.PLASMA, 14.0f, 0.3f);
			world.spawnEntity(orb);
			world.playSound(null, user.getBlockPos(), ModSounds.PLASMA_SHOT, SoundCategory.PLAYERS, 1.0f, 0.85f);
			user.addVelocity(-user.getRotationVector().x * 0.25, 0, -user.getRotationVector().z * 0.25);
			if (user instanceof net.minecraft.server.network.ServerPlayerEntity sp) sp.velocityModified = true;
			if (!creative) stack.damage(1, user, net.minecraft.entity.EquipmentSlot.MAINHAND);
			user.getItemCooldownManager().set(this, 24);
		}
		user.setCurrentHand(hand);
		return TypedActionResult.success(stack);
	}

	private boolean hasAmmo(PlayerEntity user) {
		return user.getInventory().contains(new ItemStack(ModItems.PLASMA_CELL));
	}

	private void consumeAmmo(PlayerEntity user) {
		var inv = user.getInventory();
		for (int i = 0; i < inv.size(); i++) {
			ItemStack s = inv.getStack(i);
			if (s.isOf(ModItems.PLASMA_CELL)) {
				s.decrement(1);
				return;
			}
		}
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.add(Text.translatable("tooltip.rickmorty.plasma_launcher").formatted(Formatting.DARK_GRAY));
	}
}
