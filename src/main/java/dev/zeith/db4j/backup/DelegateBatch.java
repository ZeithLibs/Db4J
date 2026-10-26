package dev.zeith.db4j.backup;

import lombok.*;

import java.util.function.Function;

@ToString
@RequiredArgsConstructor
public class DelegateBatch<E extends Exception>
		implements IBackupReceptor.Batch<E>
{
	protected final IBackupReceptor.Batch<E> delegate;
	
	@Override
	public void addBatch()
			throws E
	{
		this.delegate.addBatch();
	}
	
	@Override
	public void setObject(int i, Object object)
			throws E
	{
		this.delegate.setObject(i, object);
	}
	
	@Override
	public void executeBatch()
			throws E
	{
		this.delegate.executeBatch();
	}
	
	@Override
	public void close()
			throws E
	{
		this.delegate.close();
	}
	
	@Override
	public IBackupReceptor.Batch<E> andThen(IBackupReceptor.Batch<E> next)
	{
		return this.delegate.andThen(next);
	}
	
	@Override
	public <TE extends Exception> IBackupReceptor.Batch<TE> wrapException(Class<E> our, Function<E, TE> wrapper)
	{
		return this.delegate.wrapException(our, wrapper);
	}
}
