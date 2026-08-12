package dev.zeith.db4j.rows.impl;

import dev.zeith.db4j.data.SQLDataType;
import dev.zeith.db4j.rows.RowType;

import java.sql.*;
import java.time.Instant;

public class RowTypeInstantMsPrecision
		extends RowType<Instant>
{
	public RowTypeInstantMsPrecision()
	{
		super(SQLDataType.LONG, Instant.class);
	}
	
	@Override
	public void set(PreparedStatement statement, int paremeterIndex, Instant value)
			throws SQLException
	{
		if(value == null)
			statement.setNull(paremeterIndex, type.getSqlType());
		else
			statement.setLong(paremeterIndex, value.toEpochMilli());
	}
	
	@Override
	public Instant get(ResultSet set, int columnIndex)
			throws SQLException
	{
		long ms = set.getLong(columnIndex);
		return ms != 0L ? Instant.ofEpochMilli(ms) : null;
	}
	
	@Override
	public Instant get(ResultSet set, String columnLabel)
			throws SQLException
	{
		long ms = set.getLong(columnLabel);
		return ms != 0L ? Instant.ofEpochMilli(ms) : null;
	}
}
