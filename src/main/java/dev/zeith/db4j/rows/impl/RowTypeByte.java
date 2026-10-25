package dev.zeith.db4j.rows.impl;

import dev.zeith.db4j.data.SQLDataType;
import dev.zeith.db4j.rows.RowType;
import org.jetbrains.annotations.NotNull;

import java.sql.*;

public class RowTypeByte
		extends RowType<Byte>
{
	public RowTypeByte()
	{
		super(SQLDataType.BYTE, Byte.class);
	}
	
	@Override
	public void set(@NotNull PreparedStatement statement, int parameterIndex, Byte value)
			throws SQLException
	{
		if(value == null)
			statement.setNull(parameterIndex, type.getSqlType());
		else
			statement.setByte(parameterIndex, value);
	}
	
	@Override
	public Byte get(@NotNull ResultSet set, int columnIndex)
			throws SQLException
	{
		return set.getByte(columnIndex);
	}
	
	@Override
	public Byte get(@NotNull ResultSet set, @NotNull String columnLabel)
			throws SQLException
	{
		return set.getByte(columnLabel);
	}
}
