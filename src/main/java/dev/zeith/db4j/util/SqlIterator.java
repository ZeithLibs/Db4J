package dev.zeith.db4j.util;

import java.sql.SQLException;
import java.util.Iterator;

public interface SqlIterator<T>
		extends Iterator<T>, AutoCloseable
{
	@Override
	void close()
			throws SQLException;
}