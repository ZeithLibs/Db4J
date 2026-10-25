package dev.zeith.db4j.rows.impl;

import dev.zeith.db4j.data.SQLDataType;
import dev.zeith.db4j.rows.RowType;
import org.jetbrains.annotations.NotNull;

import java.sql.*;

public class RowTypeDouble
		extends RowType<Double>
{
	public RowTypeDouble()
	{
		super(SQLDataType.REAL, Double.class);
	}
	
	@Override
	public void set(@NotNull PreparedStatement statement, int parameterIndex, Double value)
			throws SQLException
	{
		if(value == null)
			statement.setNull(parameterIndex, type.getSqlType());
		else
			statement.setDouble(parameterIndex, value);
	}
	
	@Override
	public Double get(@NotNull ResultSet set, int columnIndex)
			throws SQLException
	{
		return set.getDouble(columnIndex);
	}
	
	@Override
	public Double get(@NotNull ResultSet set, @NotNull String columnLabel)
			throws SQLException
	{
		return set.getDouble(columnLabel);
	}
}
