package dev.zeith.db4j.rows.impl;

import dev.zeith.db4j.data.SQLDataType;
import dev.zeith.db4j.rows.RowType;

import java.sql.*;

public class RowTypeShort
		extends RowType<Short>
{
	public RowTypeShort()
	{
		super(SQLDataType.SHORT, Short.class);
	}
	
	@Override
	public void set(PreparedStatement statement, int parameterIndex, Short value)
			throws SQLException
	{
		if(value == null)
			statement.setNull(parameterIndex, type.getSqlType());
		else
			statement.setShort(parameterIndex, value);
	}
	
	@Override
	public Short get(ResultSet set, int columnIndex)
			throws SQLException
	{
		return set.getShort(columnIndex);
	}
	
	@Override
	public Short get(ResultSet set, String columnLabel)
			throws SQLException
	{
		return set.getShort(columnLabel);
	}
}
