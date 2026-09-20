package dev.benluvzbacon.rickmorty.registry;

import com.mojang.brigadier.arguments.StringArgumentType;
import dev.benluvzbacon.rickmorty.config.ModConfig;
import dev.benluvzbacon.rickmorty.portal.TeleportUtil;
import dev.benluvzbacon.rickmorty.test.SelfTest;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.DimensionArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class ModCommands {
	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(CommandManager.literal("rickmorty")
					.requires(source -> source.hasPermissionLevel(2))
					.then(CommandManager.literal("reload").executes(ctx -> {
						ModConfig.load();
						ctx.getSource().sendFeedback(() -> Text.literal("[RickMorty] config reloaded").formatted(Formatting.GREEN), false);
						return 1;
					}))
					.then(CommandManager.literal("selftest").executes(ctx -> {
						ctx.getSource().sendFeedback(() -> Text.literal("[RickMorty] running manual self test..."), false);
						SelfTest.runManual(ctx.getSource().getServer());
						return 1;
					}))
					.then(CommandManager.literal("travel")
							.then(CommandManager.argument("dimension", StringArgumentType.word())
									.suggests((ctx, builder) -> {
										builder.suggest("minecraft:overworld");
										builder.suggest("rickmorty:alien_planet");
										builder.suggest("rickmorty:citadel");
										builder.suggest("rickmorty:cronenberg_world");
										builder.suggest("rickmorty:pocket_dimension");
										return builder.buildFuture();
									})
									.executes(ctx -> {
										ServerCommandSource source = ctx.getSource();
										ServerPlayerEntity player = source.getPlayer();
										if (player == null) {
											source.sendError(Text.literal("players only"));
											return 0;
										}
										String dimName = StringArgumentType.getString(ctx, "dimension");
										Identifier id = Identifier.tryParse(dimName);
										if (id == null) {
											source.sendError(Text.literal("bad dimension id"));
											return 0;
										}
										ServerWorld dest = source.getServer().getWorld(
												net.minecraft.registry.RegistryKey.of(net.minecraft.registry.RegistryKeys.WORLD, id));
										if (dest == null) {
											source.sendError(Text.literal("dimension not loaded"));
											return 0;
										}
										boolean ok = TeleportUtil.teleportSafe(player, dest,
												player.getX(), player.getZ(), player.getYaw());
										source.sendFeedback(() -> Text.literal("[RickMorty] travel " + (ok ? "ok" : "FAILED"))
												.formatted(ok ? Formatting.GREEN : Formatting.RED), false);
										return ok ? 1 : 0;
									}))));
		});
	}
}
