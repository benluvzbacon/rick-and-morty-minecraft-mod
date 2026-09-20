package dev.benluvzbacon.rickmorty.client.render;

import dev.benluvzbacon.rickmorty.RickMortyMod;
import dev.benluvzbacon.rickmorty.entity.mob.AlienCrawlerEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.math.MathHelper;

/** Six-legged alien pack predator. Scuttles. Glows. Bites. */
public class AlienCrawlerEntityModel extends EntityModel<AlienCrawlerEntity> {
	public static final EntityModelLayer LAYER = new EntityModelLayer(RickMortyMod.id("alien_crawler"), "main");

	private final ModelPart root;
	private final ModelPart head;
	private final ModelPart[] legs = new ModelPart[6];

	public AlienCrawlerEntityModel(ModelPart root) {
		this.root = root;
		this.head = root.getChild("head");
		for (int i = 0; i < 6; i++) {
			legs[i] = root.getChild("leg" + i);
		}
	}

	public static TexturedModelData getTexturedModelData() {
		ModelData data = new ModelData();
		ModelPartData root = data.getRoot();
		root.addChild("head", ModelPartBuilder.create()
				.uv(0, 0).cuboid(-2, -2, -6, 4, 4, 5)
				.uv(18, 0).cuboid(-1, -3, -8, 2, 1, 3) // feeler
				, ModelTransform.pivot(0, 6, -4));
		root.addChild("body", ModelPartBuilder.create()
				.uv(0, 12).cuboid(-3, 4, -4, 6, 5, 10)
				.uv(0, 28).cuboid(-2, 5, 6, 4, 4, 5) // abdomen
				, ModelTransform.pivot(0, 0, 0));
		// legs
		for (int i = 0; i < 6; i++) {
			int side = i % 2 == 0 ? -1 : 1;
			int row = i / 2; // 0,1,2 along body
			root.addChild("leg" + i, ModelPartBuilder.create()
					.uv(32, 12).cuboid(side > 0 ? 0 : -1, 0, -1, 1, 6, 1)
					.uv(36, 12).cuboid(side > 0 ? -4 : 3, 0, -1, 4, 1, 1)
					, ModelTransform.pivot(side * 3.2f, 5, -2 + row * 3.5f));
		}
		return TexturedModelData.of(data, 64, 64);
	}

	@Override
	public void setAngles(AlienCrawlerEntity entity, float limbAngle, float limbDistance,
						  float animationProgress, float headYaw, float headPitch) {
		head.yaw = headYaw * (MathHelper.PI / 180f) * 0.7f;
		head.pitch = headPitch * (MathHelper.PI / 180f) * 0.5f;
		for (int i = 0; i < 6; i++) {
			float phase = i % 2 == 0 ? 0 : MathHelper.PI;
			legs[i].roll = MathHelper.sin(limbAngle * 1.5f + phase) * 0.8f * limbDistance;
		}
	}

	@Override
	public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, int color) {
		root.render(matrices, vertices, light, overlay, color);
	}
}
