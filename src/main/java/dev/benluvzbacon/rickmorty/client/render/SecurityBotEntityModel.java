package dev.benluvzbacon.rickmorty.client.render;

import dev.benluvzbacon.rickmorty.RickMortyMod;
import dev.benluvzbacon.rickmorty.entity.SecurityBotEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.math.MathHelper;

/** Citadel patrol drone: floating gun-metal obelisk, slow antenna spin. */
public class SecurityBotEntityModel extends EntityModel<SecurityBotEntity> {
	public static final EntityModelLayer LAYER = new EntityModelLayer(RickMortyMod.id("security_bot"), "main");

	private final ModelPart root;
	private final ModelPart antenna;
	private final ModelPart barrel;

	public SecurityBotEntityModel(ModelPart root) {
		this.root = root;
		this.antenna = root.getChild("antenna");
		this.barrel = root.getChild("barrel");
	}

	public static TexturedModelData getTexturedModelData() {
		ModelData data = new ModelData();
		ModelPartData root = data.getRoot();
		root.addChild("chassis", ModelPartBuilder.create()
				.uv(0, 0).cuboid(-4, 8, -4, 8, 9, 8)
				.uv(32, 0).cuboid(-5, 10, -5, 10, 5, 10) // shoulder skirt
				.uv(0, 18).cuboid(-2, 4, -5, 4, 3, 1)   // optic visor
				, ModelTransform.pivot(0, 0, 0));
		root.addChild("antenna", ModelPartBuilder.create()
				.uv(0, 26).cuboid(-0.5f, 6, -0.5f, 1, 3, 1)
				.uv(6, 26).cuboid(-3, 5.5f, -0.5f, 6, 1, 1)
				, ModelTransform.pivot(0, 0, 0));
		root.addChild("barrel", ModelPartBuilder.create()
				.uv(20, 18).cuboid(-1, 11, -9, 2, 2, 6)
				, ModelTransform.pivot(0, 0, 0));
		return TexturedModelData.of(data, 64, 64);
	}

	@Override
	public void setAngles(SecurityBotEntity entity, float limbAngle, float limbDistance,
						  float animationProgress, float headYaw, float headPitch) {
		antenna.yaw = animationProgress * 0.9f;
		barrel.pitch = MathHelper.cos(animationProgress * 0.3f) * 0.06f;
		root.pivotY = MathHelper.sin(animationProgress * 0.8f) * 1.5f; // hover bob
	}

	@Override
	public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, int color) {
		root.render(matrices, vertices, light, overlay, color);
	}
}
