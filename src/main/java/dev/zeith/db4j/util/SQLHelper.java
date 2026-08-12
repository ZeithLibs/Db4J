package dev.zeith.db4j.util;

import dev.zeith.db4j.data.SQLDataType;
import dev.zeith.db4j.util.sqlite.SQLiteExceptionHelper;

import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.util.*;

public class SQLHelper
{
	public static void throwSQLException(SQLException e)
			throws SQLException
	{
		OptionalInt errorCode = SQLiteExceptionHelper.getErrorCodeOrdinal(e);
		if(errorCode.isPresent())
		{
			switch(errorCode.getAsInt())
			{
				case 6, // SQLITE_BUSY
				     34, // SQLITE_BUSY_RECOVERY
				     35, // SQLITE_BUSY_SNAPSHOT
				     36 // SQLITE_BUSY_TIMEOUT
						->
				{
					try
					{
						Thread.sleep(1000L);
					} catch(InterruptedException e1)
					{
						Thread.currentThread().interrupt();
					}
				}
				default -> throw e;
			}
		}
		
		throw e;
	}
	
	public static byte[] uuidToBytes(UUID uuid)
	{
		ByteBuffer buf = ByteBuffer.allocate(16);
		buf.putLong(uuid.getMostSignificantBits());
		buf.putLong(uuid.getLeastSignificantBits());
		return buf.array();
	}
	
	public static UUID bytesToUuid(byte[] bytes)
			throws SQLException
	{
		if(bytes == null)
			return null;
		
		if(bytes.length != 16)
			throw new SQLException("Invalid UUID length: " + bytes.length);
		
		ByteBuffer buf = ByteBuffer.wrap(bytes);
		
		return new UUID(
				buf.getLong(),
				buf.getLong()
		);
	}
	
	public static String escapeSqlLikePattern(String input)
	{
		if(input == null || input.isBlank())
			return "";
		return input
				.replace("\\", "\\\\")
				.replace("%", "\\%")
				.replace("_", "\\_");
	}
	
	public static long computeDefaultLength(SQLDataType sd)
	{
		return switch(sd)
		{
			case BINARY -> 32L;
			case VARCHAR -> 512L;
			case TINY_TEXT -> 255L;
			case TEXT -> 65535L;
			case MEDIUM_TEXT -> 16777215L;
			case LONG_TEXT, BLOB -> 4294967295L;
			default -> -1L;
		};
	}
}
