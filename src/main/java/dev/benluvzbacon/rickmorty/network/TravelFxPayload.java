package dev.benluvzbacon.rickmorty.network;

import dev.benluvzbacon.rickmorty.RickMortyMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

/** Sent to a client when they travel through a portal: plays the swirl effect + sound. */
public record TravelFxPayload() implements CustomPayload {
	public static final Id<TravelFxPayload> ID = new Id<>(RickMortyMod.id("travel_fx"));
	public static final PacketCodec<RegistryByteBuf, TravelFxPayload> CODEC =
			PacketCodec.unit(new TravelFxPayload());

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
