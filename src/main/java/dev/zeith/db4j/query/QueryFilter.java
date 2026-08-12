package dev.zeith.db4j.query;

import dev.zeith.db4j.data.*;
import dev.zeith.db4j.db.DatabaseTable;
import dev.zeith.db4j.rows.TableRow;
import dev.zeith.db4j.util.SQLHelper;

import java.sql.*;
import java.util.*;

public class QueryFilter
{
	private final DatabaseTable owner;
	private final List<ParamPair<?>> params = new ArrayList<>();
	private String query;
	
	private final List<OrderByEntry> orderBy = new ArrayList<>();
	
	final List<TableRow<?>> excluded = new ArrayList<>();
	
	private Long offsetRows;
	private Long limit;
	private Long offset;
	private Long fetchNext;
	
	public QueryFilter(DatabaseTable owner)
	{
		this.owner = owner;
		this.query = "";
	}
	
	protected QueryFilter(QueryFilter copyOf)
	{
		this.owner = copyOf.owner;
		this.params.addAll(copyOf.params);
		this.query = copyOf.query;
		this.orderBy.addAll(copyOf.orderBy);
		this.offsetRows = copyOf.offsetRows;
		this.limit = copyOf.limit;
		this.offset = copyOf.offset;
		this.fetchNext = copyOf.fetchNext;
		this.excluded.addAll(copyOf.excluded);
	}
	
	public List<TableRow<?>> getExcluded()
	{
		return Collections.unmodifiableList(excluded);
	}
	
	public QueryFilter exclude(TableRow<?> row)
	{
		QueryFilter qf = copy();
		qf.excluded.add(row);
		return qf;
	}
	
	public QueryFilter excludeAll(List<TableRow<?>> rows)
	{
		QueryFilter qf = copy();
		qf.excluded.addAll(rows);
		return qf;
	}
	
	public String toSQLString()
	{
		StringBuilder sb = new StringBuilder();
		
		if(!query.isEmpty())
			sb.append(" WHERE ").append(query);
		
		if(!orderBy.isEmpty())
			sb.append(" ORDER BY ").append(String.join(", ", orderBy.stream().map(OrderByEntry::toString).toList()));
		if(limit != null)
			sb.append(" LIMIT ").append(Long.toUnsignedString(limit));
		if(offsetRows != null)
			sb.append(" OFFSET ").append(Long.toUnsignedString(offsetRows)).append(" ROWS");
		if(offset != null)
		{
			if(limit == null)
				sb.append(" LIMIT 18446744073709551615"); // unsigned long max value
			sb.append(" OFFSET ").append(Long.toUnsignedString(offset));
		}
		if(fetchNext != null)
			sb.append(" FETCH NEXT ").append(Long.toUnsignedString(fetchNext)).append(" ROWS ONLY");
		
		return sb.toString();
	}
	
	public String toUnsafeSQLString()
	{
		String str = toSQLString();
		
		for(ParamPair<?> par : params)
			str = str.replaceFirst("[?]", "'" + par.data() + "'");
		
		return str;
	}
	
	public QueryFilter copy()
	{
		return new QueryFilter(this);
	}
	
	public QueryFilter merge(QueryFilter group, LogicMode mode)
	{
		Objects.requireNonNull(group, "group cannot be null");
		Objects.requireNonNull(mode, "mode cannot be null");
		
		if(params.isEmpty())
		{
			if(group.params.isEmpty())
				return new QueryFilter(owner);
			return group.copy();
		}
		
		QueryFilter res = copy();
		
		if(this.query.isEmpty())
			res.query = group.query;
		else if(group.query.isEmpty())
			res.query = this.query;
		else
			res.query = "(" + this.query + ") " + mode + " (" + group.query + ")";
		
		res.params.addAll(group.params);
		return res;
	}
	
	public QueryFilter and(QueryFilter group)
	{
		return merge(group, LogicMode.AND);
	}
	
	public QueryFilter or(QueryFilter group)
	{
		return merge(group, LogicMode.OR);
	}
	
	public void assignParams(PreparedStatement stmt, int shift)
			throws SQLException
	{
		for(int i = 0; i < params.size(); ++i)
			params.get(i).set(stmt, shift + i);
	}
	
	public QueryFilter orderBy(TableRow<?> order)
	{
		return orderBy(order, false);
	}
	
	public QueryFilter orderBy(TableRow<?> order, boolean descendingOrder)
	{
		return setOrderBy(new OrderByEntry(order, descendingOrder));
	}
	
	public QueryFilter setOrderBy(OrderByEntry... order)
	{
		QueryFilter qf = copy();
		qf.orderBy.clear();
		var rbn = owner.getRowsByName();
		for(OrderByEntry o : order)
		{
			if(!rbn.containsKey(o.row().name))
				throw new IllegalArgumentException("Tried ordering query by unknown column: " + o.row().name);
			qf.orderBy.add(o);
		}
		
		return qf;
	}
	
	public QueryFilter addOrderBy(OrderByEntry... order)
	{
		QueryFilter qf = copy();
		var rbn = owner.getRowsByName();
		for(OrderByEntry o : order)
		{
			if(!rbn.containsKey(o.row().name))
				throw new IllegalArgumentException("Tried ordering query by unknown column: " + o.row().name);
			qf.orderBy.add(o);
		}
		return qf;
	}
	
	public QueryFilter offsetRows(long offsetRows)
	{
		QueryFilter qf = copy();
		qf.offsetRows = offsetRows;
		return qf;
	}
	
	public QueryFilter offset(long offset)
	{
		QueryFilter qf = copy();
		qf.offset = offset;
		return qf;
	}
	
	public QueryFilter limit(long limit)
	{
		QueryFilter qf = copy();
		qf.limit = limit;
		return qf;
	}
	
	public QueryFilter fetchNext(long fetchNext)
	{
		QueryFilter qf = copy();
		qf.fetchNext = fetchNext;
		return qf;
	}
	
	public <DATA> QueryFilter whereAND(TableRow<DATA> row, DATA data)
	{
		return whereAND(row, data, ComparisonMode.EXACTLY);
	}
	
	public <DATA> QueryFilter whereOR(TableRow<DATA> row, DATA data)
	{
		return whereOR(row, data, ComparisonMode.EXACTLY);
	}
	
	public <DATA> QueryFilter whereAND(TableRow<DATA> row, DATA data, ComparisonMode mode)
	{
		return where(row, data, LogicMode.AND, mode);
	}
	
	public <DATA> QueryFilter whereOR(TableRow<DATA> row, DATA data, ComparisonMode mode)
	{
		return where(row, data, LogicMode.OR, mode);
	}
	
	public <DATA> QueryFilter where(TableRow<DATA> row, DATA data, LogicMode logic, ComparisonMode compare)
	{
		QueryFilter qf = copy();
		qf.query = query + (query.isEmpty() ? "" : " " + logic.name() + " ") + row.name + " " + compare.sql + " ?";
		qf.params.add(new ParamPair<>(row, data));
		return qf;
	}
	
	public QueryFilter whereIsNull(TableRow<?> row, LogicMode logic)
	{
		QueryFilter qf = copy();
		qf.query = query + (query.isEmpty() ? "" : " " + logic.name() + " ") + row.name + " IS NULL";
		return qf;
	}
	
	public <DATA> QueryFilter whereLike(TableRow<String> row, String pattern, LogicMode logic, LikeMode likeMode, boolean sanitizeWildcard)
	{
		if(pattern == null)
			throw new IllegalArgumentException("Pattern cannot be null");
		
		StringBuilder sb = new StringBuilder(query);
		sb.append(query.isEmpty() ? "" : " " + logic.name() + " ");
		if(likeMode.caseSensitive)
			sb.append(row.name).append(likeMode.inverted ? " NOT" : "").append(" LIKE ?");
		else
			sb.append("LOWER(").append(row.name).append(")").append(likeMode.inverted ? " NOT" : "").append(" LIKE LOWER(?)");
		if(sanitizeWildcard)
		{
			sb.append(" ESCAPE '\\\\'");
			pattern = SQLHelper.escapeSqlLikePattern(pattern);
		}
		QueryFilter qf = copy();
		qf.query = sb.toString();
		qf.params.add(new ParamPair<>(row, pattern));
		return qf;
	}
	
	public QueryFilter whereLikeAny(List<TableRow<String>> columns, String pattern, LogicMode outerLogic, LikeMode likeMode, boolean sanitizeWildcard)
	{
		if(columns == null || columns.isEmpty())
			throw new IllegalArgumentException("No columns provided for multi-column LIKE");
		
		if(pattern == null)
			throw new IllegalArgumentException("Pattern cannot be null");
		
		StringBuilder sb = new StringBuilder(query);
		sb.append(query.isEmpty() ? "(" : " " + outerLogic.name() + " (");
		
		boolean first = true;
		for(TableRow<String> col : columns)
		{
			if(!first)
				sb.append(" OR ");
			first = false;
			
			if(likeMode.caseSensitive)
				sb.append(col.name).append(likeMode.inverted ? " NOT" : "").append(" LIKE ?");
			else
				sb.append("LOWER(").append(col.name).append(")").append(likeMode.inverted ? " NOT" : "").append(" LIKE LOWER(?)");
		}
		
		sb.append(")");
		
		if(sanitizeWildcard)
		{
			sb.append(" ESCAPE '\\\\'");
			pattern = SQLHelper.escapeSqlLikePattern(pattern);
		}
		
		QueryFilter qf = copy();
		qf.query = sb.toString();
		
		for(TableRow<String> column : columns)
			qf.params.add(new ParamPair<>(column, pattern));
		
		return qf;
	}
}