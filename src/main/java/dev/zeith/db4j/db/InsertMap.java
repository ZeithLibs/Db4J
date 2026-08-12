package dev.zeith.db4j.db;

import dev.zeith.db4j.data.RowConstraints;
import dev.zeith.db4j.rows.TableRow;

import java.util.*;

public class InsertMap
		extends HashMap<TableRow<?>, Object>
{
	public InsertMap(int size)
	{
		super(size);
	}
	
	public InsertMap()
	{
	}
	
	public <T> void insert(TableRow<T> row, T data)
	{
		if(data == null && row.constraints.contains(RowConstraints.NOT_NULL))
			throw new NullPointerException("Null row insertion: " + row.name);
		super.put(row, data);
	}
	
	public void insertNULL(TableRow<?> row)
	{
		insert(row, null);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T get(TableRow<T> key)
	{
		return (T) super.get(key);
	}
	
	@Override
	public Object put(TableRow<?> key, Object value)
	{
		throw new UnsupportedOperationException("put");
	}
	
	@Override
	public void putAll(Map<? extends TableRow<?>, ? extends Object> m)
	{
		throw new UnsupportedOperationException("put");
	}
	
	@Override
	public Object putIfAbsent(TableRow<?> key, Object value)
	{
		throw new UnsupportedOperationException("put");
	}
}
