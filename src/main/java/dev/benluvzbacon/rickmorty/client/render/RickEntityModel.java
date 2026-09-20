package dev.benluvzbacon.rickmorty.client.render;

import dev.benluvzbacon.rickmorty.RickMortyMod;
import dev.benluvzbacon.rickmorty.entity.RickEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.math.MathHelper;

/**
 * Rick Sanchez: tall and lanky, spiky blue-grey hair, long lab coat, bad posture.
 * Code-built model (no external assets), fully original.
 */
public class RickEntityModel extends EntityModel<RickEntity> {
	public static final EntityModelLayer LAYER = new EntityModelLayer(RickMortyMod.id("rick"), "main");

	private final ModelPart root;
	private final ModelPart head;
	private final ModelPart body;
	private final ModelPart coat;
	private final ModelPart leftArm;
	private final ModelPart rightArm;
	private final ModelPart leftLeg;
	private final ModelPart rightLeg;

	public RickEntityModel(ModelPart root) {
		this.root = root;
		this.head = root.getChild("head");
		this.body = root.getChild("body");
		this.coat = root.getChild("coat");
		this.leftArm = root.getChild("left_arm");
		this.rightArm = root.getChild("right_arm");
		this.leftLeg = root.getChild("left_leg");
		this.rightLeg = root.getChild("right_leg");
	}

	public static TexturedModelData getTexturedModelData() {
		ModelData modelData = new ModelData();
		ModelPartData root = modelData.getRoot();

		ModelPartData head = root.addChild("head", ModelPartBuilder.create()
				.uv(0, 0).cuboid(-4, -10, -4, 8, 8, 8)
				// spiky hair
				.uv(32, 0).cuboid(-4.5f, -11.5f, -4.5f, 9, 2, 9, Dilation.NONE)
				.uv(32, 11).cuboid(-3, -13, -3, 2, 2, 2)
				.uv(40, 11).cuboid(0, -13.5f, -1, 2, 2, 2)
				.uv(48, 11).cuboid(2, -13, 1, 2, 2, 2)
				// unibrow
				.uv(32, 20).cuboid(-3.5f, -6.5f, -4.2f, 7, 1, 0.5f)
				, ModelTransform.pivot(0, 0, 0));

		root.addChild("body", ModelPartBuilder.create()
				.uv(0, 20).cuboid(-4, 0, -2.5f, 8, 12, 5)
				, ModelTransform.pivot(0, -2, 0));

		root.addChild("coat", ModelPartBuilder.create()
				.uv(26, 20).cuboid(-4.5f, 8, -3, 9, 7, 6)
				, ModelTransform.pivot(0, -2, 0));

		root.addChild("left_arm", ModelPartBuilder.create()
				.uv(0, 40).cuboid(-2, 0, -2, 3, 11, 3)
				, ModelTransform.pivot(-6, -1, 0));

		root.addChild("right_arm", ModelPartBuilder.create()
				.uv(16, 40).cuboid(-1, 0, -2, 3, 11, 3)
				, ModelTransform.pivot(6, -1, 0));

		root.addChild("left_leg", ModelPartBuilder.create()
				.uv(32, 40).cuboid(-2, 0, -2, 3, 12, 3)
				, ModelTransform.pivot(-2.2f, 10, 0));

		root.addChild("right_leg", ModelPartBuilder.create()
				.uv(48, 40).cuboid(-1, 0, -2, 3, 12, 3)
				, ModelTransform.pivot(2.2f, 10, 0));

		return TexturedModelData.of(modelData, 64, 64);
	}

	@Override
	public void setAngles(RickEntity entity, float limbAngle, float limbDistance,
						  float animationProgress, float headYaw, float headPitch) {
		head.yaw = headYaw * (MathHelper.PI / 180f);
		head.pitch = headPitch * (MathHelper.PI / 180f);
		rightLeg.pitch = MathHelper.cos(limbAngle * 0.6662f) * 1.2f * limbDistance;
		leftLeg.pitch = MathHelper.cos(limbAngle * 0.6662f + MathHelper.PI) * 1.2f * limbDistance;
		rightArm.pitch = MathHelper.cos(limbAngle * 0.6662f + MathHelper.PI) * 1.1f * limbDistance;
		leftArm.pitch = MathHelper.cos(limbAngle * 0.6662f) * 1.1f * limbDistance;
		if (entity.isAttacking()) {
			rightArm.pitch = -1.8f + MathHelper.sin(animationProgress * 0.4f) * 0.2f;
		}
		// lab coat sways when running
		coat.pitch = MathHelper.cos(limbAngle * 0.6662f) * 0.15f * limbDistance;
	}

	@Override
	public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, int color) {
		root.render(matrices, vertices, light, overlay, color);
	}
}
