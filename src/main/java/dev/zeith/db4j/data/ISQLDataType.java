package dev.zeith.db4j.data;

public interface ISQLDataType
{
	int getSqlType();
	
	String getSQLDataType();
	
	String getSQLDataTypeNoArgs();
}