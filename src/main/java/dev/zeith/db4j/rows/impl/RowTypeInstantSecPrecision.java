package dev.zeith.db4j.rows.impl;

import dev.zeith.db4j.data.SQLDataType;
import dev.zeith.db4j.rows.RowType;
import org.jetbrains.annotations.NotNull;

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
	public void set(@NotNull PreparedStatement statement, int parameterIndex, Instant value) throws SQLException
	{
		if(value == null)
		{
			statement.setNull(parameterIndex, this.type.getSqlType());
		} else
		{
			statement.setLong(parameterIndex, value.getEpochSecond());
		}
	}
	
	@Override
	public Instant get(@NotNull ResultSet set, int columnIndex) throws SQLException
	{
		long sec = set.getLong(columnIndex);
		return sec != 0L ? Instant.ofEpochSecond(sec) : null;
	}
	
	@Override
	public Instant get(@NotNull ResultSet set, @NotNull String columnLabel) throws SQLException
	{
		long sec = set.getLong(columnLabel);
		return sec != 0L ? Instant.ofEpochSecond(sec) : null;
	}
}