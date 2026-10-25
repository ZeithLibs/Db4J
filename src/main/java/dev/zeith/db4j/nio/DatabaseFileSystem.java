package dev.zeith.db4j.nio;

import java.nio.file.*;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.util.*;
import java.util.regex.Pattern;

public final class DatabaseFileSystem
		extends FileSystem
{
	final DatabaseFileSystemProvider provider;
	final String key;
	final FileTableSimple table;
	private final boolean readOnly;
	private volatile boolean open = true;
	private final DatabaseFileStore store;
	
	DatabaseFileSystem(DatabaseFileSystemProvider provider, String key, FileTableSimple table, boolean readOnly)
	{
		this.provider = provider;
		this.key = key;
		this.table = table;
		this.readOnly = readOnly;
		this.store = new DatabaseFileStore(this);
	}
	
	void ensureOpen()
	{
		if(!open)
			throw new ClosedFileSystemException();
	}
	
	boolean readOnly()
	{
		return readOnly;
	}
	
	@Override
	public DatabaseFileSystemProvider provider()
	{
		return provider;
	}
	
	@Override
	public void close()
	{
		if(open)
		{
			open = false;
			provider.remove(key, this);
		}
	}
	
	@Override
	public boolean isOpen()
	{
		return open;
	}
	
	@Override
	public boolean isReadOnly()
	{
		return readOnly;
	}
	
	@Override
	public String getSeparator()
	{
		return "/";
	}
	
	@Override
	public Iterable<Path> getRootDirectories()
	{
		ensureOpen();
		return List.of(getPath("/"));
	}
	
	@Override
	public Iterable<FileStore> getFileStores()
	{
		ensureOpen();
		return List.of(store);
	}
	
	@Override
	public Set<String> supportedFileAttributeViews()
	{
		return Set.of("basic");
	}
	
	@Override
	public Path getPath(String first, String... more)
	{
		ensureOpen();
		StringBuilder s = new StringBuilder(first);
		for(String part : more)
		{
			if(s.length() > 0 && s.charAt(s.length() - 1) != '/') s.append('/');
			s.append(part);
		}
		return new DatabasePath(this, s.toString());
	}
	
	@Override
	public PathMatcher getPathMatcher(String syntaxAndPattern)
	{
		int i = syntaxAndPattern.indexOf(':');
		if(i < 1) throw new IllegalArgumentException(syntaxAndPattern);
		
		String syntax = syntaxAndPattern.substring(0, i), pattern = syntaxAndPattern.substring(i + 1);
		
		final Pattern p = switch(syntax)
		{
			case "regex" -> Pattern.compile(pattern);
			case "glob" -> Pattern.compile(globToRegex(pattern));
			default -> throw new UnsupportedOperationException("Syntax: " + syntax);
		};
		
		return path -> p.matcher(path.toString()).matches();
	}
	
	private static String globToRegex(String glob)
	{
		StringBuilder b = new StringBuilder("^");
		for(int i = 0; i < glob.length(); i++)
		{
			char c = glob.charAt(i);
			switch(c)
			{
				case '*' -> b.append("[^/]*");
				case '?' -> b.append("[^/]");
				case '.' -> b.append("\\.");
				case '{' -> b.append("(");
				case '}' -> b.append(")");
				case ',' -> b.append('|');
				case '\\' ->
				{
					if(++i < glob.length())
						b.append('\\').append(glob.charAt(i));
				}
				default ->
				{
					if("+$^()[]|".indexOf(c) >= 0)
						b.append('\\'); b.append(c);
				}
			}
		} return b.append('$').toString();
	}
	
	@Override
	public UserPrincipalLookupService getUserPrincipalLookupService()
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public WatchService newWatchService()
	{
		throw new UnsupportedOperationException("Database filesystem does not support watch services");
	}
}