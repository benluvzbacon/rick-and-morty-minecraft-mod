package dev.benluvzbacon.rickmorty.item;

import dev.benluvzbacon.rickmorty.entity.projectile.EnergyBoltEntity;
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
 * Shrink Ray: makes the target 50% smaller (and slower) for 30 seconds.
 * Works on most mobs, not on bosses or players.
 */
public class ShrinkRayItem extends Item {
	public ShrinkRayItem(Settings settings) {
		super(settings.maxCount(1).maxDamage(64));
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);
		if (user.getItemCooldownManager().isCoolingDown(this)) return TypedActionResult.fail(stack);
		if (!world.isClient) {
			EnergyBoltEntity bolt = EnergyBoltEntity.shrinkBolt(user);
			world.spawnEntity(bolt);
			world.playSound(null, user.getBlockPos(), ModSounds.SHRINK_ZAP, SoundCategory.PLAYERS, 0.9f, 1.3f);
			if (!user.getAbilities().creativeMode) {
				stack.damage(1, user, net.minecraft.entity.EquipmentSlot.MAINHAND);
			}
			user.getItemCooldownManager().set(this, 30);
		}
		user.setCurrentHand(hand);
		return TypedActionResult.success(stack);
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.add(Text.translatable("tooltip.rickmorty.shrink_ray").formatted(Formatting.DARK_GRAY));
	}
}
