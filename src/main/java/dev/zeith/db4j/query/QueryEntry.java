package dev.zeith.db4j.query;

import dev.zeith.db4j.db.InsertMap;
import dev.zeith.db4j.rows.TableRow;

import java.sql.*;
import java.util.*;
import java.util.Map.Entry;

public class QueryEntry
{
	protected final Map<TableRow<?>, Object> rows;
	protected final Map<String, Object> rowsByName;
	
	public QueryEntry(Map<TableRow<?>, Object> rows)
	{
		this.rows = rows;
		
		Map<String, Object> rowsByName = new HashMap<>();
		for(Entry<TableRow<?>, Object> ent : rows.entrySet())
			rowsByName.put(ent.getKey().name, ent.getValue());
		this.rowsByName = Collections.unmodifiableMap(rowsByName);
	}
	
	public boolean has(TableRow<?> row)
	{
		return rows.containsKey(row);
	}
	
	@SuppressWarnings("unchecked")
	public <T> Optional<T> get(TableRow<T> row)
	{
		if(rows.containsKey(row))
			return Optional.ofNullable((T) rows.get(row));
		return Optional.empty();
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public InsertMap toInsertMap()
	{
		InsertMap imap = new InsertMap();
		rows.forEach((row, data) -> imap.insert((TableRow) row, data));
		return imap;
	}
	
	@Override
	public String toString()
	{
		return "QueryEntry" + rowsByName.toString();
	}
	
	public static QueryEntry fromResult(ResultSet set, TableRow<?>[] rowsIndexed) throws SQLException
	{
		Map<TableRow<?>, Object> rows = new HashMap<>();
		for(int i = 0; i < rowsIndexed.length; ++i)
			rows.put(rowsIndexed[i], rowsIndexed[i].get(set, rowsIndexed[i].name));
		return new QueryEntry(rows);
	}
	
}