package dev.zeith.db4j.rows.impl;

import dev.zeith.db4j.data.SQLDataType;
import dev.zeith.db4j.rows.RowType;
import org.jetbrains.annotations.NotNull;

import java.sql.*;

public class RowTypeBool
		extends RowType<Boolean>
{
	public RowTypeBool()
	{
		super(SQLDataType.BOOLEAN, Boolean.class);
	}
	
	@Override
	public void set(@NotNull PreparedStatement statement, int parameterIndex, Boolean value)
			throws SQLException
	{
		if(value == null)
			statement.setNull(parameterIndex, type.getSqlType());
		else
			statement.setBoolean(parameterIndex, value);
	}
	
	@Override
	public Boolean get(@NotNull ResultSet set, int columnIndex)
			throws SQLException
	{
		return set.getBoolean(columnIndex);
	}
	
	@Override
	public Boolean get(@NotNull ResultSet set, @NotNull String columnLabel)
			throws SQLException
	{
		return set.getBoolean(columnLabel);
	}
}
