package dev.zeith.db4j.rows.impl;

import dev.zeith.db4j.data.*;
import dev.zeith.db4j.rows.RowType;
import org.jetbrains.annotations.NotNull;

import java.sql.*;

public class RowTypeBytes
		extends RowType<byte[]>
{
	public RowTypeBytes(ISQLDataType type)
	{
		super(type, byte[].class);
	}
	
	@Override
	public void set(@NotNull PreparedStatement statement, int parameterIndex, byte[] value)
			throws SQLException
	{
		if(value == null)
			statement.setNull(parameterIndex, type.getSqlType());
		else
			statement.setBytes(parameterIndex, value);
	}
	
	@Override
	public byte[] get(@NotNull ResultSet set, int columnIndex)
			throws SQLException
	{
		return set.getBytes(columnIndex);
	}
	
	@Override
	public byte[] get(@NotNull ResultSet set, @NotNull String columnLabel)
			throws SQLException
	{
		return set.getBytes(columnLabel);
	}
}
