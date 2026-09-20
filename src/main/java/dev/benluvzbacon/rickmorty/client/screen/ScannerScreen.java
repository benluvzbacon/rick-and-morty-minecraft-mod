package dev.benluvzbacon.rickmorty.client.screen;

import dev.benluvzbacon.rickmorty.RickMortyMod;
import dev.benluvzbacon.rickmorty.registry.ModItems;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/** Interdimensional scanner readout. Opened with V. */
public class ScannerScreen extends Screen {
	private static final Identifier TEXTURE = RickMortyMod.id("textures/gui/scanner_overlay.png");

	private final String scanText;
	private final String[] lines;

	public ScannerScreen(String scanText) {
		super(Text.translatable("gui.rickmorty.scanner.title"));
		this.scanText = scanText;
		this.lines = scanText.split("\n");
	}

	@Override
	protected void init() {
		super.init();
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		renderBackground(context, mouseX, mouseY, delta);
		int panelW = 220;
		int panelH = 140;
		int x = (width - panelW) / 2;
		int y = (height - panelH) / 2;
		context.drawTexture(TEXTURE, x, y, 0, 0, panelW, panelH, 220, 140);
		context.drawItem(new ItemStack(ModItems.INTERDIMENSIONAL_SCANNER), x + 8, y + 8);
		context.drawCenteredTextWithShadow(textRenderer,
				Text.translatable("gui.rickmorty.scanner.title"), width / 2, y + 10, 0x39FF88);
		int ty = y + 30;
		if (lines.length == 0 || (lines.length == 1 && lines[0].isBlank())) {
			context.drawTextWithShadow(textRenderer,
					Text.translatable("gui.rickmorty.scanner.no_data"), x + 12, ty, 0xAAAAAA);
		} else {
			for (String line : lines) {
				if (ty > y + panelH - 10) break;
				context.drawTextWithShadow(textRenderer, Text.literal(trimLine(line)),
						x + 12, ty, colorFor(line));
				ty += 10;
			}
		}
		super.render(context, mouseX, mouseY, delta);
	}

	private static String trimLine(String s) {
		// strip leading "§x" style codes for our own parsing, cap length
		String out = s.replaceAll("§[0-9a-fk-or]", "");
		return out.length() > 44 ? out.substring(0, 44) : out;
	}

	private static int colorFor(String line) {
		if (line.contains("§c") || line.contains("DANGER") || line.contains("HOSTILE")) return 0xFF5555;
		if (line.contains("§e") || line.contains("WARN")) return 0xFFFF55;
		if (line.contains("§a") || line.contains("CLEAR") || line.contains("SAFE")) return 0x55FF55;
		if (line.contains("§b") || line.contains("§d")) return 0x55FFFF;
		return 0xCCCCCC;
	}

	@Override
	public boolean shouldPause() {
		return false;
	}
}
