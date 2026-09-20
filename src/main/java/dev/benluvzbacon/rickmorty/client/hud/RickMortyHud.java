package dev.benluvzbacon.rickmorty.client.hud;

import dev.benluvzbacon.rickmorty.client.RickMortyClient;
import dev.benluvzbacon.rickmorty.registry.ModItems;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.UUID;

/** HUD: portal gun charge readout, jetpack fuel bar, travel flash. */
public class RickMortyHud implements HudRenderCallback {
	/** client-side mirror of jetpack toggle state */
	private static final java.util.Set<UUID> ACTIVE = java.util.concurrent.ConcurrentHashMap.newKeySet();

	public static void toggleJetpackLocal() {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player == null) return;
		UUID id = client.player.getUuid();
		if (!ACTIVE.remove(id)) ACTIVE.add(id);
	}

	@Override
	public void onHudRender(DrawContext context, RenderTickCounter tickCounter) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player == null || client.options.hudHidden) return;

		int w = context.getScaledWindowWidth();
		int h = context.getScaledWindowHeight();

		ItemStack held = client.player.getMainHandStack();
		if (held.isOf(ModItems.PORTAL_GUN)) {
			drawGunReadout(context, client, held, w, h);
		}

		ItemStack chest = client.player.getEquippedStack(EquipmentSlot.CHEST);
		if (chest.isOf(ModItems.JETPACK)
				&& dev.benluvzbacon.rickmorty.item.JetpackItem.getFuel(chest) > 0) {
			drawJetpackBar(context, client, chest, w, h);
		}

		if (RickMortyClient.travelFxTicks > 0) {
			int alpha = Math.min(120, RickMortyClient.travelFxTicks * 8);
			context.fill(0, 0, w, h, (alpha << 24) | 0x39FF88);
		}
	}

	private void drawGunReadout(DrawContext context, MinecraftClient client, ItemStack gun, int w, int h) {
		int charge = dev.benluvzbacon.rickmorty.item.PortalGunItem.getFluid(gun);
		int capacity = 2000; // mirrors config default; a cosmetic bar only
		float frac = Math.min(1f, charge / (float) capacity);
		int chargeDisplay = charge / dev.benluvzbacon.rickmorty.config.ModConfig.get().portalFluidPerPortal;

		int x = w / 2 + 96;
		int y = h - 24;
		context.fill(x, y, x + 64, y + 5, 0xAA000000);
		context.fill(x, y, x + (int) (64 * frac), y + 5, 0xFF39FF88);
		context.drawTextWithShadow(client.textRenderer,
				Text.translatable("hud.rickmorty.gun_charges", chargeDisplay), x, y - 10, 0x39FF88);
	}

	private void drawJetpackBar(DrawContext context, MinecraftClient client, ItemStack pack, int w, int h) {
		int fuel = dev.benluvzbacon.rickmorty.item.JetpackItem.getFuel(pack);
		float frac = Math.min(1f, fuel / 800f);
		int x = w / 2 - 96;
		int y = h - 24;
		boolean active = client.player != null && ACTIVE.contains(client.player.getUuid());
		context.fill(x, y, x + 64, y + 5, 0xAA000000);
		context.fill(x, y, x + (int) (64 * frac), y + 5, active ? 0xFF55FFFF : 0xFF226688);
		context.drawTextWithShadow(client.textRenderer,
				Text.translatable("hud.rickmorty.jetpack_fuel", active ? Text.translatable("hud.rickmorty.on")
						: Text.translatable("hud.rickmorty.off")).formatted(active ? Formatting.AQUA : Formatting.GRAY),
				x, y - 10, active ? 0x55FFFF : 0x888888);
	}
}
