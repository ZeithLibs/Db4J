package dev.zeith.db4j.rows.impl;

import dev.zeith.db4j.data.SQLDataType;
import dev.zeith.db4j.rows.RowType;
import org.jetbrains.annotations.NotNull;

import java.sql.*;

public class RowTypeLong
		extends RowType<Long>
{
	public RowTypeLong()
	{
		super(SQLDataType.LONG, Long.class);
	}
	
	@Override
	public void set(@NotNull PreparedStatement statement, int parameterIndex, Long value)
			throws SQLException
	{
		if(value == null)
			statement.setNull(parameterIndex, type.getSqlType());
		else
			statement.setLong(parameterIndex, value);
	}
	
	@Override
	public Long get(@NotNull ResultSet set, int columnIndex)
			throws SQLException
	{
		return set.getLong(columnIndex);
	}
	
	@Override
	public Long get(@NotNull ResultSet set, @NotNull String columnLabel)
			throws SQLException
	{
		return set.getLong(columnLabel);
	}
}
