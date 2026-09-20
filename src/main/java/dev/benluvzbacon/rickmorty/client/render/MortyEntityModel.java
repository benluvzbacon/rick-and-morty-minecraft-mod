package dev.benluvzbacon.rickmorty.client.render;

import dev.benluvzbacon.rickmorty.RickMortyMod;
import dev.benluvzbacon.rickmorty.entity.MortyEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.math.MathHelper;

/** Morty Smith: short, anxious, round brown hair. Prone to flinching. */
public class MortyEntityModel extends EntityModel<MortyEntity> {
	public static final EntityModelLayer LAYER = new EntityModelLayer(RickMortyMod.id("morty"), "main");

	private final ModelPart root;
	private final ModelPart head;
	private final ModelPart leftArm;
	private final ModelPart rightArm;
	private final ModelPart leftLeg;
	private final ModelPart rightLeg;

	public MortyEntityModel(ModelPart root) {
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
				.uv(0, 0).cuboid(-4, -8, -4, 8, 8, 8)
				.uv(32, 0).cuboid(-4.5f, -8.5f, -4.5f, 9, 2, 9) // hair cap
				, ModelTransform.pivot(0, 2, 0));
		root.addChild("body", ModelPartBuilder.create()
				.uv(0, 20).cuboid(-4, 0, -2.5f, 8, 10, 5)
				, ModelTransform.pivot(0, 2, 0));
		root.addChild("left_arm", ModelPartBuilder.create()
				.uv(0, 40).cuboid(-2, 0, -2, 3, 9, 3)
				, ModelTransform.pivot(-6, 3, 0));
		root.addChild("right_arm", ModelPartBuilder.create()
				.uv(16, 40).cuboid(-1, 0, -2, 3, 9, 3)
				, ModelTransform.pivot(6, 3, 0));
		root.addChild("left_leg", ModelPartBuilder.create()
				.uv(32, 40).cuboid(-2, 0, -2, 3, 10, 3)
				, ModelTransform.pivot(-2.2f, 12, 0));
		root.addChild("right_leg", ModelPartBuilder.create()
				.uv(48, 40).cuboid(-1, 0, -2, 3, 10, 3)
				, ModelTransform.pivot(2.2f, 12, 0));
		return TexturedModelData.of(data, 64, 64);
	}

	@Override
	public void setAngles(MortyEntity entity, float limbAngle, float limbDistance,
						  float animationProgress, float headYaw, float headPitch) {
		head.yaw = headYaw * (MathHelper.PI / 180f);
		head.pitch = headPitch * (MathHelper.PI / 180f);
		rightLeg.pitch = MathHelper.cos(limbAngle * 0.6662f) * 1.2f * limbDistance;
		leftLeg.pitch = MathHelper.cos(limbAngle * 0.6662f + MathHelper.PI) * 1.2f * limbDistance;
		rightArm.pitch = MathHelper.cos(limbAngle * 0.6662f + MathHelper.PI) * 1.1f * limbDistance;
		leftArm.pitch = MathHelper.cos(limbAngle * 0.6662f) * 1.1f * limbDistance;
		if (entity.isAttacking()) {
			rightArm.pitch = -1.9f; // raise blaster
			leftArm.pitch = -0.6f;  // panic hand
		} else if (entity.getTarget() != null) {
			rightArm.pitch = -1.7f;
		}
	}

	@Override
	public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, int color) {
		root.render(matrices, vertices, light, overlay, color);
	}
}
