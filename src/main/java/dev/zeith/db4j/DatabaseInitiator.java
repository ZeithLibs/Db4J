package dev.zeith.db4j;

import java.io.File;
import java.sql.*;
import java.util.Properties;

public class DatabaseInitiator
		implements IDatabaseSource
{
	protected final String path;
	private Properties properties;
	private boolean closed = false;
	private IDatabaseSession active;
	
	public DatabaseInitiator(String path)
	{
		this.path = path;
	}
	
	public DatabaseInitiator setProperty(String key, String value)
	{
		if(properties == null) properties = new Properties();
		properties.put(key, value);
		return this;
	}
	
	@SuppressWarnings("resource")
	public DatabaseInitiator auth(String user, String password)
	{
		return setProperty("user", user).setProperty("password", password);
	}
	
	@Override
	public String getPath()
	{
		return path;
	}
	
	public IDatabaseSession getPrimaryConnection()
			throws SQLException
	{
		if(!closed && (active == null || active.isClosed()))
			active = createConnection();
		return active;
	}
	
	@Override
	public IDatabaseSession createConnection()
			throws SQLException
	{
		if(this.properties != null)
			return IDatabaseSession.of(DriverManager.getConnection(this.path, this.properties));
		return IDatabaseSession.of(DriverManager.getConnection(path));
	}
	
	public static DatabaseInitiator initiatorSQLite(File file)
	{
		return new DatabaseInitiator("jdbc:sqlite:" + file.getAbsolutePath());
	}
	
	@Override
	public void close()
			throws SQLException
	{
		closed = true;
		if(active != null && !active.isClosed())
		{
			active.close();
		}
		active = null;
	}
}