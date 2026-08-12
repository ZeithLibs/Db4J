package dev.zeith.db4j.rows;

import dev.zeith.db4j.data.SQLDataType;
import dev.zeith.db4j.rows.impl.*;

public class RowTypes
{
	// BASIC TYPES
	
	public static final RowTypeString TINY_TEXT = new RowTypeString(SQLDataType.TINY_TEXT);
	public static final RowTypeString TEXT = new RowTypeString(SQLDataType.TEXT);
	public static final RowTypeString MEDIUM_TEXT = new RowTypeString(SQLDataType.MEDIUM_TEXT);
	public static final RowTypeString LONG_TEXT = new RowTypeString(SQLDataType.LONG_TEXT);
	public static final RowTypeString VARCHAR = new RowTypeString(SQLDataType.VARCHAR);
	public static final RowTypeString VARCHAR_32 = new RowTypeString(SQLDataType.VARCHAR.withLength(32));
	
	public static final RowTypeFloat FLOAT = new RowTypeFloat();
	public static final RowTypeDouble DOUBLE = new RowTypeDouble();
	public static final RowTypeBool BOOLEAN = new RowTypeBool();
	public static final RowTypeShort SMALL_INT = new RowTypeShort();
	public static final RowTypeInt INT = new RowTypeInt();
	public static final RowTypeLong BIG_INT = new RowTypeLong();
	
	public static final RowTypeDate DATE = new RowTypeDate();
	public static final RowTypeTimestamp TIMESTAMP = new RowTypeTimestamp();
	
	// ADVANCED TYPES
	
	public static final RowTypeBlob BLOB = new RowTypeBlob(SQLDataType.BLOB);
	
	public static final RowTypeInstantMsPrecision INSTANT = new RowTypeInstantMsPrecision();
	public static final RowTypeInstantSecPrecision INSTANT_WITH_SECOND_PRECISION = new RowTypeInstantSecPrecision();
	
	public static final RowTypeUUIDAsBlob UUID_AS_BLOB = new RowTypeUUIDAsBlob();
	public static final RowTypeUUIDAsBinary UUID_AS_BINARY = new RowTypeUUIDAsBinary();
}
