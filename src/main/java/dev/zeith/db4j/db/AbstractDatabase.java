package dev.zeith.db4j.db;

import dev.zeith.db4j.*;

import java.sql.*;
import java.util.*;

public abstract class AbstractDatabase
		implements AutoCloseable
{
	public boolean log = false;
	
	protected final DatabaseInitiator sql;
	private final List<DatabaseTable> tables = new ArrayList<>();
	private final Map<String, DatabaseTable> tableMap = new HashMap<>();
	
	protected <T extends DatabaseTable> T registerTable(T table)
	{
		if(tableMap.containsKey(table.getName()))
			throw new IllegalArgumentException("Table with name " + table.getName() + " is already registered!");
		tables.add(table);
		tableMap.put(table.getName(), table);
		table.setDb(this);
		return table;
	}
	
	public AbstractDatabase(DatabaseInitiator sql)
	{
		this.sql = sql;
	}
	
	public void vacuum()
			throws SQLException
	{
		try(PreparedStatement stmt = prepareStatement(getSession(), "VACUUM"))
		{
			stmt.executeUpdate();
		}
	}
	
	public void createNonExistentTables()
			throws SQLException
	{
		for(DatabaseTable t : tables)
			t.createIfNotExist();
	}
	
	public IDatabaseSession getSession()
			throws SQLException
	{
		return this.sql.getPrimaryConnection();
	}
	
	public IDatabaseSession openSession()
			throws SQLException
	{
		return this.sql.createConnection();
	}
	
	public PreparedStatement prepareStatement(IDatabaseSession session, String sql)
			throws SQLException
	{
		if(log) log("prepareStatement: " + sql);
		
		// Fallback to primary connection
		if(session == null) session = getSession();
		
		return session.prepareStatement(sql);
	}
	
	@Override
	public void close()
			throws SQLException
	{
		sql.close();
	}
	
	public void log(String text)
	{
		System.out.println("[" + getClass().getSimpleName() + "] " + text);
	}
}