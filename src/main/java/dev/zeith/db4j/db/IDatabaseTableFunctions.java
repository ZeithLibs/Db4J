package dev.zeith.db4j.db;

import dev.zeith.db4j.IDatabaseSession;
import dev.zeith.db4j.query.*;
import dev.zeith.db4j.rows.TableRow;

import java.sql.*;
import java.util.*;

public interface IDatabaseTableFunctions
{
	QueryIterator entries(ResultSet set)
			throws SQLException;
	
	void insert(IDatabaseSession session, Map<TableRow<?>, ?> data)
			throws SQLException;
	
	void batchInsert(IDatabaseSession session, List<InsertMap> data)
			throws SQLException;
	
	ResultSet query(IDatabaseSession session, QueryFilter filter)
			throws SQLException;
	
	long count(IDatabaseSession session, QueryFilter filter)
			throws SQLException;
	
	int delete(IDatabaseSession session, QueryFilter filter)
			throws SQLException;
	
	<T> int updateSet(IDatabaseSession session, TableRow<T> row, T data, QueryFilter filter)
			throws SQLException;
	
	int updateSet(IDatabaseSession session, InsertMap map, QueryFilter filter)
			throws SQLException;
	
	default boolean anyMatch(IDatabaseSession session, QueryFilter filter)
			throws SQLException
	{
		return count(session, filter) > 0L;
	}
	
	default boolean noneMatch(IDatabaseSession session, QueryFilter filter)
			throws SQLException
	{
		return count(session, filter) < 1L;
	}
	
	default QueryIterator queryEntries(IDatabaseSession session, QueryFilter filter)
			throws SQLException
	{
		return entries(query(session, filter));
	}
	
	default void insert(Map<TableRow<?>, ?> data)
			throws SQLException
	{
		insert(null, data);
	}
	
	default void batchInsert(List<InsertMap> data)
			throws SQLException
	{
		batchInsert(null, data);
	}
	
	default ResultSet query(QueryFilter filter)
			throws SQLException
	{
		return query(null, filter);
	}
	
	default long count(QueryFilter filter)
			throws SQLException
	{
		return count(null, filter);
	}
	
	default int delete(QueryFilter filter)
			throws SQLException
	{
		return delete(null, filter);
	}
	
	default <T> int updateSet(TableRow<T> row, T data, QueryFilter filter)
			throws SQLException
	{
		return updateSet(null, row, data, filter);
	}
	
	default int updateSet(InsertMap map, QueryFilter filter)
			throws SQLException
	{
		return updateSet((IDatabaseSession) null, map, filter);
	}
	
	default boolean anyMatch(QueryFilter filter)
			throws SQLException
	{
		return count(filter) > 0L;
	}
	
	default boolean noneMatch(QueryFilter filter)
			throws SQLException
	{
		return count(filter) < 1L;
	}
	
	default QueryIterator queryEntries(QueryFilter filter)
			throws SQLException
	{
		return entries(query(filter));
	}
}