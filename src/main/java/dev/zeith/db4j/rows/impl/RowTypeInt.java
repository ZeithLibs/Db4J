package dev.zeith.db4j.rows.impl;

import dev.zeith.db4j.data.SQLDataType;
import dev.zeith.db4j.rows.RowType;

import java.sql.*;

public class RowTypeInt
		extends RowType<Integer>
{
	public RowTypeInt()
	{
		super(SQLDataType.INT, Integer.class);
	}
	
	@Override
	public void set(PreparedStatement statement, int parameterIndex, Integer value)
			throws SQLException
	{
		if(value == null)
			statement.setNull(parameterIndex, type.getSqlType());
		else
			statement.setInt(parameterIndex, value);
	}
	
	@Override
	public Integer get(ResultSet set, int columnIndex)
			throws SQLException
	{
		return set.getInt(columnIndex);
	}
	
	@Override
	public Integer get(ResultSet set, String columnLabel)
			throws SQLException
	{
		return set.getInt(columnLabel);
	}
}
