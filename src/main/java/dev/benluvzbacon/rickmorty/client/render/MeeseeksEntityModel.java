package dev.benluvzbacon.rickmorty.client.render;

import dev.benluvzbacon.rickmorty.RickMortyMod;
import dev.benluvzbacon.rickmorty.entity.MeeseeksEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.math.MathHelper;

/** Mr. Meeseeks. Existence is pain, and the shader knows it. */
public class MeeseeksEntityModel extends EntityModel<MeeseeksEntity> {
	public static final EntityModelLayer LAYER = new EntityModelLayer(RickMortyMod.id("meeseeks"), "main");

	private final ModelPart root;
	private final ModelPart head;
	private final ModelPart leftArm;
	private final ModelPart rightArm;
	private final ModelPart leftLeg;
	private final ModelPart rightLeg;
	private int desperation;

	public MeeseeksEntityModel(ModelPart root) {
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
				.uv(0, 0).cuboid(-4, -9, -4, 8, 9, 8)
				.uv(32, 0).cuboid(-4, -12, -4, 8, 2, 8) // head ridge
				// wide grin
				.uv(0, 20).cuboid(-3.5f, -4.5f, -4.2f, 7, 1.2f, 0.4f)
				, ModelTransform.pivot(0, 0, 0));
		root.addChild("body", ModelPartBuilder.create()
				.uv(0, 26).cuboid(-4, 0, -2.5f, 8, 11, 5)
				, ModelTransform.pivot(0, -1, 0));
		root.addChild("left_arm", ModelPartBuilder.create()
				.uv(0, 46).cuboid(-1.5f, 0, -1.5f, 3, 10, 3)
				, ModelTransform.pivot(-5.8f, 1, 0));
		root.addChild("right_arm", ModelPartBuilder.create()
				.uv(16, 46).cuboid(-1.5f, 0, -1.5f, 3, 10, 3)
				, ModelTransform.pivot(5.8f, 1, 0));
		root.addChild("left_leg", ModelPartBuilder.create()
				.uv(32, 46).cuboid(-2, 0, -2, 3, 11, 3)
				, ModelTransform.pivot(-2.2f, 10, 0));
		root.addChild("right_leg", ModelPartBuilder.create()
				.uv(48, 46).cuboid(-1, 0, -2, 3, 11, 3)
				, ModelTransform.pivot(2.2f, 10, 0));
		return TexturedModelData.of(data, 64, 64);
	}

	@Override
	public void setAngles(MeeseeksEntity entity, float limbAngle, float limbDistance,
						  float animationProgress, float headYaw, float headPitch) {
		this.desperation = entity.getDesperation();
		head.yaw = headYaw * (MathHelper.PI / 180f);
		head.pitch = headPitch * (MathHelper.PI / 180f);
		rightLeg.pitch = MathHelper.cos(limbAngle * 0.6662f) * 1.3f * limbDistance;
		leftLeg.pitch = MathHelper.cos(limbAngle * 0.6662f + MathHelper.PI) * 1.3f * limbDistance;
		rightArm.pitch = MathHelper.cos(limbAngle * 0.6662f + MathHelper.PI) * 1.2f * limbDistance;
		leftArm.pitch = MathHelper.cos(limbAngle * 0.6662f) * 1.2f * limbDistance;
		// desperation: flail
		int d = entity.getDesperation();
		if (d >= 2) {
			leftArm.pitch = -2.6f + MathHelper.sin(animationProgress * (0.6f + d * 0.4f)) * 0.7f;
			rightArm.pitch = -2.6f + MathHelper.cos(animationProgress * (0.6f + d * 0.4f)) * 0.7f;
		}
		if (entity.isAttacking()) rightArm.pitch = -1.8f;
	}

	@Override
	public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay,
					   float red, float green, float blue, float alpha) {
		int d = this.desperation;
		if (d < 0) d = 0;
		if (d > 2) d = 2;
		// fresh blue -> agitated red
		float r = 1f;
		float g = Math.max(0.3f, 1f - d * 0.25f);
		float b = Math.max(0.3f, 1f - d * 0.2f);
		root.render(matrices, vertices, light, overlay, r, g, b, alpha);
	}
}
