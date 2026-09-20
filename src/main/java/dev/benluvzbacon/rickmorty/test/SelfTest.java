package dev.benluvzbacon.rickmorty.test;

import dev.benluvzbacon.rickmorty.RickMortyMod;
import dev.benluvzbacon.rickmorty.blockentity.FluidTankBlockEntity;
import dev.benluvzbacon.rickmorty.blockentity.MeeseeksBoxBlockEntity;
import dev.benluvzbacon.rickmorty.blockentity.RickWorkbenchBlockEntity;
import dev.benluvzbacon.rickmorty.config.ModConfig;
import dev.benluvzbacon.rickmorty.entity.MeeseeksEntity;
import dev.benluvzbacon.rickmorty.entity.MortyEntity;
import dev.benluvzbacon.rickmorty.entity.RickEntity;
import dev.benluvzbacon.rickmorty.entity.boss.AbominationEntity;
import dev.benluvzbacon.rickmorty.event.DimensionalEventManager;
import dev.benluvzbacon.rickmorty.item.PortalGunItem;
import dev.benluvzbacon.rickmorty.portal.PortalBlockEntity;
import dev.benluvzbacon.rickmorty.portal.PortalLogic;
import dev.benluvzbacon.rickmorty.portal.TeleportUtil;
import dev.benluvzbacon.rickmorty.recipe.WorkbenchRecipe;
import dev.benluvzbacon.rickmorty.registry.*;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

/**
 * Automated in-game test harness. Enabled with -Drickmorty.selftest=true (CI wiring):
 * after the server finishes loading, it spawns entities, fires the portal gun, travels
 * between dimensions, crafts in the workbench, runs machines and events, then reports
 * PASS/FAIL lines greppable from the server log and shuts the server down.
 */
public class SelfTest {
	private static boolean enabled;
	private static int phase = -1;
	private static int waitTicks;
	private static int failures;
	private static int passes;
	private static final List<String> log = new ArrayList<>();

	private static boolean running = false;

	public static void register() {
		enabled = System.getProperty("rickmorty.selftest") != null;
		if (!enabled) return;
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			RickMortyMod.LOGGER.info("RICKMORTY-SELFTEST harness armed");
			running = true;
			phase = 0;
			waitTicks = 100;
			failures = 0;
			passes = 0;
			log.clear();
		});
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (!running) return;
			if (--waitTicks > 0) return;
			try {
				runPhase(server);
			} catch (Throwable t) {
				fail("phase " + phase, "threw " + t);
			}
		});
	}

	/** Manual run via /rickmorty selftest (runs the whole battery synchronously). */
	public static void runManual(MinecraftServer server) {
		failures = 0;
		passes = 0;
		log.clear();
		for (phase = 0; phase <= 11; phase++) {
			try {
				runPhase(server);
			} catch (Throwable t) {
				fail("phase " + phase, "threw " + t);
			}
		}
	}

	private static void runPhase(MinecraftServer server) {
		switch (phase) {
			case 0 -> testRegistries(server);
			case 1 -> testDimensionsPresent(server);
			case 2 -> testConfigAndSpawns(server);
			case 3 -> testRickAndMorty(server);
			case 4 -> scheduleWait(60);
			case 5 -> testMortyCombat(server);
			case 6 -> testPortalGunAndTravel(server);
			case 7 -> testCoordinateSafety(server);
			case 8 -> testWorkbench(server);
			case 9 -> testMachines(server);
			case 10 -> testBossAndEvents(server);
			case 11 -> finish(server);
			default -> running = false;
		}
		if (phase != 4) {
			phase++;
			// small breathing room between phases
			if (phase != 4) waitTicks = 30;
		}
	}

	private static void scheduleWait(int ticks) {
		waitTicks = ticks;
		phase++;
	}

	// ------------------------------------------------------------ phases

	private static void testRegistries(MinecraftServer server) {
		check("items registered", Registries.ITEM.stream().filter(i ->
				Registries.ITEM.getId(i).getNamespace().equals("rickmorty")).count() >= 40, "count");
		check("blocks registered", Registries.BLOCK.stream().filter(b ->
				Registries.BLOCK.getId(b).getNamespace().equals("rickmorty")).count() >= 20, "count");
		check("entity types registered", ModEntities.RICK != null && ModEntities.INTERDIMENSIONAL_ABOMINATION != null, "non-null");
		check("sounds registered", ModSounds.PORTAL_OPEN != null && ModSounds.BOSS_ROAR != null, "non-null");
		check("portal gun fluid api", PortalGunItem.addFluid(new ItemStack(ModItems.PORTAL_GUN), 500) == 500, "add 500");
	}

	private static void testDimensionsPresent(MinecraftServer server) {
		for (var key : ModDimensions.TRAVELABLE) {
			ServerWorld world = server.getWorld(key);
			check("dimension loaded: " + key.getValue(), world != null, "non-null");
			if (world != null) {
				var sampled = world.getChunk(0, 0);
				check("chunk 0,0 generated in " + key.getValue(), sampled != null, "chunk");
			}
		}
	}

	private static void testConfigAndSpawns(MinecraftServer server) {
		check("config sane: portal cost", ModConfig.get().portalFluidPerPortal > 0, ">0");
		check("config sane: gun capacity", ModConfig.get().portalGunCapacity >= 1000, ">=1000");
	}

	private static ServerWorld overworld;
	private static ServerPlayerEntity fake;
	private static RickEntity rick;
	private static MortyEntity morty;
	private static net.minecraft.entity.mob.ZombieEntity zombie;

	private static void testRickAndMorty(MinecraftServer server) {
		overworld = server.getOverworld();
		fake = FakePlayer.get(overworld);
		BlockPos base = safeBase(overworld);

		rick = new RickEntity(ModEntities.RICK, overworld);
		rick.refreshPositionAndAngles(base.add(3, 0, 0), 0, 0);
		overworld.spawnEntity(rick);
		check("rick spawned", rick.isAlive(), "alive");
		check("rick attributes", rick.getMaxHealth() >= 40, "hp=" + (int) rick.getMaxHealth());

		morty = new MortyEntity(ModEntities.MORTY, overworld);
		morty.refreshPositionAndAngles(base.add(-3, 0, 0), 0, 0);
		overworld.spawnEntity(morty);
		check("morty spawned", morty.isAlive(), "alive");

		// hand morty a blaster
		morty.equipStack(EquipmentSlot.MAINHAND, new ItemStack(ModItems.PORTAL_BLASTER));
		check("morty armed", morty.hasWeapon(), "blaster equipped");

		// hostile pressure
		zombie = new net.minecraft.entity.mob.ZombieEntity(overworld);
		zombie.refreshPositionAndAngles(base.add(5, 0, 0), 0, 0);
		overworld.spawnEntity(zombie);
		check("zombie spawned", zombie.isAlive(), "alive");
	}

	private static void testMortyCombat(MinecraftServer server) {
		boolean reacted = zombie == null || !zombie.isAlive() || zombie.getHealth() < zombie.getMaxHealth()
				|| (rick != null && rick.getTarget() == zombie) || (morty != null && morty.getTarget() == zombie);
		check("npcs engaged hostile", reacted, "combat reaction observed");
		if (zombie != null && zombie.isAlive()) zombie.discard();
	}

	private static BlockPos portalPos;

	private static void testPortalGunAndTravel(MinecraftServer server) {
		try {
			BlockPos base = safeBase(overworld);
			portalPos = base.up(2).south(2);
			overworld.setBlockState(portalPos.down(), overworld.getBlockState(portalPos.down()));

			ServerWorld alien = server.getWorld(ModDimensions.ALIEN_WORLD);
			check("alien world reachable", alien != null, "non-null");
			if (alien == null) return;

			// fire the actual gun: sneak+use with alien selected -> dimension portal placed
			ItemStack gun = new ItemStack(ModItems.PORTAL_GUN);
			PortalGunItem.addFluid(gun, 500);
			fake.setStackInHand(Hand.MAIN_HAND, gun);
			var nbt = new net.minecraft.nbt.NbtCompound();
			nbt.putInt("dim", 1); // alien
			gun.set(net.minecraft.component.DataComponentTypes.CUSTOM_DATA,
					net.minecraft.component.type.NbtComponent.of(nbt));
			// aim at ground
			fake.setSneaking(true);
			fake.setPos(base.getX() + 0.5, base.getY() + 1.0, base.getZ() + 0.5); // feet ON TOP of the ground block
			fake.setPitch(60f);
			// diagnostics: what does the fake player see?
			var diagHit = fake.raycast(16, 1.0f, false);
			Object diag = diagHit.getType() + "@" + (diagHit instanceof net.minecraft.util.hit.BlockHitResult bh
					? bh.getBlockPos() + " side=" + bh.getSide() + " block=" + overworld.getBlockState(bh.getBlockPos()).getBlock() : "n/a");
			TypedActionResult<ItemStack> result = gun.use(overworld, fake, Hand.MAIN_HAND);
			fake.setSneaking(false);
			check("portal gun use accepted", result.getResult().isAccepted(), "accepted hit=" + diag);

			// a portal block should exist around the fake player
			boolean foundPortal = false;
			for (BlockPos p : BlockPos.iterate(base.add(-6, -3, -6), base.add(6, 5, 6))) {
				if (overworld.getBlockState(p).isOf(ModBlocks.PORTAL_BLOCK)) {
					portalPos = p.toImmutable();
					foundPortal = true;
					break;
				}
			}
			check("portal block placed", foundPortal, "portal near base");

			if (foundPortal && overworld.getBlockEntity(portalPos) instanceof PortalBlockEntity portal) {
				check("portal targets alien dimension",
						portal.getTargetDimension() == ModDimensions.ALIEN_WORLD, "target");
				// teleport the fake player through it
				PortalLogic.tryUsePortal(portal, fake);
				check("player travelled to alien dimension", fake.getWorld() == alien,
						"world now " + fake.getWorld().getRegistryKey().getValue());
				BlockPos feet = fake.getBlockPos();
				check("arrival is breathable", alien.getBlockState(feet).isAir() && alien.getBlockState(feet.up()).isAir(),
						"feet==" + alien.getBlockState(feet));
				check("arrival inside world border", alien.getWorldBorder().contains(feet), "border");
			}

			// travel back home
			boolean back = TeleportUtil.teleportSafe(fake, overworld, base.getX(), base.getY(), base.getZ(), 0);
			check("travel home works", back && fake.getWorld() == overworld, "returned");
		} catch (Throwable t) {
			fail("portal gun/travel", t.toString());
		}
	}

	private static void testCoordinateSafety(MinecraftServer server) {
		try {
			ServerWorld alien = server.getWorld(ModDimensions.ALIEN_WORLD);
			if (alien == null) return;
			// wildly invalid coordinates must be repaired, not crash
			boolean ok = TeleportUtil.teleportSafe(fake, alien, 9.0e15, 9.0e15, 0f);
			BlockPos p = fake.getBlockPos();
			check("extreme coords handled", true, "no crash");
			if (ok) {
				check("clamped inside border", alien.getWorldBorder().contains(p), "within border");
				check("not suffocating", !alien.getBlockState(p).isSolidBlock(alien, p), "breathable");
			}
			// pocket dimension platform safety (void)
			ServerWorld pocket = server.getWorld(ModDimensions.POCKET_WORLD);
			if (pocket != null) {
				TeleportUtil.teleportSafe(fake, pocket, 5000, 5000, 0f);
				BlockPos pp = fake.getBlockPos();
				boolean hasFloor = pocket.getBlockState(pp.down()).isSolidBlock(pocket, pp.down());
				check("pocket arrival has floor/platform", hasFloor || pocket.getBlockState(pp.down()).isOf(Blocks2.SCI_FI_HACK), "floor");
			}
			TeleportUtil.teleportSafe(fake, overworld, 0, overworld.getTopY(net.minecraft.world.Heightmap.Type.MOTION_BLOCKING, 0, 0), 0, 0f);
		} catch (Throwable t) {
			fail("coordinate safety", t.toString());
		}
	}

	private static void testWorkbench(MinecraftServer server) {
		try {
			ServerWorld world = overworld;
			BlockPos pos = safeBase(world).east(8);
			world.setBlockState(pos, ModBlocks.RICK_WORKBENCH.getDefaultState());
			if (!(world.getBlockEntity(pos) instanceof RickWorkbenchBlockEntity bench)) {
				fail("workbench", "no block entity");
				return;
			}
			check("workbench tier 1 baseline", bench.computeTier() == 1, "tier=1");
			var inv = bench.getInventory();
			// t1 recipe: 3 alien crystals in a row -> crystal block? use actual JSON recipe check:
			// We simply verify the recipe type has entries and tier gating works.
			var all = world.getRecipeManager().listAllOfType(ModRecipes.WORKBENCH_TYPE);
			check("workbench recipes loaded from datapack", all.size() >= 5, "count=" + all.size());
			boolean anyTier2Plus = all.stream().anyMatch(r -> r.value().getTier() >= 2);
			check("tiered recipes present", anyTier2Plus, "has t2+");

			// place a quantum computer -> tier should rise
			world.setBlockState(pos.up(1), ModBlocks.QUANTUM_COMPUTER.getDefaultState());
			check("quantum computer boosts tier", bench.computeTier() >= 2, "tier=" + bench.computeTier());
			if (!all.isEmpty()) {
				WorkbenchRecipe any = all.get(0).value();
				check("recipe result available", !any.compiledResult().isEmpty(), "result " + any.compiledResult().getItem());
			}
		} catch (Throwable t) {
			fail("workbench", t.toString());
		}
	}

	private static void testMachines(MinecraftServer server) {
		try {
			ServerWorld world = overworld;
			BlockPos base = safeBase(world).east(-8);
			// tank + reactor fluid loop
			world.setBlockState(base, ModBlocks.PORTAL_FLUID_TANK.getDefaultState());
			if (world.getBlockEntity(base) instanceof FluidTankBlockEntity tank) {
				tank.addFluid(1000);
				ItemStack gun = new ItemStack(ModItems.PORTAL_GUN);
				// fill the gun from the tank:
				int moved = tank.drain(500);
				int added = PortalGunItem.addFluid(gun, moved);
				check("tank -> gun fluid transfer", added == 500 && tank.getFluidAmount() == 500,
						"gun=" + PortalGunItem.getFluid(gun) + " tank=" + tank.getFluidAmount());
			} else {
				fail("tank", "no block entity");
			}

			// meeseeks box
			BlockPos boxPos = base.north(3);
			world.setBlockState(boxPos, ModBlocks.MEESEEKS_BOX.getDefaultState());
			if (world.getBlockEntity(boxPos) instanceof MeeseeksBoxBlockEntity box) {
				boolean summoned = box.summon((ServerWorld) world, boxPos, fake);
				check("meeseeks summoned by box", summoned, "summon");
				var found = world.getEntitiesByClass(MeeseeksEntity.class,
						net.minecraft.util.math.Box.of(boxPos.toCenterPos(), 8, 8, 8), e -> true);
				check("meeseeks entity alive", !found.isEmpty() && found.get(0).getTask() != null, "task set");
				// box must be on cooldown now
				check("box cooldown after use", !box.summon((ServerWorld) world, boxPos, fake), "cooldown");
			} else {
				fail("meeseeks box", "no block entity");
			}
		} catch (Throwable t) {
			fail("machines", t.toString());
		}
	}

	private static void testBossAndEvents(MinecraftServer server) {
		try {
			ServerWorld world = overworld;
			BlockPos base = safeBase(world).north(12);
			AbominationEntity boss = new AbominationEntity(ModEntities.INTERDIMENSIONAL_ABOMINATION, world);
			boss.refreshPositionAndAngles(base, 0, 0);
			world.spawnEntity(boss);
			check("boss spawned", boss.isAlive() && boss.getPhase() == 1, "phase 1");
			boss.setHealth(boss.getMaxHealth() * 0.5f);
			check("boss phase system", boss.getPhase() == 2, "phase 2 at 50%");
			boss.setHealth(boss.getMaxHealth() * 0.2f);
			check("boss phase 3", boss.getPhase() == 3, "phase 3 at 20%");
			boss.discard();

			int placed = DimensionalEventManager.spawnStormAt(world, base, 3);
			check("portal storm spawns rifts", placed > 0, "placed=" + placed);
		} catch (Throwable t) {
			fail("boss/events", t.toString());
		}
	}

	private static void finish(MinecraftServer server) {
		running = false;
		phase++;
		RickMortyMod.LOGGER.info("RICKMORTY-SELFTEST SUMMARY: fail={} pass={}", failures, passes);
		for (String line : log) {
			RickMortyMod.LOGGER.info("RICKMORTY-SELFTEST {}", line);
		}
		if (enabled) {
			RickMortyMod.LOGGER.info("RICKMORTY-SELFTEST harness shutting server down");
			server.stop(false);
		}
	}

	// ------------------------------------------------------------ helpers

	private static BlockPos safeBase(ServerWorld world) {
		BlockPos spawn = world.getSpawnPos();
		int y = world.getTopY(net.minecraft.world.Heightmap.Type.MOTION_BLOCKING,
				spawn.getX(), spawn.getZ());
		return new BlockPos(spawn.getX(), y, spawn.getZ());
	}

	private static void check(String name, boolean ok, String detail) {
		if (ok) {
			passes++;
			log.add("PASS " + name + " (" + detail + ")");
		} else {
			fail(name, detail);
		}
	}

	private static void fail(String name, String detail) {
		failures++;
		log.add("FAIL " + name + " (" + detail + ")");
		RickMortyMod.LOGGER.error("RICKMORTY-SELFTEST FAIL {} ({})", name, detail);
	}

	/** marker interface to avoid importing ModBlocks in one spot twice */
	private interface Blocks2 {
		net.minecraft.block.Block SCI_FI_HACK = ModBlocks.SCI_FI_METAL;
	}
}
