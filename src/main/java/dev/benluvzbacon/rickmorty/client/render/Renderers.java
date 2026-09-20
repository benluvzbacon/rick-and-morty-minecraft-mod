package dev.benluvzbacon.rickmorty.client.render;

import dev.benluvzbacon.rickmorty.RickMortyMod;
import dev.benluvzbacon.rickmorty.entity.MeeseeksEntity;
import dev.benluvzbacon.rickmorty.entity.PortalAnomalyEntity;
import dev.benluvzbacon.rickmorty.entity.SecurityBotEntity;
import dev.benluvzbacon.rickmorty.entity.boss.AbominationEntity;
import dev.benluvzbacon.rickmorty.entity.CronenbergEntity;
import dev.benluvzbacon.rickmorty.entity.AlienCrawlerEntity;
import dev.benluvzbacon.rickmorty.entity.GazorpianEntity;
import dev.benluvzbacon.rickmorty.entity.ParasiteEntity;
import dev.benluvzbacon.rickmorty.entity.projectile.EnergyBoltEntity;
import dev.benluvzbacon.rickmorty.entity.projectile.GrapnelEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

public final class Renderers {
public static class MeeseeksEntityRenderer extends MobEntityRenderer<MeeseeksEntity, MeeseeksEntityModel> {
	private static final Identifier TEXTURE = RickMortyMod.id("textures/entity/meeseeks.png");
	MeeseeksEntityRenderer(EntityRendererFactory.Context context) {
		super(context, new MeeseeksEntityModel(context.getPart(MeeseeksEntityModel.LAYER)), 0.5f);
	}
	@Override public Identifier getTexture(MeeseeksEntity entity) { return TEXTURE; }
}

public static class CronenbergEntityRenderer extends MobEntityRenderer<CronenbergEntity, CronenbergEntityModel> {
	private static final Identifier TEXTURE = RickMortyMod.id("textures/entity/cronenberg.png");
	CronenbergEntityRenderer(EntityRendererFactory.Context context) {
		super(context, new CronenbergEntityModel(context.getPart(CronenbergEntityModel.LAYER)), 0.7f);
	}
	@Override public Identifier getTexture(CronenbergEntity entity) { return TEXTURE; }
}

public static class AlienCrawlerEntityRenderer extends MobEntityRenderer<AlienCrawlerEntity, AlienCrawlerEntityModel> {
	private static final Identifier TEXTURE = RickMortyMod.id("textures/entity/alien_crawler.png");
	AlienCrawlerEntityRenderer(EntityRendererFactory.Context context) {
		super(context, new AlienCrawlerEntityModel(context.getPart(AlienCrawlerEntityModel.LAYER)), 0.4f);
	}
	@Override public Identifier getTexture(AlienCrawlerEntity entity) { return TEXTURE; }
}

public static class ParasiteEntityRenderer extends MobEntityRenderer<ParasiteEntity, ParasiteEntityModel> {
	private static final Identifier TEXTURE = RickMortyMod.id("textures/entity/parasite.png");
	ParasiteEntityRenderer(EntityRendererFactory.Context context) {
		super(context, new ParasiteEntityModel(context.getPart(ParasiteEntityModel.LAYER)), 0.6f);
	}
	@Override public Identifier getTexture(ParasiteEntity entity) { return TEXTURE; }
}

public static class GazorpianEntityRenderer extends MobEntityRenderer<GazorpianEntity, GazorpianEntityModel> {
	private static final Identifier TEXTURE = RickMortyMod.id("textures/entity/gazorpian.png");
	GazorpianEntityRenderer(EntityRendererFactory.Context context) {
		super(context, new GazorpianEntityModel(context.getPart(GazorpianEntityModel.LAYER)), 0.8f);
	}
	@Override public Identifier getTexture(GazorpianEntity entity) { return TEXTURE; }
}

public static class SecurityBotEntityRenderer extends MobEntityRenderer<SecurityBotEntity, SecurityBotEntityModel> {
	private static final Identifier TEXTURE = RickMortyMod.id("textures/entity/security_bot.png");
	SecurityBotEntityRenderer(EntityRendererFactory.Context context) {
		super(context, new SecurityBotEntityModel(context.getPart(SecurityBotEntityModel.LAYER)), 0.5f);
	}
	@Override public Identifier getTexture(SecurityBotEntity entity) { return TEXTURE; }
}

public static class PortalAnomalyEntityRenderer extends MobEntityRenderer<PortalAnomalyEntity, PortalAnomalyEntityModel> {
	private static final Identifier TEXTURE = RickMortyMod.id("textures/entity/portal_anomaly.png");
	PortalAnomalyEntityRenderer(EntityRendererFactory.Context context) {
		super(context, new PortalAnomalyEntityModel(context.getPart(PortalAnomalyEntityModel.LAYER)), 0.5f);
	}
	@Override public Identifier getTexture(PortalAnomalyEntity entity) { return TEXTURE; }
}

public static class AbominationEntityRenderer extends MobEntityRenderer<AbominationEntity, AbominationEntityModel> {
	private static final Identifier TEXTURE = RickMortyMod.id("textures/entity/abomination.png");
	AbominationEntityRenderer(EntityRendererFactory.Context context) {
		super(context, new AbominationEntityModel(context.getPart(AbominationEntityModel.LAYER)), 3.0f);
	}
	@Override public Identifier getTexture(AbominationEntity entity) { return TEXTURE; }
}

public static class EnergyBoltEntityRenderer extends EntityRenderer<EnergyBoltEntity> {
	private static final Identifier TEXTURE = RickMortyMod.id("textures/entity/energy_bolt.png");
	private final EnergyBoltEntityModel model;

	EnergyBoltEntityRenderer(EntityRendererFactory.Context context) {
		super(context);
		this.model = new EnergyBoltEntityModel(context.getPart(EnergyBoltEntityModel.LAYER));
	}

	@Override
	public void render(EnergyBoltEntity entity, float yaw, float tickDelta, MatrixStack matrices,
					   VertexConsumerProvider vertexConsumers, int light) {
		matrices.push();
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f - yaw));
		float renderedPitch = entity.prevPitch + (entity.getPitch() - entity.prevPitch) * tickDelta;
		matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(renderedPitch));
		VertexConsumer vc = vertexConsumers.getBuffer(RenderLayer.getEntityTranslucentEmissive(TEXTURE));
		boolean laser = entity.getKind() == EnergyBoltEntity.Kind.LASER;
		model.render(matrices, vc, 0xF000F0, OverlayTexture.DEFAULT_UV,
				laser ? 1f : 0.22f, laser ? 0.27f : 1f, laser ? 0.27f : 0.53f, 1f);
		matrices.pop();
	}

	@Override
	public Identifier getTexture(EnergyBoltEntity entity) {
		return TEXTURE;
	}
}

public static class GrapnelEntityRenderer extends EntityRenderer<GrapnelEntity> {
	private static final Identifier TEXTURE = RickMortyMod.id("textures/entity/energy_bolt.png");
	private final EnergyBoltEntityModel model;

	GrapnelEntityRenderer(EntityRendererFactory.Context context) {
		super(context);
		this.model = new EnergyBoltEntityModel(context.getPart(EnergyBoltEntityModel.LAYER));
	}

	@Override
	public void render(GrapnelEntity entity, float yaw, float tickDelta, MatrixStack matrices,
					   VertexConsumerProvider vertexConsumers, int light) {
		matrices.push();
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f - yaw));
		VertexConsumer vc = vertexConsumers.getBuffer(RenderLayer.getEntityCutout(TEXTURE));
		model.render(matrices, vc, light, OverlayTexture.DEFAULT_UV, 0.33f, 0.33f, 0.33f, 1f);
		matrices.pop();
	}

	@Override
	public Identifier getTexture(GrapnelEntity entity) {
		return TEXTURE;
	}
}

}
