package dev.zeith.db4j.rows;

import dev.zeith.db4j.data.ISQLDataType;
import dev.zeith.db4j.rows.impl.*;
import org.jetbrains.annotations.*;

import java.sql.*;
import java.util.function.Function;

public abstract class RowType<DATA>
{
	public final ISQLDataType type;
	public final Class<DATA> javaType;
	
	public RowType(ISQLDataType type, Class<DATA> javaType)
	{
		this.type = type;
		this.javaType = javaType;
	}
	
	public abstract void set(@NotNull PreparedStatement statement, int parameterIndex, @Nullable DATA value)
			throws SQLException;
	
	@Nullable
	public abstract DATA get(@NotNull ResultSet set, int columnIndex)
			throws SQLException;
	
	@Nullable
	public abstract DATA get(@NotNull ResultSet set, @NotNull String columnLabel)
			throws SQLException;
	
	public <NEW> RowType<NEW> map(Class<NEW> targetType, Function<@Nullable DATA, @Nullable NEW> mapper, Function<@Nullable NEW, @Nullable DATA> unmapper)
	{
		return new RowTypeMapped<>(this, targetType, mapper, unmapper);
	}
	
	public <NEW> RowType<NEW> mapNotNull(Class<NEW> targetType, Function<@NotNull DATA, @Nullable NEW> mapper, Function<@NotNull NEW, @Nullable DATA> unmapper)
	{
		return new RowTypeMappedNullChecked<>(this, targetType, mapper, unmapper);
	}
}