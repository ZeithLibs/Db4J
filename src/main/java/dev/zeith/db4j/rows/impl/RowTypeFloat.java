package dev.zeith.db4j.rows.impl;

import dev.zeith.db4j.data.SQLDataType;
import dev.zeith.db4j.rows.RowType;

import java.sql.*;

public class RowTypeFloat
		extends RowType<Float>
{
	public RowTypeFloat()
	{
		super(SQLDataType.FLOAT, Float.class);
	}
	
	@Override
	public void set(PreparedStatement statement, int paremeterIndex, Float value)
			throws SQLException
	{
		if(value == null)
			statement.setNull(paremeterIndex, type.getSqlType());
		else
			statement.setFloat(paremeterIndex, value);
	}
	
	@Override
	public Float get(ResultSet set, int columnIndex)
			throws SQLException
	{
		return set.getFloat(columnIndex);
	}
	
	@Override
	public Float get(ResultSet set, String columnLabel)
			throws SQLException
	{
		return set.getFloat(columnLabel);
	}
}
