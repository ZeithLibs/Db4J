package dev.zeith.db4j.backup;

import lombok.RequiredArgsConstructor;

import java.sql.*;
import java.util.*;

@RequiredArgsConstructor
public class BackupApplier
		implements IBackupReceptor<SQLException>
{
	protected final Connection target;
	protected final List<String> copiedTables = new ArrayList<>();
	protected String catalog;
	
	protected final boolean writeTableData;
	
	@Override
	public void dropTables()
			throws SQLException
	{
		DatabaseMetaData meta = target.getMetaData();
		Statement targetStmt = target.createStatement();
		
		catalog = target.getCatalog();
		
		targetStmt.executeUpdate("SET FOREIGN_KEY_CHECKS = 0");
		
		// Step 1: Drop all tables in backup DB
		List<String> existingTables = new ArrayList<>();
		try(ResultSet tables = meta.getTables(null, null, "%", new String[] {"TABLE"}))
		{
			while(tables.next())
			{
				if(!tables.getString("TABLE_CAT").equals("univcc")) continue;
				existingTables.add(tables.getString("TABLE_NAME"));
			}
		}
		
		do
		{
			Set<String> removed = new HashSet<>();
			for(String table : existingTables)
			{
				try
				{
					targetStmt.executeUpdate("DROP TABLE IF EXISTS `" + table + "` CASCADE");
				} catch(SQLException e)
				{
					if(e.getMessage().contains("foreign key constraint"))
						continue;
					throw e;
				}
				removed.add(table);
			}
			existingTables.removeAll(removed);
		} while(!existingTables.isEmpty());
		
		try(ResultSet rs = meta.getTables(catalog, null, "%", new String[] {"VIEW"}))
		{
			while(rs.next())
			{
				String view = rs.getString("TABLE_NAME");
				targetStmt.executeUpdate("DROP VIEW IF EXISTS `" + view + "`");
			}
		}
		
		// Drop all routines (functions & procedures)
		try(ResultSet rs = meta.getProcedures(catalog, null, "%"))
		{
			while(rs.next())
			{
				String proc = rs.getString("PROCEDURE_NAME");
				targetStmt.executeUpdate("DROP PROCEDURE IF EXISTS `" + proc + "`");
			}
		}
		try(ResultSet rs = meta.getFunctions(catalog, null, "%"))
		{
			while(rs.next())
			{
				String func = rs.getString("FUNCTION_NAME");
				targetStmt.executeUpdate("DROP FUNCTION IF EXISTS `" + func + "`");
			}
		}
		
		// Drop all triggers
		try(ResultSet rs = targetStmt.executeQuery(
				"SELECT TRIGGER_NAME FROM information_schema.triggers WHERE TRIGGER_SCHEMA = '" + catalog + "'"))
		{
			while(rs.next())
			{
				targetStmt.executeUpdate("DROP TRIGGER IF EXISTS `" + rs.getString(1) + "`");
			}
		}
		
		// Drop all events
		try(ResultSet rs = targetStmt.executeQuery(
				"SELECT EVENT_NAME FROM information_schema.events WHERE EVENT_SCHEMA = '" + catalog + "'"))
		{
			while(rs.next())
			{
				targetStmt.executeUpdate("DROP EVENT IF EXISTS `" + rs.getString(1) + "`");
			}
		}
		
		targetStmt.executeUpdate("SET FOREIGN_KEY_CHECKS = 1");
		targetStmt.close();
	}
	
	@Override
	public void setTables(LinkedHashSet<String> copiedTables)
	{
		this.copiedTables.clear();
		this.copiedTables.addAll(copiedTables);
	}
	
	@Override
	public void useSqlCommand(String originCatalog, String sql)
			throws SQLException
	{
		var backupDb = Objects.requireNonNull(catalog, "dropTables was not called");
		try(Statement bSt = target.createStatement())
		{
			bSt.executeUpdate("USE `" + backupDb + "`");
			bSt.executeUpdate(sql.replace("`" + originCatalog + "`", "`" + backupDb + "`"));
		}
	}
	
	@Override
	public void sqlCommand(String sql)
			throws SQLException
	{
		try(Statement targetStmt = target.createStatement())
		{
			targetStmt.executeUpdate(sql);
		}
	}
	
	@Override
	public Batch<SQLException> insertSql(String sql)
			throws SQLException
	{
		if(!writeTableData) return new Batch<>()
		{
			@Override
			public void addBatch()
			{
			}
			
			@Override
			public void setObject(int i, Object object)
			{
			}
			
			@Override
			public void executeBatch()
			{
			}
			
			@Override
			public void close()
			{
			}
		};
		
		PreparedStatement stmt = target.prepareStatement(sql);
		return new Batch<>()
		{
			@Override
			public void addBatch()
					throws SQLException
			{
				stmt.addBatch();
			}
			
			@Override
			public void setObject(int i, Object object)
					throws SQLException
			{
				stmt.setObject(i, object);
			}
			
			@Override
			public void executeBatch()
					throws SQLException
			{
				stmt.executeBatch();
			}
			
			@Override
			public void close()
					throws SQLException
			{
				stmt.close();
			}
		};
	}
	
	@Override
	public void close()
			throws SQLException
	{
		target.close();
	}
}