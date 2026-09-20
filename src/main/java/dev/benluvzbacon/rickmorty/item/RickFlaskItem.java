package dev.benluvzbacon.rickmorty.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;

/**
 * Rick's Flask. Side effects may include: temporary invincibility, nausea, burping.
 */
public class RickFlaskItem extends Item {
	public RickFlaskItem(Settings settings) {
		super(settings.maxCount(16));
	}

	@Override
	public int getMaxUseTime(ItemStack stack, LivingEntity user) {
		return 32;
	}

	@Override
	public net.minecraft.util.UseAction getUseAction(ItemStack stack) {
		return net.minecraft.util.UseAction.DRINK;
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		return ItemUsage.consumeHeldItem(world, user, hand);
	}

	@Override
	public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
		if (!world.isClient) {
			user.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 400, 1, true, false, true));
			user.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 400, 0, true, false, true));
			user.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 200, 0, true, false, true));
		}
		if (user instanceof PlayerEntity player && !player.getAbilities().creativeMode) {
			stack.decrement(1);
			ItemStack bottle = new ItemStack(Items.GLASS_BOTTLE);
			if (!player.getInventory().insertStack(bottle)) player.dropItem(bottle, false);
		}
		return stack;
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.add(Text.translatable("tooltip.rickmorty.rick_flask").formatted(Formatting.DARK_GRAY));
	}
}
