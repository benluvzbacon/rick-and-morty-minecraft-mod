package dev.benluvzbacon.rickmorty.network;

import dev.benluvzbacon.rickmorty.RickMortyMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record JetpackTogglePayload() implements CustomPayload {
	public static final Id<JetpackTogglePayload> ID = new Id<>(RickMortyMod.id("jetpack_toggle"));
	public static final PacketCodec<RegistryByteBuf, JetpackTogglePayload> CODEC =
			PacketCodec.unit(new JetpackTogglePayload());

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
