package dev.benluvzbacon.rickmorty.client.render;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.math.RotationAxis;

/** Dead-simple held-item feature: renders the main hand stack near the right shoulder/hand area. */
public class HeldItemFeatureRenderer<T extends LivingEntity, M extends EntityModel<T>> extends FeatureRenderer<T, M> {
	public HeldItemFeatureRenderer(LivingEntityRenderer<T, M> context) {
		super(context);
	}

	@Override
	public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light,
					   T entity, float limbAngle, float limbDistance, float tickDelta, float age, float headYaw, float headPitch) {
		ItemStack stack = entity.getMainHandStack();
		if (stack.isEmpty()) return;
		matrices.push();
		// body-space: approach the right hand (right arm at x≈+6px in model space == negative x world after body rotation)
		matrices.translate(0.35, 0.75, 0.1);
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180 - entity.getBodyYaw()));
		matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-20f));
		matrices.scale(1.0f, 1.0f, 1.0f);
		// render using the client's item renderer in third-person hand mode
		net.minecraft.client.MinecraftClient.getInstance().getItemRenderer().renderItem(
				entity, stack, ModelTransformationMode.THIRD_PERSON_RIGHT_HAND, entity.getMainArm() == Arm.LEFT,
				matrices, vertexConsumers, entity.getWorld(), light,
				net.minecraft.client.render.OverlayTexture.DEFAULT_UV, entity.getId());
		matrices.pop();
	}
}
