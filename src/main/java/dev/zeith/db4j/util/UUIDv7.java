package dev.zeith.db4j.util;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class UUIDv7
{
	private UUIDv7() {}
	
	public static UUID randomUUID()
	{
		long timestamp = System.currentTimeMillis();
		var rng = ThreadLocalRandom.current();
		long msb = (timestamp << 16) | 0x7000L | (rng.nextLong() & 0x0FFFL);
		long lsb = (rng.nextLong() & 0x3FFF_FFFF_FFFF_FFFFL) | 0x8000_0000_0000_0000L;
		return new UUID(msb, lsb);
	}
}