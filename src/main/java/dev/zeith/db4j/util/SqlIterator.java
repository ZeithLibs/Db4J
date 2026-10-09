package dev.zeith.db4j.util;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.function.Consumer;

public interface SqlIterator<T>
		extends Iterator<T>, AutoCloseable
{
	@Override
	void close()
			throws SQLException;
	
	@Override
	default void forEachRemaining(Consumer<? super T> action)
	{
		Iterator.super.forEachRemaining(action);
		try
		{
			close();
		} catch(SQLException e)
		{
			throw new UncheckedSQLException(e);
		}
	}
}