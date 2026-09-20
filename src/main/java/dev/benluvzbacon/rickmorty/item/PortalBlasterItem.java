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
 * Portal Blaster: rapid mid-tier gun. Durability-based, no ammo required.
 */
public class PortalBlasterItem extends Item {
	public PortalBlasterItem(Settings settings) {
		super(settings.maxCount(1).maxDamage(300));
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);
		if (user.getItemCooldownManager().isCoolingDown(this)) return TypedActionResult.fail(stack);
		user.setCurrentHand(hand);
		if (!world.isClient) {
			EnergyBoltEntity bolt = EnergyBoltEntity.firedBy(user, EnergyBoltEntity.Kind.PORTAL, 6.0f, 1.0f);
			world.spawnEntity(bolt);
			world.playSound(null, user.getBlockPos(), ModSounds.BLASTER_SHOT, SoundCategory.PLAYERS,
					0.8f, 0.9f + user.getRandom().nextFloat() * 0.3f);
			if (!user.getAbilities().creativeMode) {
				stack.damage(1, user, net.minecraft.entity.EquipmentSlot.MAINHAND);
			}
			user.getItemCooldownManager().set(this, 7);
		}
		return TypedActionResult.consume(stack);
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.add(Text.translatable("tooltip.rickmorty.portal_blaster").formatted(Formatting.DARK_GRAY));
	}
}
