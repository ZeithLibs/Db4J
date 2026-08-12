package dev.zeith.db4j.data;

public enum ComparisonMode
{
	EXACTLY("="), //
	NOT_EQUAL("<>"), //
	GREATER_THAN(">"),
	GREATER_EQ_THAN(">="), //
	LESS_THAN("<"),
	LESS_EQ_THAN("<=");
	
	public final String sql;
	
	ComparisonMode(String sql)
	{
		this.sql = sql;
	}
}