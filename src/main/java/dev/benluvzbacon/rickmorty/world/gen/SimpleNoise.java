package dev.benluvzbacon.rickmorty.world.gen;

/**
 * Tiny deterministic value-noise implementation (no dependencies on vanilla's
 * noise registries, so terrain generation stays fully self-contained).
 */
public final class SimpleNoise {
	private SimpleNoise() {}

	private static long mix(long a) {
		long h = a * -7046029254386353131L;
		h ^= h >>> 32;
		h *= 69069L + 1L;
		h ^= h >>> 29;
		h *= -4658895280553007687L;
		h ^= h >>> 32;
		return h;
	}

	/** lattice hash in [0,1) */
	public static double hash2(int x, int z, long seed) {
		return (mix(x * 0x8da6b343L ^ z * 0xd8163841L ^ seed) >>> 11) * 0x1.0p-53;
	}

	public static double hash3(int x, int y, int z, long seed) {
		return (mix(x * 0x8da6b343L ^ y * 0xd8163841L ^ z * 0xc2b2ae35L ^ seed) >>> 11) * 0x1.0p-53;
	}

	private static double smooth(double t) {
		return t * t * (3 - 2 * t);
	}

	/** 2D value noise in [0,1) sampled at world coordinates scaled by freq. */
	public static double noise2(double x, double z, double freq, long seed) {
		double nx = x * freq;
		double nz = z * freq;
		int ix = (int) Math.floor(nx);
		int iz = (int) Math.floor(nz);
		double fx = nx - ix;
		double fz = nz - iz;
		double a = hash2(ix, iz, seed);
		double b = hash2(ix + 1, iz, seed);
		double c = hash2(ix, iz + 1, seed);
		double d = hash2(ix + 1, iz + 1, seed);
		double sx = smooth(fx);
		double sz = smooth(fz);
		return (a + (b - a) * sx) + ((c + (d - c) * sx) - (a + (b - a) * sx)) * sz;
	}

	public static double noise3(double x, double y, double z, double freq, long seed) {
		double nx = x * freq;
		double ny = y * freq;
		double nz = z * freq;
		int ix = (int) Math.floor(nx);
		int iy = (int) Math.floor(ny);
		int iz = (int) Math.floor(nz);
		double fx = nx - ix;
		double fy = ny - iy;
		double fz = nz - iz;
		double sx = smooth(fx);
		double sy = smooth(fy);
		double sz = smooth(fz);
		double c000 = hash3(ix, iy, iz, seed);
		double c100 = hash3(ix + 1, iy, iz, seed);
		double c010 = hash3(ix, iy + 1, iz, seed);
		double c110 = hash3(ix + 1, iy + 1, iz, seed);
		double c001 = hash3(ix, iy, iz + 1, seed);
		double c101 = hash3(ix + 1, iy, iz + 1, seed);
		double c011 = hash3(ix, iy + 1, iz + 1, seed);
		double c111 = hash3(ix + 1, iy + 1, iz + 1, seed);
		double x00 = c000 + (c100 - c000) * sx;
		double x10 = c010 + (c110 - c010) * sx;
		double x01 = c001 + (c101 - c001) * sx;
		double x11 = c011 + (c111 - c011) * sx;
		double y0 = x00 + (x10 - x00) * sy;
		double y1 = x01 + (x11 - x01) * sy;
		return y0 + (y1 - y0) * sz;
	}

	/** fractal brownian motion, roughly [0,1) */
	public static double fbm2(double x, double z, double freq, int octaves, long seed) {
		double sum = 0;
		double amp = 0.5;
		double f = freq;
		double norm = 0;
		for (int i = 0; i < octaves; i++) {
			sum += noise2(x, z, f, seed + i * 1013L) * amp;
			norm += amp;
			amp *= 0.5;
			f *= 2.1;
		}
		return sum / norm;
	}

	public static double fbm3(double x, double y, double z, double freq, int octaves, long seed) {
		double sum = 0;
		double amp = 0.5;
		double f = freq;
		double norm = 0;
		for (int i = 0; i < octaves; i++) {
			sum += noise3(x, y, z, f, seed + i * 733L) * amp;
			norm += amp;
			amp *= 0.5;
			f *= 2.1;
		}
		return sum / norm;
	}
}
