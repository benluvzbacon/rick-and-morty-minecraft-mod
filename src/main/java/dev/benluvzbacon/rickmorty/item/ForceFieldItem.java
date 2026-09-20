package dev.benluvzbacon.rickmorty.item;

import dev.benluvzbacon.rickmorty.registry.ModSounds;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.joml.Vector3f;

import java.util.List;

/**
 * Force-Field Generator: 5 seconds of hardened skin and a shockwave that shoves
 * everything hostile away. A Rick classic for "get off me" moments.
 */
public class ForceFieldItem extends Item {
	public ForceFieldItem(Settings settings) {
		super(settings.maxCount(1).maxDamage(16));
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);
		if (user.getItemCooldownManager().isCoolingDown(this)) return TypedActionResult.fail(stack);
		if (world instanceof ServerWorld sw) {
			user.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 100, 3, true, false, true));
			user.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 100, 1, true, false, true));
			var hostiles = sw.getEntitiesByClass(HostileEntity.class, user.getBoundingBox().expand(6), LivingEntity::isAlive);
			for (HostileEntity h : hostiles) {
				double dx = h.getX() - user.getX();
				double dz = h.getZ() - user.getZ();
				double len = Math.max(0.1, Math.sqrt(dx * dx + dz * dz));
				h.addVelocity(dx / len * 1.8, 0.45, dz / len * 1.8);
			}
			sw.spawnParticles(new DustParticleEffect(new Vector3f(0.3f, 1.0f, 0.6f), 1.6f),
					user.getX(), user.getY() + 1, user.getZ(), 80, 2.0, 1.4, 2.0, 0.01);
			sw.playSound(null, user.getBlockPos(), ModSounds.PORTAL_OPEN, SoundCategory.PLAYERS, 1.0f, 1.8f);
			if (!user.getAbilities().creativeMode) {
				stack.damage(1, user, net.minecraft.entity.EquipmentSlot.MAINHAND);
			}
			user.getItemCooldownManager().set(this, 900); // 45 s
		}
		return TypedActionResult.success(stack);
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.add(Text.translatable("tooltip.rickmorty.force_field").formatted(Formatting.DARK_GRAY));
	}
}
