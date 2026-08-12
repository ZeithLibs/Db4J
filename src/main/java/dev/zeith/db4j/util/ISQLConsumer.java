package dev.zeith.db4j.util;

import java.sql.SQLException;

@FunctionalInterface
public interface ISQLConsumer<T>
{
	void accept(T t)
			throws SQLException;
	
	static <R> void accept(R thing, ISQLConsumer<R> consumer)
			throws SQLException
	{
		consumer.acceptAndRetryIfBusy(thing);
	}
	
	default void acceptAndRetryIfBusy(T t)
			throws SQLException
	{
		while(true)
		{
			try
			{
				accept(t);
				return;
			} catch(SQLException e)
			{
				SQLHelper.throwSQLException(e);
			}
		}
	}
}