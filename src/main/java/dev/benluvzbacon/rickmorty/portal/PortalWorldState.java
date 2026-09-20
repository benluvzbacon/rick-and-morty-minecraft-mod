package dev.benluvzbacon.rickmorty.portal;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Server-authoritative portal pair registry. All gun-linked portals are stored here so
 * two players / many guns can share the network safely and pairs survive restarts.
 */
public class PortalWorldState extends PersistentState {
	private static final String ID = "rickmorty_portals";

	public static class Link {
		public RegistryKey<World> dimA;
		public BlockPos posA;
		public boolean hasA;
		public RegistryKey<World> dimB;
		public BlockPos posB;
		public boolean hasB;
	}

	private final Map<UUID, Link> pairs = new HashMap<>();

	public static PortalWorldState get(MinecraftServer server) {
		PersistentState.Type<PortalWorldState> type = new PersistentState.Type<>(
				PortalWorldState::new, PortalWorldState::fromNbt, null);
		return server.getOverworld().getPersistentStateManager().getOrCreate(type, ID);
	}

	public Link getOrCreate(UUID pairId) {
		Link link = pairs.get(pairId);
		if (link == null) {
			link = new Link();
			pairs.put(pairId, new Link());
			link = pairs.get(pairId);
		}
		return link;
	}

	@Nullable
	public Link get(UUID pairId) {
		return pairs.get(pairId);
	}

	public void recordA(UUID pairId, RegistryKey<World> dim, BlockPos pos) {
		Link link = getOrCreate(pairId);
		link.dimA = dim;
		link.posA = pos.toImmutable();
		link.hasA = true;
		markDirty();
	}

	public void recordB(UUID pairId, RegistryKey<World> dim, BlockPos pos) {
		Link link = getOrCreate(pairId);
		link.dimB = dim;
		link.posB = pos.toImmutable();
		link.hasB = true;
		markDirty();
	}

	private static PortalWorldState fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
		PortalWorldState state = new PortalWorldState();
		NbtCompound map = nbt.getCompound("pairs");
		for (String key : map.getKeys()) {
			try {
				UUID id = UUID.fromString(key);
				NbtCompound e = map.getCompound(key);
				Link link = new Link();
				if (e.contains("dimA")) {
					link.hasA = true;
					link.dimA = RegistryKey.of(RegistryKeys.WORLD, Identifier.of(e.getString("dimA")));
					link.posA = BlockPos.fromLong(e.getLong("posA"));
				}
				if (e.contains("dimB")) {
					link.hasB = true;
					link.dimB = RegistryKey.of(RegistryKeys.WORLD, Identifier.of(e.getString("dimB")));
					link.posB = BlockPos.fromLong(e.getLong("posB"));
				}
				state.pairs.put(id, link);
			} catch (IllegalArgumentException ignored) {
			}
		}
		return state;
	}

	@Override
	public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
		NbtCompound map = new NbtCompound();
		for (Map.Entry<UUID, Link> entry : pairs.entrySet()) {
			Link l = entry.getValue();
			NbtCompound e = new NbtCompound();
			if (l.hasA && l.dimA != null && l.posA != null) {
				e.putString("dimA", l.dimA.getValue().toString());
				e.putLong("posA", l.posA.asLong());
			}
			if (l.hasB && l.dimB != null && l.posB != null) {
				e.putString("dimB", l.dimB.getValue().toString());
				e.putLong("posB", l.posB.asLong());
			}
			map.put(entry.getKey().toString(), e);
		}
		nbt.put("pairs", map);
		return nbt;
	}
}
