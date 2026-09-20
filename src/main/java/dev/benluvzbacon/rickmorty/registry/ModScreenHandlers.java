package dev.benluvzbacon.rickmorty.registry;

import dev.benluvzbacon.rickmorty.RickMortyMod;
import dev.benluvzbacon.rickmorty.screen.RickWorkbenchScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.math.BlockPos;

public class ModScreenHandlers {
	public static ScreenHandlerType<RickWorkbenchScreenHandler> RICK_WORKBENCH;

	public static void register() {
		RICK_WORKBENCH = Registry.register(Registries.SCREEN_HANDLER, RickMortyMod.id("rick_workbench"),
				new ExtendedScreenHandlerType<>(RickWorkbenchScreenHandler::new, BlockPos.PACKET_CODEC));
	}
}
