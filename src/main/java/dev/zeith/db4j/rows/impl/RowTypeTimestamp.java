package dev.zeith.db4j.rows.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import dev.zeith.db4j.data.SQLDataType;
import dev.zeith.db4j.rows.RowType;

public class RowTypeTimestamp extends RowType<Timestamp>
{
	public RowTypeTimestamp()
	{
		super(SQLDataType.TIMESTAMP, Timestamp.class);
	}
	
	@Override
	public void set(PreparedStatement statement, int columnIndex, Timestamp value) throws SQLException
	{
		statement.setTimestamp(columnIndex, value);
	}
	
	@Override
	public Timestamp get(ResultSet set, int columnIndex) throws SQLException
	{
		return set.getTimestamp(columnIndex);
	}
	
	@Override
	public Timestamp get(ResultSet set, String columnLabel) throws SQLException
	{
		return set.getTimestamp(columnLabel);
	}
}