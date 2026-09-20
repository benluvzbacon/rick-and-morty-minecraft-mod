package dev.benluvzbacon.rickmorty.client.render;

import dev.benluvzbacon.rickmorty.RickMortyMod;
import dev.benluvzbacon.rickmorty.entity.MortyEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;

public class MortyEntityRenderer extends MobEntityRenderer<MortyEntity, MortyEntityModel> {
	private static final Identifier TEXTURE = RickMortyMod.id("textures/entity/morty.png");

	public MortyEntityRenderer(EntityRendererFactory.Context context) {
		super(context, new MortyEntityModel(context.getPart(MortyEntityModel.LAYER)), 0.4f);
		this.addFeature(new HeldItemFeatureRenderer(this));
	}

	@Override
	public Identifier getTexture(MortyEntity entity) {
		return TEXTURE;
	}
}
