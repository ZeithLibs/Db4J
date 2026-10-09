package dev.zeith.db4j.util;

import java.sql.SQLException;

public class UncheckedSQLException
		extends RuntimeException
{
	public UncheckedSQLException(SQLException cause)
	{
		super(cause);
	}
	
	@Override
	public synchronized SQLException getCause()
	{
		return (SQLException) super.getCause();
	}
}