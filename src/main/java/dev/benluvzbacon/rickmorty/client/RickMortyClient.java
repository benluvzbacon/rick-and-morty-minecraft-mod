package dev.benluvzbacon.rickmorty.client;

import dev.benluvzbacon.rickmorty.client.hud.RickMortyHud;
import dev.benluvzbacon.rickmorty.client.render.*;
import dev.benluvzbacon.rickmorty.client.screen.RickWorkbenchScreen;
import dev.benluvzbacon.rickmorty.client.screen.ScannerScreen;
import dev.benluvzbacon.rickmorty.network.JetpackTogglePayload;
import dev.benluvzbacon.rickmorty.network.ScannerDataPayload;
import dev.benluvzbacon.rickmorty.network.TravelFxPayload;
import dev.benluvzbacon.rickmorty.registry.ModBlocks;
import dev.benluvzbacon.rickmorty.registry.ModEntities;
import dev.benluvzbacon.rickmorty.registry.ModScreenHandlers;
import dev.benluvzbacon.rickmorty.registry.ModSounds;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.InputUtil;
import net.minecraft.sound.SoundCategory;
import org.lwjgl.glfw.GLFW;

public class RickMortyClient implements ClientModInitializer {
	public static KeyBinding scannerKey;
	public static KeyBinding jetpackKey;

	/** latest scanner output, shown by the scanner screen */
	public static String lastScan = "";

	/** travel effect overlay timer (ticks) */
	public static int travelFxTicks;

	@Override
	public void onInitializeClient() {
		// render layers
		BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.PORTAL_BLOCK, RenderLayer.getTranslucent());
		BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.UNSTABLE_PORTAL, RenderLayer.getTranslucent());
		BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.LAB_GLASS, RenderLayer.getTranslucent());
		BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.PORTAL_ENERGY_BLOCK, RenderLayer.getTranslucent());
		BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.CRYSTAL_CLUSTER, RenderLayer.getTranslucent());

		// entity models
		EntityModelLayerRegistry.registerModelLayer(RickEntityModel.LAYER, RickEntityModel::getTexturedModelData);
		EntityModelLayerRegistry.registerModelLayer(MortyEntityModel.LAYER, MortyEntityModel::getTexturedModelData);
		EntityModelLayerRegistry.registerModelLayer(MeeseeksEntityModel.LAYER, MeeseeksEntityModel::getTexturedModelData);
		EntityModelLayerRegistry.registerModelLayer(CronenbergEntityModel.LAYER, CronenbergEntityModel::getTexturedModelData);
		EntityModelLayerRegistry.registerModelLayer(AlienCrawlerEntityModel.LAYER, AlienCrawlerEntityModel::getTexturedModelData);
		EntityModelLayerRegistry.registerModelLayer(ParasiteEntityModel.LAYER, ParasiteEntityModel::getTexturedModelData);
		EntityModelLayerRegistry.registerModelLayer(GazorpianEntityModel.LAYER, GazorpianEntityModel::getTexturedModelData);
		EntityModelLayerRegistry.registerModelLayer(SecurityBotEntityModel.LAYER, SecurityBotEntityModel::getTexturedModelData);
		EntityModelLayerRegistry.registerModelLayer(PortalAnomalyEntityModel.LAYER, PortalAnomalyEntityModel::getTexturedModelData);
		EntityModelLayerRegistry.registerModelLayer(AbominationEntityModel.LAYER, AbominationEntityModel::getTexturedModelData);
		EntityModelLayerRegistry.registerModelLayer(EnergyBoltEntityModel.LAYER, EnergyBoltEntityModel::getTexturedModelData);

		// entity renderers
		EntityRendererRegistry.register(ModEntities.RICK, RickEntityRenderer::new);
		EntityRendererRegistry.register(ModEntities.MORTY, MortyEntityRenderer::new);
		EntityRendererRegistry.register(ModEntities.MEESEEKS, Renderers.MeeseeksEntityRenderer::new);
		EntityRendererRegistry.register(ModEntities.CRONENBERG_MUTANT, Renderers.CronenbergEntityRenderer::new);
		EntityRendererRegistry.register(ModEntities.ALIEN_CRAWLER, Renderers.AlienCrawlerEntityRenderer::new);
		EntityRendererRegistry.register(ModEntities.PARASITE, Renderers.ParasiteEntityRenderer::new);
		EntityRendererRegistry.register(ModEntities.GAZORPIAN_BRUTE, Renderers.GazorpianEntityRenderer::new);
		EntityRendererRegistry.register(ModEntities.SECURITY_BOT, Renderers.SecurityBotEntityRenderer::new);
		EntityRendererRegistry.register(ModEntities.PORTAL_ANOMALY, Renderers.PortalAnomalyEntityRenderer::new);
		EntityRendererRegistry.register(ModEntities.INTERDIMENSIONAL_ABOMINATION, Renderers.AbominationEntityRenderer::new);
		EntityRendererRegistry.register(ModEntities.ENERGY_BOLT, Renderers.EnergyBoltEntityRenderer::new);
		EntityRendererRegistry.register(ModEntities.GRAPNEL, Renderers.GrapnelEntityRenderer::new);

		// screens
		HandledScreens.register(ModScreenHandlers.RICK_WORKBENCH, RickWorkbenchScreen::new);

		// keybinds
		scannerKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.rickmorty.scanner", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_V, "key.categories.rickmorty"));
		jetpackKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.rickmorty.jetpack", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_J, "key.categories.rickmorty"));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (scannerKey.wasPressed()) {
				client.setScreen(new ScannerScreen(lastScan));
			}
			while (jetpackKey.wasPressed()) {
				if (client.player != null) {
					ClientPlayNetworking.send(new JetpackTogglePayload());
					dev.benluvzbacon.rickmorty.client.hud.RickMortyHud.toggleJetpackLocal();
				}
			}
			if (travelFxTicks > 0) travelFxTicks--;
		});

		// hud
		HudRenderCallback.EVENT.register(new RickMortyHud());

		// s2c payloads
		ClientPlayNetworking.registerGlobalReceiver(ScannerDataPayload.ID, (payload, context) -> {
			lastScan = payload.text();
		});
		ClientPlayNetworking.registerGlobalReceiver(TravelFxPayload.ID, (payload, context) -> {
			travelFxTicks = 25;
			if (context.client().player != null) {
				context.client().player.playSound(ModSounds.TELEPORT, 0.8f, 1.0f);
			}
		});
	}
}
