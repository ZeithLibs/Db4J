package dev.zeith.db4j;

import java.sql.SQLException;

public interface IDatabaseSource
		extends AutoCloseable
{
	String getPath();
	
	IDatabaseSession getPrimaryConnection()
			throws SQLException;
	
	IDatabaseSession createConnection()
			throws SQLException;
	
	void close()
			throws SQLException;
}