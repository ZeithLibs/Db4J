package dev.zeith.db4j.nio;

import java.nio.file.attribute.*;
import java.time.Instant;
import java.util.UUID;

record DatabaseFileAttributes(UUID uid, boolean directory, long size, Instant created, Instant modified)
		implements BasicFileAttributes
{
	@Override
	public FileTime lastModifiedTime()
	{
		return FileTime.from(modified);
	}
	
	@Override
	public FileTime lastAccessTime()
	{
		return FileTime.from(modified);
	}
	
	@Override
	public FileTime creationTime()
	{
		return FileTime.from(created);
	}
	
	@Override
	public boolean isRegularFile()
	{
		return !directory;
	}
	
	@Override
	public boolean isDirectory()
	{
		return directory;
	}
	
	@Override
	public boolean isSymbolicLink()
	{
		return false;
	}
	
	@Override
	public boolean isOther()
	{
		return false;
	}
	
	@Override
	public Object fileKey()
	{
		return uid;
	}
}