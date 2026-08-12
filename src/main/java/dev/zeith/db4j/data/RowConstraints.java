package dev.zeith.db4j.data;

public enum RowConstraints
{
	NOT_NULL("NOT NULL"),
	UNIQUE("UNIQUE"),
	PRIMARY_KEY("PRIMARY KEY"),
	FOREIGN_KEY("FOREIGN KEY"),
	CHECK("CHECK"),
	DEFAULT("DEFAULT"),
	AUTOINCREMENT("AUTOINCREMENT"),
	;
	
	final String sql;
	
	RowConstraints(String sql)
	{
		this.sql = sql;
	}
	
	public String sql()
	{
		return sql;
	}
}