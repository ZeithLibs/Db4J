package dev.zeith.db4j.util;

import lombok.SneakyThrows;

import java.sql.SQLException;
import java.util.*;
import java.util.function.*;

public class SqlIterators
{
	private static final SqlIterator<?> EMPTY = new SqlIterator<>()
	{
		@Override
		public void close()
		{
		}
		
		@Override
		public boolean hasNext()
		{
			return false;
		}
		
		@Override
		public Object next()
		{
			throw new NoSuchElementException();
		}
	};
	
	public static <T> SqlIterator<T> empty()
	{
		return (SqlIterator<T>) EMPTY;
	}
	
	public static <K> SqlIterator<K> peek(SqlIterator<K> iter, Consumer<K> handler)
	{
		return new SqlIterator<>()
		{
			@Override
			public void close()
					throws SQLException
			{
				iter.close();
			}
			
			@Override
			public boolean hasNext()
			{
				return iter.hasNext();
			}
			
			@Override
			public K next()
			{
				var v = iter.next();
				handler.accept(v);
				return v;
			}
		};
	}
	
	public static <K> SqlIterator<K> concat(SqlIterator<K> iter, Collection<SqlIterator<K>> sqlIterators)
	{
		if(sqlIterators == null || sqlIterators.isEmpty()) return iter;
		return new SqlIterator<>()
		{
			@Override
			public void close()
					throws SQLException
			{
				iter.close();
			}
			
			final List<SqlIterator<K>> seq = new LinkedList<>(sqlIterators);
			SqlIterator<K> cur = iter;
			
			private SqlIterator<K> cursor()
			{
				if(!cur.hasNext() && !seq.isEmpty()) cur = seq.remove(0);
				return cur;
			}
			
			@Override
			public boolean hasNext()
			{
				return cursor().hasNext();
			}
			
			@Override
			public K next()
			{
				return cursor().next();
			}
		};
	}
	
	public static <K> SqlIterator<K> filter(SqlIterator<K> iter, Predicate<K> filter)
	{
		record Result<K>(K k, boolean found)
		{
			static final Result<?> NOT_FOUND = new Result<>(null, false);
			
			@SuppressWarnings("unchecked")
			static <V> Result<V> notFound()
			{
				return (Result<V>) NOT_FOUND;
			}
		}
		
		return new SqlIterator<K>()
		{
			Result<K> value = find();
			
			@Override
			public void close()
					throws SQLException
			{
				iter.close();
			}
			
			Result<K> find()
			{
				while(iter.hasNext())
				{
					K found;
					try
					{
						found = iter.next();
					} catch(NoSuchElementException e)
					{
						return Result.notFound();
					}
					if(filter.test(found))
						return new Result<>(found, true);
				}
				return Result.notFound();
			}
			
			@Override
			public boolean hasNext()
			{
				return value.found();
			}
			
			@Override
			public K next()
			{
				var res = value.k();
				value = find();
				return res;
			}
		};
	}
	
	public static <K, V> SqlIterator<V> map(SqlIterator<K> iter, Function<K, V> mapper)
	{
		return new SqlIterator<>()
		{
			@Override
			public void close()
					throws SQLException
			{
				iter.close();
			}
			
			@Override
			public boolean hasNext()
			{
				return iter.hasNext();
			}
			
			@Override
			public V next()
			{
				return mapper.apply(iter.next());
			}
		};
	}
	
	public static <K, V> SqlIterator<V> sqlMap(SqlIterator<K> iter, ISQLFunction<K, V> mapper)
	{
		return new SqlIterator<>()
		{
			@Override
			public void close()
					throws SQLException
			{
				iter.close();
			}
			
			@Override
			public boolean hasNext()
			{
				return iter.hasNext();
			}
			
			@Override
			@SneakyThrows
			public V next()
			{
				return mapper.apply(iter.next());
			}
		};
	}
	
	public static <V> SqlIterator<V> unmap(SqlIterator<SqlIterator<V>> mapped)
	{
		return new SqlIterator<>()
		{
			SqlIterator<V> iter;
			
			@Override
			public void close()
					throws SQLException
			{
				if(iter != null) iter.close();
				mapped.close();
			}
			
			@Override
			public boolean hasNext()
			{
				return (iter != null && iter.hasNext()) || mapped.hasNext();
			}
			
			@Override
			@SneakyThrows
			public V next()
			{
				if(iter == null || !iter.hasNext())
					iter = mapped.next();
				return iter.next();
			}
		};
	}
	
	public static <T> List<T> toList(SqlIterator<T> itr)
	{
		return toList(itr, ArrayList::new);
	}
	
	public static <T> List<T> toList(SqlIterator<T> itr, Supplier<List<T>> lstFactory)
	{
		var lst = lstFactory.get();
		while(itr.hasNext()) lst.add(itr.next());
		return lst;
	}
}