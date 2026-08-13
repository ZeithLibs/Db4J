package dev.zeith.db4j.rows.impl;

import dev.zeith.db4j.data.SQLDataType;
import dev.zeith.db4j.rows.RowType;

import java.sql.*;

public class RowTypeByte
		extends RowType<Byte>
{
	public RowTypeByte()
	{
		super(SQLDataType.BYTE, Byte.class);
	}
	
	@Override
	public void set(PreparedStatement statement, int paremeterIndex, Byte value)
			throws SQLException
	{
		if(value == null)
			statement.setNull(paremeterIndex, type.getSqlType());
		else
			statement.setByte(paremeterIndex, value);
	}
	
	@Override
	public Byte get(ResultSet set, int columnIndex)
			throws SQLException
	{
		return set.getByte(columnIndex);
	}
	
	@Override
	public Byte get(ResultSet set, String columnLabel)
			throws SQLException
	{
		return set.getByte(columnLabel);
	}
}
