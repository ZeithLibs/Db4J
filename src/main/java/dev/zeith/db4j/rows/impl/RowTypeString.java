package dev.zeith.db4j.rows.impl;

import dev.zeith.db4j.data.ISQLDataType;
import dev.zeith.db4j.rows.RowType;
import org.jetbrains.annotations.NotNull;

import java.sql.*;

public class RowTypeString
		extends RowType<String>
{
	public RowTypeString(ISQLDataType type)
	{
		super(type, String.class);
	}
	
	@Override
	public void set(@NotNull PreparedStatement statement, int parameterIndex, String value)
			throws SQLException
	{
		statement.setString(parameterIndex, value);
	}
	
	@Override
	public String get(@NotNull ResultSet set, int columnIndex)
			throws SQLException
	{
		return set.getString(columnIndex);
	}
	
	@Override
	public String get(@NotNull ResultSet set, @NotNull String columnLabel)
			throws SQLException
	{
		return set.getString(columnLabel);
	}
}
