package dev.benluvzbacon.rickmorty.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.benluvzbacon.rickmorty.registry.ModRecipes;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Rick's Workbench recipe: a shaped 3x3 recipe with a technology tier requirement.
 * The crafting grid must match the pattern (mirroring allowed) and the bench must be
 * sufficiently upgraded with adjacent machines.
 */
public class WorkbenchRecipe implements Recipe<Inventory> {
	private final int tier;
	private final List<String> pattern;
	private final Map<String, Ingredient> key;
	private final ItemStack result;

	private final List<Ingredient> grid; // resolved 3x3 cells (width first)

	public WorkbenchRecipe(int tier, List<String> pattern, Map<String, Ingredient> key, ItemStack result) {
		this.tier = tier;
		this.pattern = pattern;
		this.key = key;
		this.result = result;
		this.grid = resolve(pattern, key);
	}

	private static List<Ingredient> resolve(List<String> pattern, Map<String, Ingredient> key) {
		List<Ingredient> out = new ArrayList<>(9);
		for (int row = 0; row < 3; row++) {
			String line = row < pattern.size() ? pattern.get(row) : "   ";
			line = (line + "   ").substring(0, 3);
			for (int col = 0; col < 3; col++) {
				String symbol = String.valueOf(line.charAt(col));
				if (symbol.equals(" ")) {
					out.add(Ingredient.EMPTY);
				} else {
					out.add(key.getOrDefault(symbol, Ingredient.EMPTY));
				}
			}
		}
		return out;
	}

	public int getTier() {
		return tier;
	}

	public ItemStack getResult() {
		return result;
	}

	@Override
	public boolean matches(Inventory inventory, World world) {
		return matchesInternal(inventory, false) || matchesInternal(inventory, true);
	}

	private boolean matchesInternal(Inventory inventory, boolean mirror) {
		for (int i = 0; i < 9; i++) {
			int gridIndex = mirror ? (i / 3) * 3 + (2 - i % 3) : i;
			Ingredient want = grid.get(gridIndex);
			ItemStack have = inventory.getStack(i);
			if (want.isEmpty()) {
				if (!have.isEmpty()) return false;
			} else if (!want.test(have)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public ItemStack craft(Inventory inventory, RegistryWrapper.WrapperLookup registries) {
		return result.copy();
	}

	@Override
	public boolean fits(int width, int height) {
		return width >= 3 && height >= 3;
	}

	@Override
	public ItemStack getResult(RegistryWrapper.WrapperLookup registries) {
		return result;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return ModRecipes.WORKBENCH_SERIALIZER;
	}

	@Override
	public RecipeType<?> getType() {
		return ModRecipes.WORKBENCH_TYPE;
	}

	public static class Serializer implements RecipeSerializer<WorkbenchRecipe> {
		private static final MapCodec<WorkbenchRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.INT.optionalFieldOf("tier", 1).forGetter(WorkbenchRecipe::getTier),
				Codec.STRING.listOf().fieldOf("pattern").forGetter(r -> r.pattern),
				Codec.unboundedMap(Codec.STRING, Ingredient.CODEC).fieldOf("key").forGetter(r -> r.key),
				ItemStack.CODEC.fieldOf("result").forGetter(WorkbenchRecipe::getResult)
		).apply(instance, WorkbenchRecipe::new));

		private static final PacketCodec<RegistryByteBuf, WorkbenchRecipe> PACKET_CODEC = PacketCodec.tuple(
				PacketCodecs.VAR_INT, r -> r.tier,
				PacketCodecs.collection(java.util.ArrayList::new, PacketCodecs.STRING), r -> r.pattern,
				PacketCodecs.map(java.util.HashMap::new, PacketCodecs.STRING, Ingredient.PACKET_CODEC), r -> r.key,
				ItemStack.PACKET_CODEC, r -> r.result,
				WorkbenchRecipe::new);

		@Override
		public MapCodec<WorkbenchRecipe> codec() {
			return CODEC;
		}

		@Override
		public PacketCodec<RegistryByteBuf, WorkbenchRecipe> packetCodec() {
			return PACKET_CODEC;
		}
	}
}
