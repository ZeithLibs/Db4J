package dev.zeith.db4j.rows.impl;

import dev.zeith.db4j.data.SQLDataType;
import dev.zeith.db4j.rows.RowType;

import java.sql.*;
import java.time.Instant;

public class RowTypeInstantSecPrecision
		extends RowType<Instant>
{
	public RowTypeInstantSecPrecision()
	{
		super(SQLDataType.LONG, Instant.class);
	}
	
	@Override
	public void set(PreparedStatement statement, int paremeterIndex, Instant value) throws SQLException
	{
		if(value == null)
		{
			statement.setNull(paremeterIndex, this.type.getSqlType());
		} else
		{
			statement.setLong(paremeterIndex, value.getEpochSecond());
		}
	}
	
	@Override
	public Instant get(ResultSet set, int columnIndex) throws SQLException
	{
		long sec = set.getLong(columnIndex);
		return sec != 0L ? Instant.ofEpochSecond(sec) : null;
	}
	
	@Override
	public Instant get(ResultSet set, String columnLabel) throws SQLException
	{
		long sec = set.getLong(columnLabel);
		return sec != 0L ? Instant.ofEpochSecond(sec) : null;
	}
}