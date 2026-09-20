package dev.benluvzbacon.rickmorty.client.render;

import dev.benluvzbacon.rickmorty.RickMortyMod;
import dev.benluvzbacon.rickmorty.entity.mob.ParasiteEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.math.MathHelper;

/** Tall, twitchy nightmare parasite. Swings with chaotic, occasionally violent joy. */
public class ParasiteEntityModel extends EntityModel<ParasiteEntity> {
	public static final EntityModelLayer LAYER = new EntityModelLayer(RickMortyMod.id("parasite"), "main");

	private final ModelPart root;
	private final ModelPart head;
	private final ModelPart jaw;
	private final ModelPart leftArm;
	private final ModelPart rightArm;
	private final ModelPart leftLeg;
	private final ModelPart rightLeg;

	public ParasiteEntityModel(ModelPart root) {
		this.root = root;
		this.head = root.getChild("head");
		this.jaw = root.getChild("jaw");
		this.leftArm = root.getChild("left_arm");
		this.rightArm = root.getChild("right_arm");
		this.leftLeg = root.getChild("left_leg");
		this.rightLeg = root.getChild("right_leg");
	}

	public static TexturedModelData getTexturedModelData() {
		ModelData data = new ModelData();
		ModelPartData root = data.getRoot();
		root.addChild("head", ModelPartBuilder.create()
				.uv(0, 0).cuboid(-3.5f, -8, -3.5f, 7, 8, 7)
				.uv(28, 0).cuboid(-4.5f, -10, -4.5f, 9, 3, 9, new Dilation(0.2f)) // pincer crown
				, ModelTransform.pivot(0, 1, 0));
		root.addChild("jaw", ModelPartBuilder.create()
				.uv(0, 44).cuboid(-2.5f, 0, -3, 5, 4, 3)
				, ModelTransform.pivot(0, 1, 0));
		root.addChild("body", ModelPartBuilder.create()
				.uv(0, 20).cuboid(-3.5f, 0, -2, 7, 12, 4)
				, ModelTransform.pivot(0, 0, 0));
		root.addChild("left_arm", ModelPartBuilder.create()
				.uv(28, 20).cuboid(-1, 0, -1.5f, 2, 16, 3)
				.uv(28, 40).cuboid(-2, 13, -3, 4, 4, 2) // claw
				, ModelTransform.pivot(-4.5f, 1, 0));
		root.addChild("right_arm", ModelPartBuilder.create()
				.uv(46, 20).cuboid(-1, 0, -1.5f, 2, 16, 3)
				.uv(46, 40).cuboid(-2, 13, -3, 4, 4, 2)
				, ModelTransform.pivot(4.5f, 1, 0));
		root.addChild("left_leg", ModelPartBuilder.create()
				.uv(0, 52).cuboid(-1.5f, 0, -1.5f, 3, 12, 3)
				, ModelTransform.pivot(-2.2f, 12, 0));
		root.addChild("right_leg", ModelPartBuilder.create()
				.uv(16, 52).cuboid(-1.5f, 0, -1.5f, 3, 12, 3)
				, ModelTransform.pivot(2.2f, 12, 0));
		return TexturedModelData.of(data, 64, 64);
	}

	@Override
	public void setAngles(ParasiteEntity entity, float limbAngle, float limbDistance,
						  float animationProgress, float headYaw, float headPitch) {
		head.yaw = headYaw * (MathHelper.PI / 180f) * 0.5f;
		head.pitch = headPitch * (MathHelper.PI / 180f) * 0.4f;
		// perpetual tweak
		jaw.pitch = Math.max(0, MathHelper.sin(animationProgress * 1.3f)) * 0.5f + (entity.isHostile() ? 0.35f : 0);
		rightLeg.pitch = MathHelper.cos(limbAngle * 0.6662f) * 1.5f * limbDistance;
		leftLeg.pitch = MathHelper.cos(limbAngle * 0.6662f + MathHelper.PI) * 1.5f * limbDistance;
		rightArm.pitch = MathHelper.cos(limbAngle * 0.6662f + MathHelper.PI) * 1.4f * limbDistance
				+ MathHelper.sin(animationProgress * 2.1f) * 0.1f;
		leftArm.pitch = MathHelper.cos(limbAngle * 0.6662f) * 1.4f * limbDistance
				+ MathHelper.cos(animationProgress * 1.8f) * 0.1f;
		if (entity.isAttacking()) {
			leftArm.pitch = -2.4f;
			rightArm.pitch = -2.4f;
		}
	}

	@Override
	public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, int color) {
		root.render(matrices, vertices, light, overlay, color);
	}
}
