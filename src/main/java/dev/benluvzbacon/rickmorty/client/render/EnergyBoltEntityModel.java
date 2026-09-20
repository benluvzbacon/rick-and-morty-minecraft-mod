package dev.benluvzbacon.rickmorty.client.render;

import dev.benluvzbacon.rickmorty.RickMortyMod;
import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;

/** Tiny emissive cube for energy projectiles and the grapnel hook. Tinted per kind. */
public class EnergyBoltEntityModel extends EntityModel<dev.benluvzbacon.rickmorty.entity.projectile.EnergyBoltEntity> {
	public static final EntityModelLayer LAYER = new EntityModelLayer(RickMortyMod.id("energy_bolt"), "main");

	private final ModelPart root;

	public EnergyBoltEntityModel(ModelPart root) {
		this.root = root;
	}

	public static TexturedModelData getTexturedModelData() {
		ModelData data = new ModelData();
		data.getRoot().addChild("bolt", ModelPartBuilder.create()
				.uv(0, 0).cuboid(-1.5f, -1.5f, -3, 3, 3, 6)
				, ModelTransform.pivot(0, 0, 0));
		return TexturedModelData.of(data, 16, 16);
	}

	@Override
	public void setAngles(dev.benluvzbacon.rickmorty.entity.projectile.EnergyBoltEntity entity,
						  float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
	}

	@Override
	public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
		root.render(matrices, vertices, light, overlay, red, green, blue, alpha);
	}
}
