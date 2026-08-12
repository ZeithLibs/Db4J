package dev.zeith.db4j.backup;

import lombok.RequiredArgsConstructor;

import java.sql.*;
import java.util.*;

@RequiredArgsConstructor
public class BackupFactory<E extends Exception>
		implements AutoCloseable
{
	protected final Connection source;
	protected final IBackupReceptor<E> target;
	
	public void backup()
			throws SQLException, E
	{
		DatabaseMetaData meta = source.getMetaData();
		
		target.dropTables();
		
		Set<String> remainingTables = new HashSet<>();
		LinkedHashSet<String> copiedTables = new LinkedHashSet<>();
		
		try(ResultSet tables = meta.getTables(null, null, "%", new String[] {"TABLE"}))
		{
			while(tables.next())
			{
				if(!tables.getString("TABLE_CAT").equals("univcc")) continue;
				String tableName = tables.getString("TABLE_NAME");
				remainingTables.add(tableName);
				System.out.println("Detected table: " + tableName);
			}
		}
		
		// Step 2: Copy tables structure
		do
		{
			Set<String> added = new HashSet<>();
			for(String tableName : remainingTables)
			{
				try
				{
					System.out.println("Recreating " + tableName);
					copyTableStructure(tableName);
					System.out.println("Created table " + tableName);
					copiedTables.add(tableName);
					added.add(tableName);
				} catch(SQLException e)
				{
					continue;
				}
			}
			remainingTables.removeAll(added);
		} while(!remainingTables.isEmpty());
		
		target.setTables(copiedTables);
		System.out.println("Structure copied.");
		
		for(String cp : copiedTables)
		{
			System.out.println("Copying data for " + cp);
			copyTableData(cp);
		}
		
		String prodDb = source.getCatalog();

		// === Copy views ===
		copyObjects(prodDb, "VIEW", "SHOW CREATE VIEW");

		// === Copy triggers ===
		copyObjects(prodDb, "TRIGGER", "SHOW CREATE TRIGGER");

		// === Copy procedures ===
		copyObjects(prodDb, "PROCEDURE", "SHOW CREATE PROCEDURE");

		// === Copy functions ===
		copyObjects(prodDb, "FUNCTION", "SHOW CREATE FUNCTION");

		// === Copy events ===
		copyObjects(prodDb, "EVENT", "SHOW CREATE EVENT");
	}
	
	private void copyTableStructure(String table)
			throws SQLException,  E
	{
		try(Statement sourceStmt = source.createStatement();
			ResultSet rs = sourceStmt.executeQuery("SHOW CREATE TABLE `" + table + "`"))
		{
			if(rs.next())
			{
				String createSQL = rs.getString(2); // second column is the CREATE statement
				target.sqlCommand(createSQL);
			}
		}
	}
	
	private void copyTableData(String table)
			throws SQLException, E
	{
		String selectSQL = "SELECT * FROM `" + table + "`";
		try(Statement sourceStmt = source.createStatement();
			ResultSet rs = sourceStmt.executeQuery(selectSQL))
		{
			ResultSetMetaData meta = rs.getMetaData();
			int columns = meta.getColumnCount();
			
			StringBuilder insertSQL = new StringBuilder();
			insertSQL.append("INSERT INTO `").append(table).append("` VALUES (");
			for(int i = 0; i < columns; i++)
			{
				insertSQL.append("?");
				if(i < columns - 1) insertSQL.append(",");
			}
			insertSQL.append(")");
			
			try(var targetStmt = target.insertSql(insertSQL.toString()))
			{
				int batch = 0;
				while(rs.next())
				{
					for(int i = 1; i <= columns; i++)
					{
						targetStmt.setObject(i, rs.getObject(i));
					}
					targetStmt.addBatch();
					batch++;
					if(batch % 500 == 0)
					{
						targetStmt.executeBatch();
					}
				}
				targetStmt.executeBatch();
			}
		}
	}
	
	@Override
	public void close()
			throws SQLException, E
	{
		source.close();
		target.close();
	}
	
	private void copyObjects(String prodDb, String objectType, String showCreatePrefix)
			throws SQLException, E
	{
		String sql = "SELECT ROUTINE_NAME FROM information_schema.routines WHERE ROUTINE_SCHEMA = ? AND ROUTINE_TYPE = ?";
		if(objectType.equals("TRIGGER"))
			sql = "SELECT TRIGGER_NAME FROM information_schema.triggers WHERE TRIGGER_SCHEMA = ?";
		if(objectType.equals("VIEW"))
			sql = "SELECT TABLE_NAME FROM information_schema.views WHERE TABLE_SCHEMA = ?";
		if(objectType.equals("EVENT"))
			sql = "SELECT EVENT_NAME FROM information_schema.events WHERE EVENT_SCHEMA = ?";
		
		try(PreparedStatement stmt = source.prepareStatement(sql))
		{
			stmt.setString(1, prodDb);
			if(sql.contains("ROUTINE_TYPE")) stmt.setString(2, objectType);
			
			try(ResultSet rs = stmt.executeQuery())
			{
				while(rs.next())
				{
					String name = rs.getString(1);
					System.out.printf("Backing up %s: %s%n", objectType, name);
					
					try(Statement st = source.createStatement();
						ResultSet crs = st.executeQuery(showCreatePrefix + " `" + prodDb + "`.`" + name + "`"))
					{
						if(crs.next())
						{
							String createSql = crs.getString(3);
							
							target.useSqlCommand(prodDb, createSql);
						}
					}
				}
			}
		}
	}
}