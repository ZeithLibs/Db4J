package dev.zeith.db4j.db;

import dev.zeith.db4j.DatabaseInitiator;

public class DummyDatabase extends AbstractDatabase
{
	public DummyDatabase(DatabaseInitiator sql)
	{
		super(sql);
	}
	
	@Override
	public <T extends DatabaseTable> T registerTable(T table)
	{
		return super.registerTable(table);
	}
}