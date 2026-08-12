package dev.zeith.db4j.rows.impl;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import dev.zeith.db4j.data.SQLDataType;
import dev.zeith.db4j.rows.RowType;

public class RowTypeDate extends RowType<Date>
{
	public RowTypeDate()
	{
		super(SQLDataType.DATE, Date.class);
	}
	
	@Override
	public void set(PreparedStatement statement, int columnIndex, Date value) throws SQLException
	{
		statement.setDate(columnIndex, value);
	}
	
	@Override
	public Date get(ResultSet set, int columnIndex) throws SQLException
	{
		return set.getDate(columnIndex);
	}
	
	@Override
	public Date get(ResultSet set, String columnLabel) throws SQLException
	{
		return set.getDate(columnLabel);
	}
}