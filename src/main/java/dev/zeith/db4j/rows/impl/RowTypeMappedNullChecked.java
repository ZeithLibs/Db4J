package dev.zeith.db4j.rows.impl;

import dev.zeith.db4j.rows.RowType;
import org.jetbrains.annotations.*;

import java.sql.*;
import java.util.function.Function;

public class RowTypeMappedNullChecked<SRC, DST>
		extends RowType<DST>
{
	protected final RowType<SRC> src;
	protected final Function<@NotNull SRC, @Nullable DST> mapper;
	protected final Function<@NotNull DST, @Nullable SRC> unmapper;
	
	public RowTypeMappedNullChecked(RowType<SRC> src, Class<DST> javaType, Function<@NotNull SRC, @Nullable DST> mapper, Function<@NotNull DST, @Nullable SRC> unmapper)
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
		src.set(statement, parameterIndex, value != null ? unmapper.apply(value) : null);
	}
	
	@Override
	public DST get(@NotNull ResultSet set, int columnIndex)
			throws SQLException
	{
		SRC s = src.get(set, columnIndex);
		return s != null ? mapper.apply(s) : null;
	}
	
	@Override
	public DST get(@NotNull ResultSet set, @NotNull String columnLabel)
			throws SQLException
	{
		SRC s = src.get(set, columnLabel);
		return s != null ? mapper.apply(s) : null;
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