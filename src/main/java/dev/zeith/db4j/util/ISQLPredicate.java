package dev.zeith.db4j.util;

import java.sql.SQLException;

@FunctionalInterface
public interface ISQLPredicate<T>
{
	boolean test(T t) throws SQLException;
	
	static <R> boolean test(R thing, ISQLPredicate<R> predicate) throws SQLException
	{
		return predicate.testAndRetryIfBusy(thing);
	}
	
	default boolean testAndRetryIfBusy(T t) throws SQLException
	{
		while(true)
		{
			try
			{
				return test(t);
			} catch(SQLException e)
			{
				SQLHelper.throwSQLException(e);
			}
		}
	}
}