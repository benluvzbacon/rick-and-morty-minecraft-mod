package dev.benluvzbacon.rickmorty.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ModNetworking {
	public static void register() {
		// payload type registrations (both directions, both logical sides)
		PayloadTypeRegistry.playC2S().register(JetpackTogglePayload.ID, JetpackTogglePayload.CODEC);
		PayloadTypeRegistry.playS2C().register(TravelFxPayload.ID, TravelFxPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(ScannerDataPayload.ID, ScannerDataPayload.CODEC);

		ServerPlayNetworking.registerGlobalReceiver(JetpackTogglePayload.ID, (payload, context) -> {
			context.server().execute(() -> {
				ServerPlayerEntity player = context.player();
				JetpackServer.toggle(player);
				boolean active = JetpackServer.isActive(player);
				player.sendMessage(Text.translatable(active
								? "msg.rickmorty.jetpack.on"
								: "msg.rickmorty.jetpack.off").formatted(active ? Formatting.GREEN : Formatting.GRAY),
						true);
			});
		});
	}
}
