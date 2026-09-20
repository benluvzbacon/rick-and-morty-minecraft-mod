package dev.benluvzbacon.rickmorty.world.feature;

import dev.benluvzbacon.rickmorty.registry.ModBlocks;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

/**
 * Rare alien crystal ore veins in the overworld underground (your first hint that
 * something stranger is going on).
 */
public class OverworldCrystalFeature extends Feature<DefaultFeatureConfig> {
	public OverworldCrystalFeature() {
		super(DefaultFeatureConfig.CODEC);
	}

	@Override
	public boolean generate(FeatureContext<DefaultFeatureConfig> context) {
		StructureWorldAccess world = context.getWorld();
		BlockPos origin = context.getOrigin();
		var random = context.getRandom();
		if (random.nextInt(7) != 0) return false;
		int y = world.getBottomY() + 6 + random.nextInt(70);
		boolean placedAny = false;
		int size = 2 + random.nextInt(4);
		for (int i = 0; i < size; i++) {
			BlockPos p = origin.add(random.nextInt(5) - 2, y - origin.getY() + random.nextInt(4) - 2, random.nextInt(5) - 2);
			var state = world.getBlockState(p);
			if (state.isOf(Blocks.STONE) || state.isOf(Blocks.DEEPSLATE) || state.isOf(Blocks.TUFF)
					|| state.isOf(Blocks.GRANITE) || state.isOf(Blocks.DIORITE) || state.isOf(Blocks.ANDESITE)) {
				world.setBlockState(p, ModBlocks.ALIEN_CRYSTAL_ORE.getDefaultState(), 2);
				placedAny = true;
			}
		}
		return placedAny;
	}
}
