package dev.zeith.db4j.rows;

import dev.zeith.db4j.data.ISQLDataType;

import java.sql.*;

public abstract class RowType<DATA>
{
	public final ISQLDataType type;
	public final Class<DATA> javaType;
	
	public RowType(ISQLDataType type, Class<DATA> javaType)
	{
		this.type = type;
		this.javaType = javaType;
	}
	
	public abstract void set(PreparedStatement statement, int columnIndex, DATA value) throws SQLException;
	
	public abstract DATA get(ResultSet set, int columnIndex) throws SQLException;
	
	public abstract DATA get(ResultSet set, String columnLabel) throws SQLException;
}