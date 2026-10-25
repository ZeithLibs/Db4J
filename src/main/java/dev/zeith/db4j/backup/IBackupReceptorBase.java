package dev.zeith.db4j.backup;

import java.util.LinkedHashSet;

interface IBackupReceptorBase<E extends Exception>
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
	
	IBackupReceptor.Batch<E> insertSql(String sql)
			throws E;
	
	@Override
	void close()
			throws E;
}
