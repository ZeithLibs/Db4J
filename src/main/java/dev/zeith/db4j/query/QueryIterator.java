package dev.zeith.db4j.query;

import dev.zeith.db4j.rows.TableRow;
import dev.zeith.db4j.util.*;

import java.sql.*;
import java.util.NoSuchElementException;

public class QueryIterator
		implements CloseableIterator<QueryEntry>
{
	private ResultSet set;
	private boolean hasNext;
	private TableRow<?>[] rowsIndexed;
	
	public QueryIterator(ResultSet set, TableRow<?>[] rowsIndexed)
			throws SQLException
	{
		this.set = set;
		this.rowsIndexed = rowsIndexed;
		hasNext = this.set.next();
	}
	
	@Override
	public void close()
			throws SQLException
	{
		if(!set.isClosed())
		{
			set.close();
		}
	}
	
	@Override
	public QueryEntry next()
	{
		try
		{
			if(!hasNext || set.isClosed())
				throw new NoSuchElementException("End of queries.");
			
			QueryEntry qe = QueryEntry.fromResult(set, rowsIndexed);
			hasNext = set.next();
			if(!hasNext)
				set.close();
			return qe;
		} catch(SQLException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public boolean hasNext()
	{
		return hasNext;
	}
	
	public SQLOptional<QueryEntry> fetchFirstAndClose()
			throws SQLException
	{
		try(QueryIterator qe = this)
		{
			if(!qe.hasNext())
				return SQLOptional.empty();
			return SQLOptional.ofNullable(qe.next());
		}
	}
}
