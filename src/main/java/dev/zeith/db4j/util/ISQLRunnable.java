package dev.zeith.db4j.util;

import java.sql.SQLException;

@FunctionalInterface
public interface ISQLRunnable
{
	void run() throws SQLException;
	
	static void run(ISQLRunnable run) throws SQLException
	{
		run.runAndRetryIfBusy();
	}
	
	default void runAndRetryIfBusy() throws SQLException
	{
		while(true)
		{
			try
			{
				run();
				return;
			} catch(SQLException e)
			{
				SQLHelper.throwSQLException(e);
			}
		}
	}
}