package dev.zeith.db4j.util;

import java.sql.SQLException;

@FunctionalInterface
public interface ISQLFunction<T, R>
{
	R apply(T t) throws SQLException;
	
	static <R, RES> RES apply(R thing, ISQLFunction<R, RES> func) throws SQLException
	{
		return func.applyAndRetryIfBusy(thing);
	}
	
	default R applyAndRetryIfBusy(T t) throws SQLException
	{
		while(true)
		{
			try
			{
				return apply(t);
			} catch(SQLException e)
			{
				SQLHelper.throwSQLException(e);
			}
		}
	}
}