package dev.benluvzbacon.rickmorty.item;

import dev.benluvzbacon.rickmorty.entity.projectile.GrapnelEntity;
import dev.benluvzbacon.rickmorty.registry.ModEntities;
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
 * Grappling Device: fires a hook; once it latches, you get reeled in. Sneak to let go.
 */
public class GrapplingHookItem extends Item {
	public GrapplingHookItem(Settings settings) {
		super(settings.maxCount(1).maxDamage(200));
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);
		if (user.getItemCooldownManager().isCoolingDown(this)) return TypedActionResult.fail(stack);
		if (!world.isClient) {
			// release any existing hook first
			boolean hadExisting = false;
			for (GrapnelEntity hook : world.getEntitiesByClass(GrapnelEntity.class,
					user.getBoundingBox().expand(64), h -> h.getOwner() == user)) {
				hook.release();
				hadExisting = true;
			}
			if (hadExisting && user.isSneaking()) {
				user.getItemCooldownManager().set(this, 6);
				return TypedActionResult.success(stack);
			}
			GrapnelEntity hook = new GrapnelEntity(ModEntities.GRAPNEL, world);
			hook.setOwner(user);
			hook.setPosition(user.getX(), user.getEyeY() - 0.1, user.getZ());
			hook.setVelocity(user, user.getPitch(), user.getYaw(), 0, 2.4f, 0.1f);
			world.spawnEntity(hook);
			world.playSound(null, user.getBlockPos(), ModSounds.GRAPPLE_FIRE, SoundCategory.PLAYERS, 0.9f, 1.0f);
			if (!user.getAbilities().creativeMode) {
				stack.damage(1, user, net.minecraft.entity.EquipmentSlot.MAINHAND);
			}
			user.getItemCooldownManager().set(this, 12);
		}
		user.setCurrentHand(hand);
		return TypedActionResult.success(stack);
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.add(Text.translatable("tooltip.rickmorty.grappling_hook").formatted(Formatting.DARK_GRAY));
	}
}
