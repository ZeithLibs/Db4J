package dev.zeith.db4j.util;

import lombok.experimental.UtilityClass;

import java.util.*;

@UtilityClass
public class ULID
{
	private static final char[] ENCODING = "0123456789ABCDEFGHJKMNPQRSTVWXYZ".toCharArray();
	
	private static final byte[] DECODING = new byte[128];
	
	static
	{
		Arrays.fill(DECODING, (byte)-1);
		
		for (byte i = 0; i < ENCODING.length; i++)
		{
			char c = ENCODING[i];
			
			DECODING[c] = i;
			
			if (c >= 'A' && c <= 'Z')
				DECODING[Character.toLowerCase(c)] = i;
		}
		
		DECODING['i'] = DECODING['I'] = DECODING['1'];
		DECODING['l'] = DECODING['L'] = DECODING['1'];
		DECODING['o'] = DECODING['O'] = DECODING['0'];
	}
	
	/**
	 * Convert a UUID to its 26-char ULID representation.
	 */
	public String toString(UUID uuid)
	{
		if (uuid == null)
			throw new NullPointerException("uuid");
		
		long msb = uuid.getMostSignificantBits();
		long lsb = uuid.getLeastSignificantBits();
		
		char[] out = new char[26];
		
		out[0] = ENCODING[(int) ((msb >>> 61) & 0x07)];
		out[1] = ENCODING[(int) ((msb >>> 56) & 0x1F)];
		out[2] = ENCODING[(int) ((msb >>> 51) & 0x1F)];
		out[3] = ENCODING[(int) ((msb >>> 46) & 0x1F)];
		out[4] = ENCODING[(int) ((msb >>> 41) & 0x1F)];
		out[5] = ENCODING[(int) ((msb >>> 36) & 0x1F)];
		out[6] = ENCODING[(int) ((msb >>> 31) & 0x1F)];
		out[7] = ENCODING[(int) ((msb >>> 26) & 0x1F)];
		out[8] = ENCODING[(int) ((msb >>> 21) & 0x1F)];
		out[9] = ENCODING[(int) ((msb >>> 16) & 0x1F)];
		out[10] = ENCODING[(int) ((msb >>> 11) & 0x1F)];
		out[11] = ENCODING[(int) ((msb >>> 6) & 0x1F)];
		out[12] = ENCODING[(int) ((msb >>> 1) & 0x1F)];
		out[13] = ENCODING[(int) (((msb & 1) << 4) | ((lsb >>> 60) & 0x0F))];
		out[14] = ENCODING[(int) ((lsb >>> 55) & 0x1F)];
		out[15] = ENCODING[(int) ((lsb >>> 50) & 0x1F)];
		out[16] = ENCODING[(int) ((lsb >>> 45) & 0x1F)];
		out[17] = ENCODING[(int) ((lsb >>> 40) & 0x1F)];
		out[18] = ENCODING[(int) ((lsb >>> 35) & 0x1F)];
		out[19] = ENCODING[(int) ((lsb >>> 30) & 0x1F)];
		out[20] = ENCODING[(int) ((lsb >>> 25) & 0x1F)];
		out[21] = ENCODING[(int) ((lsb >>> 20) & 0x1F)];
		out[22] = ENCODING[(int) ((lsb >>> 15) & 0x1F)];
		out[23] = ENCODING[(int) ((lsb >>> 10) & 0x1F)];
		out[24] = ENCODING[(int) ((lsb >>> 5) & 0x1F)];
		out[25] = ENCODING[(int) (lsb & 0x1F)];
		
		return new String(out);
	}
	
	/**
	 * Convert a 26-char ULID representation back to a UUID.
	 */
	public UUID parse(String ulid)
	{
		Objects.requireNonNull(ulid, "ulid");
		
		if (ulid.length() != 26)
			throw new IllegalArgumentException("ULID must be exactly 26 characters long");
		
		byte[] values = new byte[26];
		for (int i = 0; i < 26; i++)
		{
			char c = ulid.charAt(i);
			if (c >= DECODING.length || DECODING[c] < 0)
				throw new IllegalArgumentException("Invalid ULID character: " + c);
			values[i] = DECODING[c];
		}
		
		if (values[0] > 7)
			throw new IllegalArgumentException("Invalid ULID: first character must be 0-7");
		
		long msb =
				((long) values[0] << 61) |
				((long) values[1] << 56) |
				((long) values[2] << 51) |
				((long) values[3] << 46) |
				((long) values[4] << 41) |
				((long) values[5] << 36) |
				((long) values[6] << 31) |
				((long) values[7] << 26) |
				((long) values[8] << 21) |
				((long) values[9] << 16) |
				((long) values[10] << 11) |
				((long) values[11] << 6) |
				((long) values[12] << 1) |
				((long) (values[13] >>> 4));
		
		long lsb =
				((long) (values[13] & 0x0F) << 60) |
				((long) values[14] << 55) |
				((long) values[15] << 50) |
				((long) values[16] << 45) |
				((long) values[17] << 40) |
				((long) values[18] << 35) |
				((long) values[19] << 30) |
				((long) values[20] << 25) |
				((long) values[21] << 20) |
				((long) values[22] << 15) |
				((long) values[23] << 10) |
				((long) values[24] << 5) |
				values[25];
		
		return new UUID(msb, lsb);
	}
}