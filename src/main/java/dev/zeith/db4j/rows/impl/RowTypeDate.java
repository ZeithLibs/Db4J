package dev.zeith.db4j.rows.impl;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import dev.zeith.db4j.data.SQLDataType;
import dev.zeith.db4j.rows.RowType;
import org.jetbrains.annotations.NotNull;

public class RowTypeDate extends RowType<Date>
{
	public RowTypeDate()
	{
		super(SQLDataType.DATE, Date.class);
	}
	
	@Override
	public void set(@NotNull PreparedStatement statement, int parameterIndex, Date value) throws SQLException
	{
		statement.setDate(parameterIndex, value);
	}
	
	@Override
	public Date get(@NotNull ResultSet set, int columnIndex) throws SQLException
	{
		return set.getDate(columnIndex);
	}
	
	@Override
	public Date get(@NotNull ResultSet set, @NotNull String columnLabel) throws SQLException
	{
		return set.getDate(columnLabel);
	}
}