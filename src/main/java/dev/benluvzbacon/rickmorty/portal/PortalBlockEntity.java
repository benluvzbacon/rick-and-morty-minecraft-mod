package dev.benluvzbacon.rickmorty.portal;

import dev.benluvzbacon.rickmorty.RickMortyMod;
import dev.benluvzbacon.rickmorty.registry.ModBlockEntities;
import dev.benluvzbacon.rickmorty.registry.ModSounds;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class PortalBlockEntity extends BlockEntity {
	@Nullable
	private UUID pairId;
	@Nullable
	private RegistryKey<World> targetDimension;
	private long expiryTime = -1; // world time at which this portal decays; -1 = never

	public PortalBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.PORTAL_BLOCK_ENTITY, pos, state);
	}

	public void configure(@Nullable UUID pairId, @Nullable RegistryKey<World> targetDimension, int lifetimeTicks) {
		this.pairId = pairId;
		this.targetDimension = targetDimension;
		if (world != null && lifetimeTicks > 0) {
			this.expiryTime = world.getTime() + lifetimeTicks;
		} else {
			this.expiryTime = -1;
		}
		markDirty();
	}

	@Nullable
	public UUID getPairId() {
		return pairId;
	}

	@Nullable
	public RegistryKey<World> getTargetDimension() {
		return targetDimension;
	}

	public static void tick(World world, BlockPos pos, BlockState state, PortalBlockEntity be) {
		if (world.isClient) return;
		if (be.expiryTime > 0 && world.getTime() >= be.expiryTime) {
			world.playSound(null, pos, ModSounds.PORTAL_CLOSE, SoundCategory.BLOCKS, 0.8f, 1.0f);
			world.setBlockState(pos, net.minecraft.block.Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
		}
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
		super.readNbt(nbt, registries);
		if (nbt.containsUuid("pairId")) {
			this.pairId = nbt.getUuid("pairId");
		} else {
			this.pairId = null;
		}
		if (nbt.contains("targetDim")) {
			Identifier id = Identifier.tryParse(nbt.getString("targetDim"));
			this.targetDimension = id == null ? null : RegistryKey.of(RegistryKeys.WORLD, id);
		} else {
			this.targetDimension = null;
		}
		this.expiryTime = nbt.contains("expiry") ? nbt.getLong("expiry") : -1;
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
		super.writeNbt(nbt, registries);
		if (pairId != null) nbt.putUuid("pairId", pairId);
		if (targetDimension != null) nbt.putString("targetDim", targetDimension.getValue().toString());
		nbt.putLong("expiry", expiryTime);
	}
}
