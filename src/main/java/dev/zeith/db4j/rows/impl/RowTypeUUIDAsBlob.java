package dev.zeith.db4j.rows.impl;

import dev.zeith.db4j.data.SQLDataType;
import dev.zeith.db4j.rows.RowType;
import dev.zeith.db4j.util.SQLHelper;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.util.UUID;

public class RowTypeUUIDAsBlob
		extends RowType<UUID>
{
	public RowTypeUUIDAsBlob()
	{
		super(SQLDataType.BLOB, UUID.class);
	}
	
	@Override
	public void set(@NotNull PreparedStatement statement, int parameterIndex, UUID value)
			throws SQLException
	{
		if(value == null)
			statement.setNull(parameterIndex, type.getSqlType());
		else
			statement.setBytes(parameterIndex, SQLHelper.uuidToBytes(value));
	}
	
	@Override
	public UUID get(@NotNull ResultSet set, int columnIndex)
			throws SQLException
	{
		return SQLHelper.bytesToUuid(set.getBytes(columnIndex));
	}
	
	@Override
	public UUID get(@NotNull ResultSet set, @NotNull String columnLabel)
			throws SQLException
	{
		return SQLHelper.bytesToUuid(set.getBytes(columnLabel));
	}
}
