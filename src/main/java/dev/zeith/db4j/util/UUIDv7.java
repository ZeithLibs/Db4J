package dev.zeith.db4j.util;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class UUIDv7
{
	private UUIDv7() {}
	
	public static UUID randomUUID()
	{
		return randomUUID(System.currentTimeMillis());
	}
	
	public static UUID randomUUID(final long timestamp)
	{
		final var rng = ThreadLocalRandom.current();
		final long msb = (timestamp << 16) | 0x7000L | (rng.nextLong() & 0x0FFFL);
		final long lsb = (rng.nextLong() & 0x3FFF_FFFF_FFFF_FFFFL) | 0x8000_0000_0000_0000L;
		return new UUID(msb, lsb);
	}
}