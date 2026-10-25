package dev.zeith.db4j.rows.impl;

import dev.zeith.db4j.data.SQLDataType;
import dev.zeith.db4j.rows.RowType;
import org.jetbrains.annotations.NotNull;

import java.sql.*;

public class RowTypeFloat
		extends RowType<Float>
{
	public RowTypeFloat()
	{
		super(SQLDataType.FLOAT, Float.class);
	}
	
	@Override
	public void set(@NotNull PreparedStatement statement, int parameterIndex, Float value)
			throws SQLException
	{
		if(value == null)
			statement.setNull(parameterIndex, type.getSqlType());
		else
			statement.setFloat(parameterIndex, value);
	}
	
	@Override
	public Float get(@NotNull ResultSet set, int columnIndex)
			throws SQLException
	{
		return set.getFloat(columnIndex);
	}
	
	@Override
	public Float get(@NotNull ResultSet set, @NotNull String columnLabel)
			throws SQLException
	{
		return set.getFloat(columnLabel);
	}
}
