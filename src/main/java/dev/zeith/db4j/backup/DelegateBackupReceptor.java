package dev.zeith.db4j.backup;

import lombok.*;

import java.util.LinkedHashSet;

@ToString
@RequiredArgsConstructor
public class DelegateBackupReceptor<E extends Exception>
		implements IBackupReceptor<E>
{
	protected final IBackupReceptor<E> delegate;
	
	@Override
	public void dropTables()
			throws E
	{
		this.delegate.dropTables();
	}
	
	@Override
	public void setTables(LinkedHashSet<String> copiedTables)
			throws E
	{
		this.delegate.setTables(copiedTables);
	}
	
	@Override
	public void useSqlCommand(String originCatalog, String sql)
			throws E
	{
		this.delegate.useSqlCommand(originCatalog, sql);
	}
	
	@Override
	public void sqlCommand(String sql)
			throws E
	{
		this.delegate.sqlCommand(sql);
	}
	
	@Override
	public Batch<E> insertSql(String sql)
			throws E
	{
		return this.delegate.insertSql(sql);
	}
	
	@Override
	public void close()
			throws E
	{
		this.delegate.close();
	}
}