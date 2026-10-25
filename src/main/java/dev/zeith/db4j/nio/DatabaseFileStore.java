package dev.zeith.db4j.nio;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.attribute.*;

final class DatabaseFileStore
		extends FileStore
{
	private final DatabaseFileSystem fs;
	
	DatabaseFileStore(DatabaseFileSystem fs)
	{
		this.fs = fs;
	}
	
	@Override
	public String name()
	{
		return "DB4J:" + fs.key;
	}
	
	@Override
	public String type()
	{
		return "db4j";
	}
	
	@Override
	public boolean isReadOnly()
	{
		return fs.isReadOnly();
	}
	
	@Override
	public long getTotalSpace()
	{
		return Long.MAX_VALUE;
	}
	
	@Override
	public long getUsableSpace()
	{
		return Long.MAX_VALUE;
	}
	
	@Override
	public long getUnallocatedSpace()
	{
		return Long.MAX_VALUE;
	}
	
	@Override
	public boolean supportsFileAttributeView(Class<? extends FileAttributeView> type)
	{
		return type == BasicFileAttributeView.class;
	}
	
	@Override
	public boolean supportsFileAttributeView(String name)
	{
		return "basic".equals(name);
	}
	
	@Override
	public <V extends FileStoreAttributeView> V getFileStoreAttributeView(Class<V> type)
	{
		return null;
	}
	
	@Override
	public Object getAttribute(String attribute)
			throws IOException
	{
		if("totalSpace".equals(attribute) || "usableSpace".equals(attribute) || "unallocatedSpace".equals(attribute))
			return Long.MAX_VALUE;
		throw new UnsupportedOperationException(attribute);
	}
}