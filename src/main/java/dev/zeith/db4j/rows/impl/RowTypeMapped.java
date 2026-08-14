package dev.zeith.db4j.rows.impl;

import dev.zeith.db4j.rows.RowType;

import java.sql.*;
import java.util.function.Function;

public class RowTypeMapped<SRC, DST>
		extends RowType<DST>
{
	protected final RowType<SRC> src;
	protected final Function<SRC, DST> mapper;
	protected final Function<DST, SRC> unmapper;
	
	public RowTypeMapped(RowType<SRC> src, Class<DST> javaType, Function<SRC, DST> mapper, Function<DST, SRC> unmapper)
	{
		super(src.type, javaType);
		this.src = src;
		this.mapper = mapper;
		this.unmapper = unmapper;
	}
	
	@Override
	public void set(PreparedStatement statement, int columnIndex, DST value)
			throws SQLException
	{
		src.set(statement, columnIndex, unmapper.apply(value));
	}
	
	@Override
	public DST get(ResultSet set, int columnIndex)
			throws SQLException
	{
		return mapper.apply(src.get(set, columnIndex));
	}
	
	@Override
	public DST get(ResultSet set, String columnLabel)
			throws SQLException
	{
		return mapper.apply(src.get(set, columnLabel));
	}
	
	@Override
	public String toString()
	{
		return "RowTypeMapped{" +
		       "src=" + src +
		       ", mapper=" + mapper +
		       ", unmapper=" + unmapper +
		       '}';
	}
}