package dev.zeith.db4j.rows.impl;

import dev.zeith.db4j.rows.RowType;
import org.jetbrains.annotations.*;

import java.sql.*;
import java.util.function.Function;

public class RowTypeMapped<SRC, DST>
		extends RowType<DST>
{
	protected final RowType<SRC> src;
	protected final Function<@Nullable SRC, @Nullable DST> mapper;
	protected final Function<@Nullable DST, @Nullable SRC> unmapper;
	
	public RowTypeMapped(RowType<SRC> src, Class<DST> javaType, Function<@Nullable SRC, @Nullable DST> mapper, Function<@Nullable DST, @Nullable SRC> unmapper)
	{
		super(src.type, javaType);
		this.src = src;
		this.mapper = mapper;
		this.unmapper = unmapper;
	}
	
	@Override
	public void set(@NotNull PreparedStatement statement, int parameterIndex, DST value)
			throws SQLException
	{
		src.set(statement, parameterIndex, unmapper.apply(value));
	}
	
	@Override
	public DST get(@NotNull ResultSet set, int columnIndex)
			throws SQLException
	{
		return mapper.apply(src.get(set, columnIndex));
	}
	
	@Override
	public DST get(@NotNull ResultSet set, @NotNull String columnLabel)
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