package dev.zeith.db4j.db;

import dev.zeith.db4j.IDatabaseSession;
import dev.zeith.db4j.data.RowConstraints;
import dev.zeith.db4j.query.*;
import dev.zeith.db4j.rows.TableRow;

import java.sql.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.UnaryOperator;
import java.util.stream.*;

public class DatabaseTable
{
	private AbstractDatabase db;
	
	protected final List<TableRow<?>> rows;
	protected final Map<String, TableRow<?>> rowsByName;
	protected final Map<String, TableRow<?>> rowsByNameUnmod;
	protected final String name;
	
	public DatabaseTable(String name, TableRow<?>... rows)
	{
		this(name, Arrays.asList(rows));
	}
	
	public DatabaseTable(String name, List<TableRow<?>> rows)
	{
		this.name = name;
		this.rows = new ArrayList<>(rows);
		this.rowsByName = this.rows.stream().collect(Collectors.toMap(e -> e.name, UnaryOperator.identity()));
		this.rowsByNameUnmod = Collections.unmodifiableMap(this.rowsByName);
	}
	
	public void createIfNotExist()
			throws SQLException
	{
		createIfNotExistCore();
		alterColumns();
		createIndices();
	}
	
	protected void createIndices()
			throws SQLException
	{
//		createIndexIfNotExists("ft_project_name", """
//				ADD FULLTEXT INDEX ft_project_name (%s);
//				""".formatted(MyTable.UID.name)
//		);
	}
	
	protected void createIndexIfNotExists(String indexName, String stmt)
			throws SQLException
	{
		var conn = db.getSession().getConnection();
		DatabaseMetaData meta = conn.getMetaData();
		String cat = conn.getCatalog();
		String schema = conn.getSchema();
		
		try(ResultSet rs = meta.getIndexInfo(cat, schema, getName(), false, false))
		{
			while(rs.next())
				if(indexName.equals(rs.getString("INDEX_NAME")))
					return;
		}
		
		try(var s = conn.prepareStatement("ALTER TABLE " + getName() + "\n" + stmt))
		{
			s.executeUpdate();
		}
	}
	
	protected void createIfNotExistCore()
			throws SQLException
	{
		try(PreparedStatement stmt = db.prepareStatement(db.getSession(),
				"CREATE TABLE IF NOT EXISTS " + getName() + " (" +
				Stream.concat(rows.stream().map(TableRow::toPreparedSQLString),
						rows.stream().map(TableRow::getExtraSQLCreationFlags)
						    .filter(s -> s != null && !s.isEmpty())
				).collect(Collectors.joining(", ")) + ")"
		))
		{
			stmt.execute();
		}
	}
	
	protected void alterColumns()
			throws SQLException
	{
		Map<String, String> dbColumns = new HashMap<>();
		try(PreparedStatement stmt = db.prepareStatement(db.getSession(), "SELECT * FROM " + getName() + " LIMIT 0"))
		{
			ResultSet set = stmt.executeQuery();
			ResultSetMetaData meta = set.getMetaData();
			for(int i = 1; i <= meta.getColumnCount(); i++)
				dbColumns.put(meta.getColumnLabel(i), meta.getColumnTypeName(i));
		}
		
		Set<String> dbKeys = dbColumns.keySet();
		Set<String> ourKeys = rowsByName.keySet();
		
		if(!dbKeys.equals(ourKeys))
		{
			for(String dbk : dbKeys)
			{
				if(!ourKeys.contains(dbk))
				{
					try(PreparedStatement stmt = db.prepareStatement(db.getSession(), "ALTER TABLE " + getName() + " DROP COLUMN " + dbk))
					{
						stmt.execute();
					}
				}
			}
			
			for(String ourk : ourKeys)
			{
				if(!dbKeys.contains(ourk))
				{
					try(PreparedStatement stmt = db.prepareStatement(db.getSession(), "ALTER TABLE " + getName() + " ADD " + ourk + " " + rowsByName.get(ourk).getSQLTypeStr()))
					{
						stmt.execute();
					}
				}
			}
		} else
		{
			for(String dbk : dbKeys)
			{
				String provType = dbColumns.get(dbk);
				TableRow<?> req = rowsByName.get(dbk);
				String reqType = req.getSQLTypeStrNoArgs();
				if(db.log)
					db.log("Compare table types: " + provType + " [expect " + reqType + "]");
				if(!provType.equalsIgnoreCase(reqType))
				{
					try(PreparedStatement stmt = db.prepareStatement(db.getSession(), "ALTER TABLE " + getName() + " MODIFY COLUMN " + dbk + " " + req.getSQLTypeStr()))
					{
						stmt.execute();
					}
				}
			}
		}
	}
	
	public InsertMap createInsertMap()
	{
		InsertMap imap = new InsertMap(rows.size());
		for(TableRow<?> row : rows)
		{
			if(row.constraints.contains(RowConstraints.NOT_NULL))
				continue; // Require to assign non null keys manually!
			imap.insertNULL(row);
		}
		return imap;
	}
	
	@SuppressWarnings({"rawtypes", "unchecked"})
	public void insert(IDatabaseSession session, Map<TableRow<?>, ?> data)
			throws SQLException
	{
		List<Entry<TableRow<?>, ?>> entries = new ArrayList<>(data.entrySet());
		
		try(PreparedStatement stmt = db.prepareStatement(session,
				"INSERT INTO "
				+ name
				+ "(" +
				entries.stream().map(a -> a.getKey().name).collect(Collectors.joining(", "))
				+ ") VALUES(" +
				entries.stream().map(a -> "?").collect(Collectors.joining(", "))
				+ ")"
		))
		{
			int j = 1;
			
			for(Entry<TableRow<?>, ?> e : entries)
			{
				TableRow row = e.getKey();
				Object d0 = e.getValue();
				if(d0 == null && row.constraints.contains(RowConstraints.NOT_NULL))
					throw new NullPointerException(row.name + " can not be null!");
				row.set(stmt, j, d0);
				++j;
			}
			
			stmt.executeUpdate();
		}
	}
	
	@SuppressWarnings({"rawtypes", "unchecked"})
	public void batchInsert(IDatabaseSession session, List<InsertMap> data)
			throws SQLException
	{
		List<Set<TableRow<?>>> batches = data.stream().map(InsertMap::keySet).distinct().toList();
		
		for(Set<TableRow<?>> batch : batches)
		{
			List<InsertMap> batchData = data.stream().filter(i -> i.keySet().equals(batch)).toList();
			
			try(PreparedStatement stmt = db.prepareStatement(session,
					"INSERT INTO " + name
					+ "(" +
					batch.stream().map(a -> a.name).collect(Collectors.joining(", "))
					+ ") VALUES(" +
					batch.stream().map(a -> "?").collect(Collectors.joining(", "))
					+ ")"
			))
			{
				for(int i = 0; i < batchData.size(); ++i)
				{
					int j = 1;
					
					for(TableRow row : batch)
					{
						Object d0 = batchData.get(i).get(row);
						if(d0 == null && row.constraints.contains(RowConstraints.NOT_NULL))
							throw new NullPointerException(row.name + " can not be null!");
						row.set(stmt, j, d0);
						++j;
					}
					
					stmt.addBatch();
				}
				
				stmt.executeBatch();
			}
		}
	}
	
	public QueryFilter allEntries()
	{
		return new QueryFilter(this);
	}
	
	public PreparedStatement prepQuery(IDatabaseSession session, QueryFilter filter)
			throws SQLException
	{
		String select = buildSelectList(filter);
		PreparedStatement stmt = db.prepareStatement(session, "SELECT " + select + " FROM " + name + filter.toSQLString());
		filter.assignParams(stmt, 1);
		stmt.closeOnCompletion();
		return stmt;
	}
	
	public ResultSet query(IDatabaseSession session, QueryFilter filter)
			throws SQLException
	{
		PreparedStatement stmt = prepQuery(session, filter);
		
		try
		{
			return stmt.executeQuery();
		} catch(Throwable e)
		{
			stmt.close();
			throw e;
		}
	}
	
	public long count(IDatabaseSession session, QueryFilter filter)
			throws SQLException
	{
		try(PreparedStatement stmt = db.prepareStatement(session, "SELECT COUNT(*) AS count_long FROM (SELECT 1 FROM " + name + filter.toSQLString() + ") AS sub"))
		{
			filter.assignParams(stmt, 1);
			try(ResultSet res = stmt.executeQuery())
			{
				if(res.next())
					return res.getLong("count_long");
			}
			return 0L;
		}
	}
	
	public int delete(IDatabaseSession session, QueryFilter filter)
			throws SQLException
	{
		PreparedStatement stmt = db.prepareStatement(session, "DELETE FROM " + name + filter.toSQLString());
		filter.assignParams(stmt, 1);
		stmt.closeOnCompletion();
		return stmt.executeUpdate();
	}
	
	public <T> int updateSet(IDatabaseSession session, TableRow<T> row, T data, QueryFilter filter)
			throws SQLException
	{
		try(PreparedStatement stmt = db.prepareStatement(session, "UPDATE " + name + " SET " + row.name + " = ?" + filter.toSQLString()))
		{
			row.set(stmt, 1, data);
			filter.assignParams(stmt, 2);
			return stmt.executeUpdate();
		}
	}
	
	@SuppressWarnings({"unchecked", "rawtypes"})
	public int updateSet(IDatabaseSession session, InsertMap map, QueryFilter filter)
			throws SQLException
	{
		List<TableRow<?>> rowsToUpdate = map.keySet().stream().toList();
		String sets = rowsToUpdate.stream().map(row -> row.name + " = ?").collect(Collectors.joining(", "));
		
		try(PreparedStatement stmt = db.prepareStatement(session, "UPDATE " + name + " SET " + sets + filter.toSQLString()))
		{
			for(int i = 0; i < rowsToUpdate.size(); ++i)
			{
				TableRow row = rowsToUpdate.get(i);
				row.set(stmt, 1 + i, map.get(row));
			}
			
			filter.assignParams(stmt, rowsToUpdate.size() + 1);
			return stmt.executeUpdate();
		}
	}
	
	public QueryIterator queryEntries(IDatabaseSession session, QueryFilter filter)
			throws SQLException
	{
		return entries(query(session, filter));
	}
	
	public QueryIterator entries(ResultSet set)
			throws SQLException
	{
		ResultSetMetaData meta = set.getMetaData();
		int cc = meta.getColumnCount();
		TableRow<?>[] rowsIndexed = new TableRow<?>[cc];
		for(int i = 0; i < cc; ++i)
			rowsIndexed[i] = this.rowsByName.get(meta.getColumnName(1 + i)); // +1 because SQL moment.
		return new QueryIterator(set, rowsIndexed);
	}
	
	public Map<String, TableRow<?>> getRowsByName()
	{
		return rowsByNameUnmod;
	}
	
	public final String getName()
	{
		return name;
	}
	
	public AbstractDatabase getDatabase()
	{
		return db;
	}
	
	void setDb(AbstractDatabase db)
	{
		this.db = db;
	}
	
	private String buildSelectList(QueryFilter filter)
	{
		var excl = filter.getExcluded();
		
		// if no exclusions, don't do streams -> SELECT *
		if(excl.isEmpty())
			return "*";
		
		List<String> cols = rows
				.stream() // all our rows
				.filter(r -> !excl.contains(r)) // that are not excluded
				.map(r -> r.name) // collecting their name
				.toList();
		
		// if nothing was excluded -> still need SELECT *
		if(cols.size() == rows.size())
			return "*";
		
		return String.join(", ", cols);
	}
}