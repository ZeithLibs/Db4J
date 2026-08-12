package dev.zeith.db4j.util;

import java.sql.SQLException;
import java.util.*;

public class SQLOptional<T>
{
	private static final SQLOptional<?> EMPTY = new SQLOptional<>();
	
	private final T value;
	
	private SQLOptional()
	{
		this.value = null;
	}
	
	public static <T> SQLOptional<T> empty()
	{
		@SuppressWarnings("unchecked")
		SQLOptional<T> t = (SQLOptional<T>) EMPTY;
		return t;
	}
	
	private SQLOptional(T value)
	{
		this.value = Objects.requireNonNull(value);
	}
	
	public static <T> SQLOptional<T> withSQL(Optional<T> value)
	{
		return new SQLOptional<>(value.orElse(null));
	}
	
	public static <T> SQLOptional<T> of(T value)
	{
		return new SQLOptional<>(value);
	}
	
	public static <T> SQLOptional<T> ofNullable(T value)
	{
		return value == null ? empty() : of(value);
	}
	
	public T get()
	{
		if(value == null)
		{
			throw new NoSuchElementException("No value present");
		}
		return value;
	}
	
	public boolean isPresent()
	{
		return value != null;
	}
	
	public void ifPresent(ISQLConsumer<? super T> consumer) throws SQLException
	{
		if(value != null)
			consumer.accept(value);
	}
	
	public SQLOptional<T> filter(ISQLPredicate<? super T> predicate) throws SQLException
	{
		Objects.requireNonNull(predicate);
		if(!isPresent())
			return this;
		else
			return predicate.test(value) ? this : empty();
	}
	
	public <U> SQLOptional<U> map(ISQLFunction<? super T, ? extends U> mapper) throws SQLException
	{
		Objects.requireNonNull(mapper);
		if(!isPresent())
			return empty();
		else
		{
			return SQLOptional.ofNullable(mapper.apply(value));
		}
	}
	
	public <U> SQLOptional<U> flatMapSQL(ISQLFunction<? super T, SQLOptional<U>> mapper) throws SQLException
	{
		Objects.requireNonNull(mapper);
		if(!isPresent())
			return empty();
		else
		{
			return Objects.requireNonNull(mapper.apply(value));
		}
	}
	
	public <U> SQLOptional<U> flatMap(ISQLFunction<? super T, Optional<U>> mapper) throws SQLException
	{
		Objects.requireNonNull(mapper);
		if(!isPresent())
			return empty();
		else
		{
			Optional<U> opt = Objects.requireNonNull(mapper.apply(value));
			return opt.isPresent() ? of(opt.orElse(null)) : empty();
		}
	}
	
	public T orElse(T other)
	{
		return value != null ? value : other;
	}
	
	public T orElseGet(ISQLSupplier<? extends T> other) throws SQLException
	{
		return value != null ? value : other.get();
	}
	
	public Optional<T> noSQL()
	{
		return Optional.ofNullable(value);
	}
	
	public <X extends Throwable> T orElseThrow(ISQLSupplier<? extends X> exceptionSupplier) throws X, SQLException
	{
		if(value != null)
		{
			return value;
		} else
		{
			throw exceptionSupplier.get();
		}
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if(this == obj)
		{
			return true;
		}
		
		if(!(obj instanceof SQLOptional<?> other))
		{
			return false;
		}
		
		return Objects.equals(value, other.value);
	}
	
	@Override
	public int hashCode()
	{
		return Objects.hashCode(value);
	}
	
	@Override
	public String toString()
	{
		return value != null ? String.format("SQLOptional[%s]", value) : "SQLOptional.empty";
	}
}