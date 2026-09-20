package dev.benluvzbacon.rickmorty.item;

import dev.benluvzbacon.rickmorty.config.ModConfig;
import dev.benluvzbacon.rickmorty.entity.boss.AbominationEntity;
import dev.benluvzbacon.rickmorty.registry.ModDamageTypes;
import dev.benluvzbacon.rickmorty.registry.ModSounds;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import java.util.List;

/**
 * Dimension Eraser. Experimental Rick-level technology: deletes what it touches.
 * Deliberately single-use-per-cooldown, expensive to build, harmless to bosses
 * (they're too big to erase - it just hurts them).
 */
public class DimensionEraserItem extends Item {
	public DimensionEraserItem(Settings settings) {
		super(settings.maxCount(1).maxDamage(12));
	}

	@Override
	public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
		if (!ModConfig.get().enableDimensionEraser) {
			user.sendMessage(Text.translatable("msg.rickmorty.eraser.disabled").formatted(Formatting.RED), true);
			return ActionResult.FAIL;
		}
		if (user.getItemCooldownManager().isCoolingDown(this)) return ActionResult.FAIL;
		World world = user.getWorld();
		if (!(world instanceof ServerWorld sw)) return ActionResult.SUCCESS;

		if (entity instanceof AbominationEntity) {
			entity.damage(sw, user.getDamageSources().create(ModDamageTypes.DIMENSION_ERASE, user), 40.0f);
			user.sendMessage(Text.translatable("msg.rickmorty.eraser.boss").formatted(Formatting.DARK_PURPLE), true);
		} else {
			entity.damage(sw, user.getDamageSources().create(ModDamageTypes.DIMENSION_ERASE, user), Float.MAX_VALUE / 4f);
			user.sendMessage(Text.translatable("msg.rickmorty.eraser.erased", entity.getName())
					.formatted(Formatting.DARK_PURPLE), true);
		}
		sw.spawnParticles(ParticleTypes.EXPLOSION_EMITTER, entity.getX(), entity.getY() + 0.7, entity.getZ(),
				1, 0, 0, 0, 0);
		sw.spawnParticles(ParticleTypes.REVERSE_PORTAL, entity.getX(), entity.getY() + 0.7, entity.getZ(),
				60, 0.5, 0.8, 0.5, 0.1);
		sw.playSound(null, entity.getBlockPos(), ModSounds.PORTAL_CLOSE, SoundCategory.PLAYERS, 1.2f, 0.4f);
		if (!user.getAbilities().creativeMode) {
			stack.damage(1, user, net.minecraft.entity.EquipmentSlot.MAINHAND);
		}
		user.getItemCooldownManager().set(this, 200);
		return ActionResult.SUCCESS;
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.add(Text.translatable("tooltip.rickmorty.dimension_eraser").formatted(Formatting.DARK_GRAY));
	}
}
