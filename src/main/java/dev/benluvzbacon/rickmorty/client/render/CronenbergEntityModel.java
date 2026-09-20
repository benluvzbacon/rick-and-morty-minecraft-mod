package dev.benluvzbacon.rickmorty.client.render;

import dev.benluvzbacon.rickmorty.RickMortyMod;
import dev.benluvzbacon.rickmorty.entity.CronenbergEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.math.MathHelper;

/** Hunched, asymmetrical body-horror humanoid. There is no skin texture. There is flesh geometry. */
public class CronenbergEntityModel extends EntityModel<CronenbergEntity> {
	public static final EntityModelLayer LAYER = new EntityModelLayer(RickMortyMod.id("cronenberg_mutant"), "main");

	private final ModelPart root;
	private final ModelPart head;
	private final ModelPart leftArm;
	private final ModelPart rightArm;
	private final ModelPart leftLeg;
	private final ModelPart rightLeg;
	private final ModelPart growth;

	public CronenbergEntityModel(ModelPart root) {
		this.root = root;
		this.head = root.getChild("head");
		this.leftArm = root.getChild("left_arm");
		this.rightArm = root.getChild("right_arm");
		this.leftLeg = root.getChild("left_leg");
		this.rightLeg = root.getChild("right_leg");
		this.growth = root.getChild("growth");
	}

	public static TexturedModelData getTexturedModelData() {
		ModelData data = new ModelData();
		ModelPartData root = data.getRoot();
		// hunched forward head
		root.addChild("head", ModelPartBuilder.create()
				.uv(0, 0).cuboid(-4, -7, -8, 8, 6, 7)
				.uv(32, 0).cuboid(-3, -4, -10, 6, 3, 3) // jaw
				.uv(32, 8).cuboid(-2, -9, -7, 4, 2, 4)  // exposed skull ridge
				, ModelTransform.of(0, 4, 2, 0.35f, 0, 0));
		root.addChild("body", ModelPartBuilder.create()
				.uv(0, 20).cuboid(-5, 0, -3, 10, 12, 6)
				, ModelTransform.of(0, 0, 0, 0.25f, 0, 0));
		// asymmetric masses
		root.addChild("growth", ModelPartBuilder.create()
				.uv(0, 44).cuboid(0, 0, 0, 4, 5, 4)
				.uv(16, 44).cuboid(-2, 4, -1, 3, 3, 3)
				, ModelTransform.pivot(-6, 5, -2));
		root.addChild("left_arm", ModelPartBuilder.create()
				.uv(32, 44).cuboid(-1.5f, 0, -1.5f, 3, 14, 3)
				, ModelTransform.pivot(-6.5f, 3, 0));
		root.addChild("right_arm", ModelPartBuilder.create()
				.uv(44, 44).cuboid(-1.5f, 0, -1.5f, 4, 12, 4)
				, ModelTransform.pivot(7, 2, 0));
		root.addChild("left_leg", ModelPartBuilder.create()
				.uv(0, 56).cuboid(-2, 0, -2, 4, 11, 4)
				, ModelTransform.pivot(-3, 13, 0));
		root.addChild("right_leg", ModelPartBuilder.create()
				.uv(16, 56).cuboid(-2, 0, -2, 4, 11, 4)
				, ModelTransform.pivot(3, 13, 0));
		return TexturedModelData.of(data, 64, 64);
	}

	@Override
	public void setAngles(CronenbergEntity entity, float limbAngle, float limbDistance,
						  float animationProgress, float headYaw, float headPitch) {
		head.yaw = headYaw * (MathHelper.PI / 180f) * 0.6f;
		head.pitch = 0.35f + headPitch * (MathHelper.PI / 180f) * 0.5f;
		rightLeg.pitch = MathHelper.cos(limbAngle * 0.6662f) * 1.4f * limbDistance;
		leftLeg.pitch = MathHelper.cos(limbAngle * 0.6662f + MathHelper.PI) * 1.4f * limbDistance;
		// twitchy limbs — nothing about this creature is right
		float twitch = MathHelper.sin(animationProgress * 1.7f) * 0.06f;
		rightArm.pitch = MathHelper.cos(limbAngle * 0.6662f + MathHelper.PI) * 1.3f * limbDistance + twitch;
		leftArm.pitch = MathHelper.cos(limbAngle * 0.6662f) * 1.3f * limbDistance - twitch;
		growth.yaw = MathHelper.sin(animationProgress * 0.3f) * 0.1f;
		if (entity.isAttacking()) rightArm.pitch = -2.2f; // overhead slam
	}

	@Override
	public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, int color) {
		root.render(matrices, vertices, light, overlay, color);
	}
}
