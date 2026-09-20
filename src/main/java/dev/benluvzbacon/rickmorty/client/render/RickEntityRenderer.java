package dev.benluvzbacon.rickmorty.client.render;

import dev.benluvzbacon.rickmorty.RickMortyMod;
import dev.benluvzbacon.rickmorty.entity.RickEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;

public class RickEntityRenderer extends MobEntityRenderer<RickEntity, RickEntityModel> {
	private static final Identifier TEXTURE = RickMortyMod.id("textures/entity/rick.png");

	public RickEntityRenderer(EntityRendererFactory.Context context) {
		super(context, new RickEntityModel(context.getPart(RickEntityModel.LAYER)), 0.5f);
		this.addFeature(new HeldItemFeatureRenderer(this));
	}

	@Override
	public Identifier getTexture(RickEntity entity) {
		return TEXTURE;
	}
}
