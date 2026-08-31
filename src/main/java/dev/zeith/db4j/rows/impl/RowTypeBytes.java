package dev.zeith.db4j.rows.impl;

import dev.zeith.db4j.data.*;
import dev.zeith.db4j.rows.RowType;

import java.sql.*;

public class RowTypeBytes
		extends RowType<byte[]>
{
	public RowTypeBytes(ISQLDataType type)
	{
		super(type, byte[].class);
	}
	
	@Override
	public void set(PreparedStatement statement, int parameterIndex, byte[] value)
			throws SQLException
	{
		if(value == null)
			statement.setNull(parameterIndex, type.getSqlType());
		else
			statement.setBytes(parameterIndex, value);
	}
	
	@Override
	public byte[] get(ResultSet set, int columnIndex)
			throws SQLException
	{
		return set.getBytes(columnIndex);
	}
	
	@Override
	public byte[] get(ResultSet set, String columnLabel)
			throws SQLException
	{
		return set.getBytes(columnLabel);
	}
}
