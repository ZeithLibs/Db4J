package dev.zeith.db4j.backup;

import lombok.*;
import lombok.experimental.Delegate;

@ToString
@RequiredArgsConstructor
public class DelegateBackupReceptor<E extends Exception>
		implements IBackupReceptor<E>
{
	@Delegate(types = IBackupReceptorBase.class)
	protected final IBackupReceptor<E> delegate;
}