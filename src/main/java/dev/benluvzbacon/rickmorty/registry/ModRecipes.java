package dev.benluvzbacon.rickmorty.registry;

import dev.benluvzbacon.rickmorty.RickMortyMod;
import dev.benluvzbacon.rickmorty.recipe.WorkbenchRecipe;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ModRecipes {
	public static final RecipeType<WorkbenchRecipe> WORKBENCH_TYPE = new RecipeType<>() {
		@Override
		public String toString() {
			return "rickmorty:workbench";
		}
	};

	public static final RecipeSerializer<WorkbenchRecipe> WORKBENCH_SERIALIZER = new WorkbenchRecipe.Serializer();

	public static void register() {
		Registry.register(Registries.RECIPE_TYPE, RickMortyMod.id("workbench"), WORKBENCH_TYPE);
		Registry.register(Registries.RECIPE_SERIALIZER, RickMortyMod.id("workbench"), WORKBENCH_SERIALIZER);
	}
}
