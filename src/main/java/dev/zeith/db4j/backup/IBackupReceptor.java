package dev.zeith.db4j.backup;

import java.util.LinkedHashSet;
import java.util.function.Function;

public interface IBackupReceptor<E extends Exception>
		extends AutoCloseable
{
	void dropTables()
			throws E;
	
	void setTables(LinkedHashSet<String> copiedTables)
			throws E;
	
	void useSqlCommand(String originCatalog, String sql)
			throws E;
	
	void sqlCommand(String sql)
			throws E;
	
	Batch<E> insertSql(String sql)
			throws E;
	
	@Override
	void close()
			throws E;
	
	interface Batch<E extends Exception>
			extends AutoCloseable
	{
		void addBatch()
				throws E;
		
		void setObject(int i, Object object)
				throws E;
		
		void executeBatch()
				throws E;
		
		@Override
		void close()
				throws E;
		
		default Batch<E> andThen(Batch<E> next)
		{
			Batch<E> o = this;
			return new Batch<E>()
			{
				@Override
				public void addBatch()
						throws E
				{
					o.addBatch();
					next.addBatch();
				}
				
				@Override
				public void setObject(int i, Object object)
						throws E
				{
					o.setObject(i, object);
					next.setObject(i, object);
				}
				
				@Override
				public void executeBatch()
						throws E
				{
					o.executeBatch();
					next.executeBatch();
				}
				
				@Override
				public void close()
						throws E
				{
					o.close();
					next.close();
				}
			};
		}
		
		default <TE extends Exception> Batch<TE> wrapException(Class<E> our, Function<E, TE> wrapper)
		{
			Batch<E> o = this;
			return new Batch<>()
			{
				private void rethrow(Exception e)
						throws TE
				{
					if(our.isInstance(e))
						throw wrapper.apply(our.cast(e));
					if(e instanceof RuntimeException re)
						throw re;
					throw new RuntimeException(e);
				}
				
				@Override
				public void addBatch()
						throws TE
				{
					try
					{
						o.addBatch();
					} catch(Exception e)
					{
						rethrow(e);
					}
				}
				
				@Override
				public void setObject(int i, Object object)
						throws TE
				{
					try
					{
						o.setObject(i, object);
					} catch(Exception e)
					{
						rethrow(e);
					}
				}
				
				@Override
				public void executeBatch()
						throws TE
				{
					try
					{
						o.executeBatch();
					} catch(Exception e)
					{
						rethrow(e);
					}
				}
				
				@Override
				public void close()
						throws TE
				{
					try
					{
						o.close();
					} catch(Exception e)
					{
						rethrow(e);
					}
				}
			};
		}
	}
	
	default <TE extends Exception> IBackupReceptor<TE> wrapException(Class<E> our, Function<E, TE> wrapper)
	{
		IBackupReceptor<E> o = this;
		return new IBackupReceptor<TE>()
		{
			private void rethrow(Exception e)
					throws TE
			{
				if(our.isInstance(e))
					throw wrapper.apply(our.cast(e));
				if(e instanceof RuntimeException re)
					throw re;
				throw new RuntimeException(e);
			}
			
			@Override
			public void dropTables()
					throws TE
			{
				try
				{
					o.dropTables();
				} catch(Exception e)
				{
					rethrow(e);
				}
			}
			
			@Override
			public void setTables(LinkedHashSet<String> copiedTables)
					throws TE
			{
				try
				{
					o.setTables(copiedTables);
				} catch(Exception e)
				{
					rethrow(e);
				}
			}
			
			@Override
			public void useSqlCommand(String originCatalog, String sql)
					throws TE
			{
				try
				{
					o.useSqlCommand(originCatalog, sql);
				} catch(Exception e)
				{
					rethrow(e);
				}
			}
			
			@Override
			public void sqlCommand(String sql)
					throws TE
			{
				try
				{
					o.sqlCommand(sql);
				} catch(Exception e)
				{
					rethrow(e);
				}
			}
			
			@Override
			public Batch<TE> insertSql(String sql)
					throws TE
			{
				try
				{
					return o.insertSql(sql).wrapException(our, wrapper);
				} catch(Exception e)
				{
					rethrow(e);
					return null; // should never happen.
				}
			}
			
			@Override
			public void close()
					throws TE
			{
				try
				{
					o.close();
				} catch(Exception e)
				{
					rethrow(e);
				}
			}
		};
	}
	
	default IBackupReceptor<E> andThen(IBackupReceptor<E> next)
	{
		IBackupReceptor<E> o = this;
		return new IBackupReceptor<>()
		{
			@Override
			public void dropTables()
					throws E
			{
				o.dropTables();
				next.dropTables();
			}
			
			@Override
			public void setTables(LinkedHashSet<String> copiedTables)
					throws E
			{
				o.setTables(copiedTables);
				next.setTables(copiedTables);
			}
			
			@Override
			public void useSqlCommand(String originCatalog, String sql)
					throws E
			{
				o.useSqlCommand(originCatalog, sql);
				next.useSqlCommand(originCatalog, sql);
			}
			
			@Override
			public void sqlCommand(String sql)
					throws E
			{
				o.sqlCommand(sql);
				next.sqlCommand(sql);
			}
			
			@Override
			public Batch<E> insertSql(String sql)
					throws E
			{
				return o.insertSql(sql).andThen(next.insertSql(sql));
			}
			
			@Override
			public void close()
					throws E
			{
				o.close();
				next.close();
			}
		};
	}
}