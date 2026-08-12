package dev.zeith.db4j.data;

public record SQLDataTypeWithParams(ISQLDataType type, String parameters)
		implements ISQLDataType
{
	@Override
	public int getSqlType()
	{
		return type.getSqlType();
	}
	
	@Override
	public String getSQLDataType()
	{
		String s = type.getSQLDataTypeNoArgs();
		if(parameters != null && !parameters.isBlank()) return s + "(" + parameters + ")";
		return s;
	}
	
	@Override
	public String getSQLDataTypeNoArgs()
	{
		return type.getSQLDataTypeNoArgs();
	}
}