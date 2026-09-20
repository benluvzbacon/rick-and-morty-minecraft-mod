package dev.benluvzbacon.rickmorty.client.render;

import dev.benluvzbacon.rickmorty.RickMortyMod;
import dev.benluvzbacon.rickmorty.entity.boss.AbominationEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.math.MathHelper;

/** The Interdimensional Abomination. Titan-sized, parts from too many animals, none of them friends. */
public class AbominationEntityModel extends EntityModel<AbominationEntity> {
	public static final EntityModelLayer LAYER = new EntityModelLayer(RickMortyMod.id("interdimensional_abomination"), "main");

	private final ModelPart root;
	private final ModelPart head1;
	private final ModelPart head2;
	private final ModelPart head3;
	private final ModelPart leftArm;
	private final ModelPart rightArm;
	private final ModelPart leftLeg;
	private final ModelPart rightLeg;
	private final ModelPart[] spikes = new ModelPart[4];

	public AbominationEntityModel(ModelPart root) {
		this.root = root;
		this.head1 = root.getChild("head1");
		this.head2 = root.getChild("head2");
		this.head3 = root.getChild("head3");
		this.leftArm = root.getChild("left_arm");
		this.rightArm = root.getChild("right_arm");
		this.leftLeg = root.getChild("left_leg");
		this.rightLeg = root.getChild("right_leg");
		for (int i = 0; i < 4; i++) spikes[i] = root.getChild("spike" + i);
	}

	public static TexturedModelData getTexturedModelData() {
		ModelData data = new ModelData();
		ModelPartData root = data.getRoot();

		// main mass
		root.addChild("torso", ModelPartBuilder.create()
				.uv(0, 0).cuboid(-28, -6, -18, 56, 30, 36)
				, ModelTransform.pivot(0, 30, 0));
		root.addChild("hips", ModelPartBuilder.create()
				.uv(0, 70).cuboid(-22, 24, -14, 44, 18, 28)
				, ModelTransform.pivot(0, 30, 0));

		// three heads sprouting at wrong angles
		root.addChild("head1", ModelPartBuilder.create()
				.uv(0, 120).cuboid(-10, -46, -12, 20, 18, 20)
				.uv(64, 120).cuboid(-8, -36, -28, 16, 8, 16) // beak/jaw
				, ModelTransform.pivot(0, 30, 0));
		root.addChild("head2", ModelPartBuilder.create()
				.uv(0, 170).cuboid(-28, -52, -8, 16, 14, 16)
				, ModelTransform.of(0, 30, 0, 0, 0, 0.3f));
		root.addChild("head3", ModelPartBuilder.create()
				.uv(0, 210).cuboid(12, -50, -6, 14, 12, 14)
				, ModelTransform.of(0, 30, 0, 0, 0, -0.35f));

		// massive limbs
		root.addChild("left_arm", ModelPartBuilder.create()
				.uv(64, 180).cuboid(-8, 0, -8, 16, 34, 16)
				.uv(64, 232).cuboid(-10, 30, -10, 20, 10, 20) // hand slab
				, ModelTransform.pivot(-34, 26, 0));
		root.addChild("right_arm", ModelPartBuilder.create()
				.uv(64, 180).cuboid(-8, 0, -8, 16, 34, 16)
				.uv(64, 232).cuboid(-10, 30, -10, 20, 10, 20)
				, ModelTransform.pivot(34, 26, 0));
		root.addChild("left_leg", ModelPartBuilder.create()
				.uv(0, 260).cuboid(-9, 0, -9, 18, 26, 18)
				, ModelTransform.pivot(-16, 72, 0));
		root.addChild("right_leg", ModelPartBuilder.create()
				.uv(80, 260).cuboid(-9, 0, -9, 18, 26, 18)
				, ModelTransform.pivot(16, 72, 0));

		// back spikes
		for (int i = 0; i < 4; i++) {
			root.addChild("spike" + i, ModelPartBuilder.create()
					.uv(200, 0).cuboid(-3, -16, -3, 6, 16, 6)
					, ModelTransform.of(-12 + i * 8, 24, 14, -0.5f, 0, (i - 1.5f) * 0.25f));
		}
		return TexturedModelData.of(data, 256, 320);
	}

	@Override
	public void setAngles(AbominationEntity entity, float limbAngle, float limbDistance,
						  float animationProgress, float headYaw, float headPitch) {
		float t = animationProgress;
		head1.yaw = headYaw * (MathHelper.PI / 180f) * 0.4f;
		head1.pitch = headPitch * (MathHelper.PI / 180f) * 0.4f;
		// side heads wander independently — no head of this thing is in charge
		head2.yaw = MathHelper.sin(t * 0.21f) * 0.5f;
		head3.yaw = MathHelper.cos(t * 0.17f) * 0.5f;
		rightLeg.pitch = MathHelper.cos(limbAngle * 0.5f) * 0.9f * limbDistance;
		leftLeg.pitch = MathHelper.cos(limbAngle * 0.5f + MathHelper.PI) * 0.9f * limbDistance;
		rightArm.pitch = MathHelper.cos(limbAngle * 0.5f + MathHelper.PI) * 0.8f * limbDistance;
		leftArm.pitch = MathHelper.cos(limbAngle * 0.5f) * 0.8f * limbDistance;
		for (int i = 0; i < 4; i++) spikes[i].roll = (i - 1.5f) * 0.25f + MathHelper.sin(t * 0.3f + i) * 0.06f;
		if (entity.isAttacking()) {
			leftArm.pitch = -2.4f;
			rightArm.pitch = -2.4f;
		}
		if (entity.isRoaring()) {
			head1.pitch = -0.5f;
			head2.roll = 0.4f;
			head3.roll = -0.4f;
		}
	}

	@Override
	public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, int color) {
		root.render(matrices, vertices, light, overlay, color);
	}
}
