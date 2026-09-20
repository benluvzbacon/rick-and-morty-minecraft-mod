package dev.benluvzbacon.rickmorty.client.render;

import dev.benluvzbacon.rickmorty.RickMortyMod;
import dev.benluvzbacon.rickmorty.entity.GazorpianEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.math.MathHelper;

/** Slab-built horned brute. Practically the entire upper body is deltoids. */
public class GazorpianEntityModel extends EntityModel<GazorpianEntity> {
	public static final EntityModelLayer LAYER = new EntityModelLayer(RickMortyMod.id("gazorpian_brute"), "main");

	private final ModelPart root;
	private final ModelPart head;
	private final ModelPart leftArm;
	private final ModelPart rightArm;
	private final ModelPart leftLeg;
	private final ModelPart rightLeg;

	public GazorpianEntityModel(ModelPart root) {
		this.root = root;
		this.head = root.getChild("head");
		this.leftArm = root.getChild("left_arm");
		this.rightArm = root.getChild("right_arm");
		this.leftLeg = root.getChild("left_leg");
		this.rightLeg = root.getChild("right_leg");
	}

	public static TexturedModelData getTexturedModelData() {
		ModelData data = new ModelData();
		ModelPartData root = data.getRoot();
		root.addChild("head", ModelPartBuilder.create()
				.uv(0, 0).cuboid(-3.5f, -7, -3.5f, 7, 7, 7)
				.uv(28, 0).cuboid(-6, -8, -1, 3, 2, 2) // horns
				.uv(28, 6).cuboid(3, -8, -1, 3, 2, 2)
				, ModelTransform.pivot(0, 2, 0));
		root.addChild("body", ModelPartBuilder.create()
				.uv(0, 20).cuboid(-6, 0, -3, 12, 11, 6)
				, ModelTransform.pivot(0, 0, 0));
		root.addChild("left_arm", ModelPartBuilder.create()
				.uv(36, 20).cuboid(-2.5f, 0, -2.5f, 4, 14, 4)
				, ModelTransform.pivot(-8.5f, 1, 0));
		root.addChild("right_arm", ModelPartBuilder.create()
				.uv(52, 20).cuboid(-2.5f, 0, -2.5f, 4, 14, 4)
				, ModelTransform.pivot(8.5f, 1, 0));
		root.addChild("left_leg", ModelPartBuilder.create()
				.uv(0, 44).cuboid(-2, 0, -2, 4, 12, 4)
				, ModelTransform.pivot(-3, 12, 0));
		root.addChild("right_leg", ModelPartBuilder.create()
				.uv(16, 44).cuboid(-2, 0, -2, 4, 12, 4)
				, ModelTransform.pivot(3, 12, 0));
		return TexturedModelData.of(data, 64, 64);
	}

	@Override
	public void setAngles(GazorpianEntity entity, float limbAngle, float limbDistance,
						  float animationProgress, float headYaw, float headPitch) {
		head.yaw = headYaw * (MathHelper.PI / 180f) * 0.6f;
		head.pitch = headPitch * (MathHelper.PI / 180f) * 0.5f;
		rightLeg.pitch = MathHelper.cos(limbAngle * 0.6662f) * 1.2f * limbDistance;
		leftLeg.pitch = MathHelper.cos(limbAngle * 0.6662f + MathHelper.PI) * 1.2f * limbDistance;
		// arms slightly out like a bodybuilder's lats pose
		leftArm.roll = -0.22f;
		rightArm.roll = 0.22f;
		leftArm.pitch = MathHelper.cos(limbAngle * 0.6662f) * 0.9f * limbDistance;
		rightArm.pitch = MathHelper.cos(limbAngle * 0.6662f + MathHelper.PI) * 0.9f * limbDistance;
		if (entity.isAttacking()) {
			leftArm.pitch = -2.6f;
			rightArm.pitch = -2.6f;
		}
	}

	@Override
	public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
		root.render(matrices, vertices, light, overlay, red, green, blue, alpha);
	}
}
