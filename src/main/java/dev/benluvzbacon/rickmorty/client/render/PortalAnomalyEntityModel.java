package dev.benluvzbacon.rickmorty.client.render;

import dev.benluvzbacon.rickmorty.RickMortyMod;
import dev.benluvzbacon.rickmorty.entity.PortalAnomalyEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.math.MathHelper;

/** A floating knot of orbiting shards — the visible symptom of damaged spacetime. */
public class PortalAnomalyEntityModel extends EntityModel<PortalAnomalyEntity> {
	public static final EntityModelLayer LAYER = new EntityModelLayer(RickMortyMod.id("portal_anomaly"), "main");

	private final ModelPart root;
	private final ModelPart ring1;
	private final ModelPart ring2;
	private final ModelPart shard1;
	private final ModelPart shard2;
	private final ModelPart shard3;

	public PortalAnomalyEntityModel(ModelPart root) {
		this.root = root;
		this.ring1 = root.getChild("ring1");
		this.ring2 = root.getChild("ring2");
		this.shard1 = root.getChild("shard1");
		this.shard2 = root.getChild("shard2");
		this.shard3 = root.getChild("shard3");
	}

	public static TexturedModelData getTexturedModelData() {
		ModelData data = new ModelData();
		ModelPartData root = data.getRoot();
		root.addChild("core", ModelPartBuilder.create()
				.uv(0, 0).cuboid(-2, -2, -2, 4, 4, 4)
				, ModelTransform.pivot(0, 10, 0));
		root.addChild("ring1", ModelPartBuilder.create()
				.uv(16, 0).cuboid(-5, -0.5f, -5, 10, 1, 2)
				, ModelTransform.pivot(0, 10, 0));
		root.addChild("ring2", ModelPartBuilder.create()
				.uv(16, 4).cuboid(-1, -0.5f, -5, 2, 1, 10)
				, ModelTransform.pivot(0, 10, 0));
		root.addChild("shard1", ModelPartBuilder.create()
				.uv(0, 12).cuboid(4, -0.5f, -0.5f, 2, 1, 1)
				, ModelTransform.pivot(0, 10, 0));
		root.addChild("shard2", ModelPartBuilder.create()
				.uv(0, 12).cuboid(-6, -0.5f, -0.5f, 2, 1, 1)
				, ModelTransform.pivot(0, 10, 0));
		root.addChild("shard3", ModelPartBuilder.create()
				.uv(0, 15).cuboid(-0.5f, 4, -0.5f, 1, 2, 1)
				, ModelTransform.pivot(0, 10, 0));
		return TexturedModelData.of(data, 64, 32);
	}

	@Override
	public void setAngles(PortalAnomalyEntity entity, float limbAngle, float limbDistance,
						  float animationProgress, float headYaw, float headPitch) {
		float t = animationProgress;
		ring1.yaw = t * 1.3f;
		ring2.yaw = -t * 1.1f;
		ring2.roll = t * 0.4f;
		shard1.yaw = t * 2.1f;
		shard2.yaw = t * 2.1f + MathHelper.PI;
		shard3.roll = t * 1.7f;
		root.pivotY = MathHelper.sin(t * 0.9f) * 2f;
	}

	@Override
	public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, int color) {
		root.render(matrices, vertices, light, overlay, color);
	}
}
