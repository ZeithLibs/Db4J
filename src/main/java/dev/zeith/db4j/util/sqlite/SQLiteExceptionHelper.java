package dev.zeith.db4j.util.sqlite;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.OptionalInt;

public class SQLiteExceptionHelper
{
	private static final Class<? extends SQLException> SQLiteException;
	private static final Method getResultCode;
	
	static
	{
		Class<? extends SQLException> ex = null;
		Method mth = null;
		try
		{
			ex = (Class<? extends SQLException>) Thread.currentThread().getContextClassLoader().loadClass("org.sqlite.SQLiteException");
			mth = ex.getMethod("getResultCode");
		} catch(Exception ignored)
		{
		}
		
		SQLiteException = ex;
		getResultCode = mth;
	}
	
	public static OptionalInt getErrorCodeOrdinal(SQLException ex)
	{
		if(SQLiteException == null || !SQLiteException.isInstance(ex)) return OptionalInt.empty();
		try
		{
			Enum<?> e = (Enum<?>) getResultCode.invoke(ex);
			return OptionalInt.of(e.ordinal());
		} catch(Exception e)
		{
			return OptionalInt.empty();
		}
	}
}