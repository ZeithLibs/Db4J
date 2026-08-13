package dev.zeith.db4j.rows.impl;

import dev.zeith.db4j.data.SQLDataType;
import dev.zeith.db4j.rows.RowType;

import java.sql.*;

public class RowTypeEnum<E extends Enum<E>>
		extends RowType<E>
{
	private final E[] enums;
	
	public RowTypeEnum(SQLDataType type, Class<E> javaType)
	{
		super(type, javaType);
		if(type != SQLDataType.BYTE && type != SQLDataType.SHORT && type != SQLDataType.INT)
			throw new UnsupportedOperationException("Unsupported SQL type for enum(" + javaType + ") row: " + type);
		this.enums = javaType.getEnumConstants();
	}
	
	@Override
	public void set(PreparedStatement statement, int columnIndex, E value)
			throws SQLException
	{
		if(value == null)
		{
			statement.setNull(columnIndex, type.getSqlType());
			return;
		}
		
		if(this.type == SQLDataType.BYTE)
		{
			statement.setByte(columnIndex, (byte) value.ordinal());
			return;
		}
		
		if(this.type == SQLDataType.SHORT)
		{
			statement.setShort(columnIndex, (short) value.ordinal());
			return;
		}
		
		if(this.type == SQLDataType.INT)
		{
			statement.setInt(columnIndex, value.ordinal());
			return;
		}
	}
	
	@Override
	public E get(ResultSet set, int columnIndex)
			throws SQLException
	{
		if(this.type == SQLDataType.BYTE)
			return enums[set.getByte(columnIndex)];
		
		if(this.type == SQLDataType.SHORT)
			return enums[set.getShort(columnIndex)];
		
		if(this.type == SQLDataType.INT)
			return enums[set.getInt(columnIndex)];
		
		throw new SQLException("Unsupported SQL type: " + type + " for enum " + javaType);
	}
	
	@Override
	public E get(ResultSet set, String columnLabel)
			throws SQLException
	{
		if(this.type == SQLDataType.BYTE)
			return enums[set.getByte(columnLabel)];
		
		if(this.type == SQLDataType.SHORT)
			return enums[set.getShort(columnLabel)];
		
		if(this.type == SQLDataType.INT)
			return enums[set.getInt(columnLabel)];
		
		throw new SQLException("Unsupported SQL type: " + type + " for enum " + javaType);
	}
	
	public static SQLDataType forLength(int count)
	{
		if(count < 255) return SQLDataType.BYTE;
		else if(count < 65535) return SQLDataType.SHORT;
		return SQLDataType.INT;
	}
}
