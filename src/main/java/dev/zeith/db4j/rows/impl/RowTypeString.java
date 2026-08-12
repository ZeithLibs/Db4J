package dev.zeith.db4j.rows.impl;

import dev.zeith.db4j.data.ISQLDataType;
import dev.zeith.db4j.rows.RowType;

import java.sql.*;

public class RowTypeString
		extends RowType<String>
{
	public RowTypeString(ISQLDataType type)
	{
		super(type, String.class);
	}
	
	@Override
	public void set(PreparedStatement statement, int paremeterIndex, String value)
			throws SQLException
	{
		statement.setString(paremeterIndex, value);
	}
	
	@Override
	public String get(ResultSet set, int columnIndex)
			throws SQLException
	{
		return set.getString(columnIndex);
	}
	
	@Override
	public String get(ResultSet set, String columnLabel)
			throws SQLException
	{
		return set.getString(columnLabel);
	}
}
