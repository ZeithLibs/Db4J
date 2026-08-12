package dev.zeith.db4j;

import java.sql.*;

public interface IDatabaseSession
		extends AutoCloseable
{
	Connection getConnection()
			throws SQLException;
	
	boolean isClosed()
			throws SQLException;
	
	@Override
	void close()
			throws SQLException;
	
	default PreparedStatement prepareStatement(String sql)
			throws SQLException
	{
		return getConnection().prepareStatement(sql);
	}
	
	static IDatabaseSession of(Connection connection)
	{
		return new IDatabaseSession()
		{
			@Override
			public Connection getConnection()
			{
				return connection;
			}
			
			@Override
			public boolean isClosed()
					throws SQLException
			{
				return connection.isClosed();
			}
			
			@Override
			public void close()
					throws SQLException
			{
				if(connection.isClosed()) return;
				connection.close();
			}
		};
	}
}