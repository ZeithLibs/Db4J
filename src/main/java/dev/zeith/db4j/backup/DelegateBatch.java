package dev.zeith.db4j.backup;

import lombok.*;
import lombok.experimental.Delegate;

@ToString
@RequiredArgsConstructor
public
class DelegateBatch<E extends Exception>
		implements IBackupReceptor.Batch<E>
{
	@Delegate
	protected final IBackupReceptor.Batch<E> delegate;
}
