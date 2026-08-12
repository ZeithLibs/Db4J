package dev.zeith.db4j.util;

import java.sql.SQLException;

@FunctionalInterface
public interface ISQLSupplier<T>
{
	T get() throws SQLException;
	
	static <R> R get(ISQLSupplier<R> supplier) throws SQLException
	{
		return supplier.getAndRetryIfBusy();
	}
	
	default T getAndRetryIfBusy() throws SQLException
	{
		while(true)
		{
			try
			{
				return get();
			} catch(SQLException e)
			{
				SQLHelper.throwSQLException(e);
			}
		}
	}
}