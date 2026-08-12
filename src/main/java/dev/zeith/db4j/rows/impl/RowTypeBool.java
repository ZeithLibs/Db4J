package dev.zeith.db4j.rows.impl;

import dev.zeith.db4j.data.SQLDataType;
import dev.zeith.db4j.rows.RowType;

import java.sql.*;

public class RowTypeBool
		extends RowType<Boolean>
{
	public RowTypeBool()
	{
		super(SQLDataType.BOOLEAN, Boolean.class);
	}
	
	@Override
	public void set(PreparedStatement statement, int paremeterIndex, Boolean value)
			throws SQLException
	{
		if(value == null)
			statement.setNull(paremeterIndex, type.getSqlType());
		else
			statement.setBoolean(paremeterIndex, value);
	}
	
	@Override
	public Boolean get(ResultSet set, int columnIndex)
			throws SQLException
	{
		return set.getBoolean(columnIndex);
	}
	
	@Override
	public Boolean get(ResultSet set, String columnLabel)
			throws SQLException
	{
		return set.getBoolean(columnLabel);
	}
}
