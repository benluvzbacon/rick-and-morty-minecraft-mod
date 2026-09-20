package dev.benluvzbacon.rickmorty.registry;

import dev.benluvzbacon.rickmorty.RickMortyMod;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class ModSounds {
	public static SoundEvent PORTAL_OPEN;
	public static SoundEvent PORTAL_CLOSE;
	public static SoundEvent PORTAL_SHOOT;
	public static SoundEvent TELEPORT;
	public static SoundEvent BLASTER_SHOT;
	public static SoundEvent LASER_SHOT;
	public static SoundEvent PLASMA_SHOT;
	public static SoundEvent SHRINK_ZAP;
	public static SoundEvent MEESEEKS_SPAWN;
	public static SoundEvent MEESEEKS_POOF;
	public static SoundEvent RICK_AMBIENT;
	public static SoundEvent RICK_TELEPORT;
	public static SoundEvent MORTY_PANIC;
	public static SoundEvent ANOMALY;
	public static SoundEvent BOSS_ROAR;
	public static SoundEvent BOSS_HURT;
	public static SoundEvent REACTOR_HUM;
	public static SoundEvent SCANNER_PING;
	public static SoundEvent GRAPPLE_FIRE;
	public static SoundEvent GRAPPLE_HIT;
	public static SoundEvent PORTAL_STORM;
	public static SoundEvent WORKBENCH_CRAFT;

	private static SoundEvent reg(String name) {
		Identifier id = RickMortyMod.id(name);
		return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
	}

	public static void register() {
		PORTAL_OPEN = reg("portal_open");
		PORTAL_CLOSE = reg("portal_close");
		PORTAL_SHOOT = reg("portal_shoot");
		TELEPORT = reg("teleport");
		BLASTER_SHOT = reg("blaster_shot");
		LASER_SHOT = reg("laser_shot");
		PLASMA_SHOT = reg("plasma_shot");
		SHRINK_ZAP = reg("shrink_zap");
		MEESEEKS_SPAWN = reg("meeseeks_spawn");
		MEESEEKS_POOF = reg("meeseeks_poof");
		RICK_AMBIENT = reg("rick_ambient");
		RICK_TELEPORT = reg("rick_teleport");
		MORTY_PANIC = reg("morty_panic");
		ANOMALY = reg("anomaly");
		BOSS_ROAR = reg("boss_roar");
		BOSS_HURT = reg("boss_hurt");
		REACTOR_HUM = reg("reactor_hum");
		SCANNER_PING = reg("scanner_ping");
		GRAPPLE_FIRE = reg("grapple_fire");
		GRAPPLE_HIT = reg("grapple_hit");
		PORTAL_STORM = reg("portal_storm");
		WORKBENCH_CRAFT = reg("workbench_craft");
	}
}
