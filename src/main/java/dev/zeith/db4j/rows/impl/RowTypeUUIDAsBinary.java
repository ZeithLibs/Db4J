package dev.zeith.db4j.rows.impl;

import dev.zeith.db4j.data.SQLDataType;
import dev.zeith.db4j.rows.RowType;
import dev.zeith.db4j.util.SQLHelper;

import java.sql.*;
import java.util.UUID;

public class RowTypeUUIDAsBinary
		extends RowType<UUID>
{
	public RowTypeUUIDAsBinary()
	{
		super(SQLDataType.BINARY.withLength(16), UUID.class);
	}
	
	@Override
	public void set(PreparedStatement statement, int paremeterIndex, UUID value)
			throws SQLException
	{
		if(value == null)
			statement.setNull(paremeterIndex, type.getSqlType());
		else
			statement.setBytes(paremeterIndex, SQLHelper.uuidToBytes(value));
	}
	
	@Override
	public UUID get(ResultSet set, int columnIndex)
			throws SQLException
	{
		return SQLHelper.bytesToUuid(set.getBytes(columnIndex));
	}
	
	@Override
	public UUID get(ResultSet set, String columnLabel)
			throws SQLException
	{
		return SQLHelper.bytesToUuid(set.getBytes(columnLabel));
	}
}
