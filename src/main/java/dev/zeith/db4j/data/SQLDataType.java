package dev.zeith.db4j.data;

import java.sql.Types;

public enum SQLDataType
		implements ISQLDataType
{
	BYTE("TINYINT", Types.TINYINT, "A tiny integer. Signed range is from -128 to 127. Unsigned range is from 0 to 255."),
	SHORT("SMALLINT", Types.SMALLINT, "A small integer. Signed range is from -32768 to 32767. Unsigned range is from 0 to 65535."),
	INT("INTEGER", Types.INTEGER, "A medium integer. Signed range is from -2147483648 to 2147483647. Unsigned range is from 0 to 4294967295."),
	REAL("REAL", Types.REAL, "32-bit float"),
	FLOAT("FLOAT", Types.FLOAT, "32 or 64 bit float"),
	LONG("BIGINT", Types.BIGINT, "A large integer. Signed range is from -9223372036854775808 to 9223372036854775807. Unsigned range is from 0 to 18446744073709551615."),
	BOOLEAN("BOOLEAN", Types.BOOLEAN, "Zero is considered as false, nonzero values are considered as true."),
	TINY_TEXT("TINYTEXT", Types.NVARCHAR, "Holds a string with a maximum length of 255 characters."),
	TEXT("TEXT", Types.NVARCHAR, "Holds a string with a maximum length of 65,535 bytes."),
	MEDIUM_TEXT("MEDIUMTEXT", Types.NVARCHAR, "Holds a string with a maximum length of 16,777,215 characters"),
	LONG_TEXT("LONGTEXT", Types.LONGNVARCHAR, "Holds a string with a maximum length of 4,294,967,295 characters."),
	VARCHAR("VARCHAR", Types.VARCHAR, "Array of characters"),
	BINARY("BINARY", Types.BINARY, "Holds binary data."),
	BLOB("BLOB", Types.BLOB, "Holds binary data."),
	DATE("DATE", Types.DATE, "A calendar date (year, month, day) without time."),
	TIMESTAMP("TIMESTAMP", Types.TIMESTAMP, "A date and time value with nanosecond precision."),
	;
	
	private final String sql;
	private final int sqlType;
	
	SQLDataType(String sql, int sqlType, String comment)
	{
		this.sql = sql;
		this.sqlType = sqlType;
	}
	
	@Override
	public int getSqlType()
	{
		return sqlType;
	}
	
	@Override
	public String getSQLDataType()
	{
		return sql;
	}
	
	@Override
	public String getSQLDataTypeNoArgs()
	{
		return sql;
	}
	
	public SQLDataTypeWithParams withLength(long length)
	{
		return new SQLDataTypeWithParams(this, Long.toUnsignedString(length));
	}
}