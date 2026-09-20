package dev.benluvzbacon.rickmorty.client.screen;

import dev.benluvzbacon.rickmorty.RickMortyMod;
import dev.benluvzbacon.rickmorty.screen.RickWorkbenchScreenHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class RickWorkbenchScreen extends HandledScreen<RickWorkbenchScreenHandler> {
	private static final Identifier TEXTURE = RickMortyMod.id("textures/gui/rick_workbench.png");

	public RickWorkbenchScreen(RickWorkbenchScreenHandler handler, PlayerInventory inventory, Text title) {
		super(handler, inventory, title);
		this.backgroundWidth = 176;
		this.backgroundHeight = 166;
		this.playerInventoryTitleY = this.backgroundHeight - 94;
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		renderBackground(context, mouseX, mouseY, delta);
		super.render(context, mouseX, mouseY, delta);
		drawMouseoverTooltip(context, mouseX, mouseY);
	}

	@Override
	protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
		int x = (this.width - this.backgroundWidth) / 2;
		int y = (this.height - this.backgroundHeight) / 2;
		context.drawTexture(TEXTURE, x, y, 0, 0, this.backgroundWidth, this.backgroundHeight);
	}

	@Override
	protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
		super.drawForeground(context, mouseX, mouseY);
		int tier = handler.getTier();
		context.drawText(this.textRenderer, Text.translatable("gui.rickmorty.workbench.tier", tier).formatted(
				tier >= 3 ? 0x55FFFF : (tier == 2 ? 0xFF55FF : 0xAAAAAA)
		), 8, 6, 0xFFFFFF, false);
	}
}
