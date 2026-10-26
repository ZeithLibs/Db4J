package dev.zeith.db4j.util;

import org.jetbrains.annotations.Contract;

import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.util.UUID;

public class UUIDHelper
{
	private static final char[] HEX = "0123456789abcdef".toCharArray();
	
	@Contract("null -> null; !null -> !null")
	public static byte[] toBytes16(UUID uuid)
	{
		if(uuid == null) return null;
		
		ByteBuffer buf = ByteBuffer.allocate(16);
		buf.putLong(uuid.getMostSignificantBits());
		buf.putLong(uuid.getLeastSignificantBits());
		return buf.array();
	}
	
	@Contract("null -> null; !null -> !null")
	public static UUID fromBytes16(byte[] bytes)
			throws SQLException
	{
		if(bytes == null) return null;
		
		if(bytes.length != 16)
			throw new SQLException("Invalid UUID length: " + bytes.length);
		
		ByteBuffer buf = ByteBuffer.wrap(bytes);
		
		return new UUID(
				buf.getLong(),
				buf.getLong()
		);
	}
	
	@Contract("null -> null; !null -> !null")
	public static String toString32(UUID uuid)
	{
		if(uuid == null) return null;
		
		char[] chars = new char[32];
		writeLong(chars, 0, uuid.getMostSignificantBits());
		writeLong(chars, 16, uuid.getLeastSignificantBits());
		return new String(chars);
	}
	
	@Contract("null -> null; !null -> !null")
	public static UUID fromString32(String input)
	{
		if(input == null) return null;
		
		if(input.length() != 32)
			throw new IllegalArgumentException("Invalid UUID: " + input);
		
		long most = parseLong(input, 0);
		long least = parseLong(input, 16);
		
		return new UUID(most, least);
	}
	
	private static void writeLong(char[] chars, int offset, long value)
	{
		for(int i = 15; i >= 0; i--)
		{
			chars[offset + i] = HEX[(int) value & 0xF];
			value >>>= 4;
		}
	}
	
	private static long parseLong(String input, int offset)
	{
		long value = 0;
		
		for(int i = offset, end = offset + 16; i < end; i++)
			value = (value << 4) | hexValue(input.charAt(i));
		
		return value;
	}
	
	private static int hexValue(char c)
	{
		if(c >= '0' && c <= '9')
			return c - '0';
		if(c >= 'a' && c <= 'f')
			return c - 'a' + 10;
		if(c >= 'A' && c <= 'F')
			return c - 'A' + 10;
		
		throw new IllegalArgumentException("Invalid UUID hex character: " + c);
	}
}