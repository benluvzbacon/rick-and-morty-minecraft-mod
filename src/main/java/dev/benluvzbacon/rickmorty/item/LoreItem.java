package dev.benluvzbacon.rickmorty.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

/** Simple material/flavor item with translatable tooltip lines. */
public class LoreItem extends Item {
	private final int loreLines;

	public LoreItem(Settings settings, int loreLines) {
		super(settings);
		this.loreLines = loreLines;
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		String key = this.toString(); // not reliable; use translation key from stack
		var optional = stack.getRegistryEntry().getKey();
		optional.ifPresent(k -> {
			for (int i = 1; i <= loreLines; i++) {
				tooltip.add(Text.translatable("item." + k.getValue().getNamespace() + "." + k.getValue().getPath() + ".lore." + i)
						.formatted(Formatting.DARK_GRAY));
			}
		});
	}
}
