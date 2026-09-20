package dev.benluvzbacon.rickmorty.network;

import dev.benluvzbacon.rickmorty.RickMortyMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record ScannerDataPayload(String text) implements CustomPayload {
	public static final Id<ScannerDataPayload> ID = new Id<>(RickMortyMod.id("scanner_data"));
	public static final PacketCodec<RegistryByteBuf, ScannerDataPayload> CODEC =
			PacketCodecs.STRING.xmap(ScannerDataPayload::new, ScannerDataPayload::text).cast();

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
