package dev.zeith.db4j.rows.impl;

import dev.zeith.db4j.data.SQLDataType;
import dev.zeith.db4j.rows.RowType;

import java.sql.*;

public class RowTypeDouble
		extends RowType<Double>
{
	public RowTypeDouble()
	{
		super(SQLDataType.REAL, Double.class);
	}
	
	@Override
	public void set(PreparedStatement statement, int paremeterIndex, Double value)
			throws SQLException
	{
		if(value == null)
			statement.setNull(paremeterIndex, type.getSqlType());
		else
			statement.setDouble(paremeterIndex, value);
	}
	
	@Override
	public Double get(ResultSet set, int columnIndex)
			throws SQLException
	{
		return set.getDouble(columnIndex);
	}
	
	@Override
	public Double get(ResultSet set, String columnLabel)
			throws SQLException
	{
		return set.getDouble(columnLabel);
	}
}
