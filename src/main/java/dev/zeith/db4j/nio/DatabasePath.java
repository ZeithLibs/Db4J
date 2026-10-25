package dev.zeith.db4j.nio;

import java.io.File;
import java.net.URI;
import java.nio.file.*;
import java.util.*;

public final class DatabasePath
		implements Path
{
	final DatabaseFileSystem fs;
	private final String value;
	
	DatabasePath(DatabaseFileSystem fs, String input) {this.fs = fs; this.value = normalize(input);}
	
	private static String normalize(String s)
	{
		if(s == null)
			throw new NullPointerException("path");
		s = s.replace('\\', '/');
		boolean abs = s.startsWith("/");
		Deque<String> parts = new ArrayDeque<>();
		for(String p : s.split("/+"))
		{
			if(p.isEmpty() || p.equals("."))
				continue;
			if(p.equals(".."))
			{
				if(!parts.isEmpty() && !parts.peekLast().equals(".."))
					parts.removeLast();
				else if(!abs)
					parts.addLast(p);
			} else
				parts.addLast(p);
		}
		String out = String.join("/", parts);
		return (abs ? "/" : "") + out;
	}
	
	String dbPath()
	{
		if(isAbsolute())
			return value.isEmpty() ? "/" : value;
		return normalize("/" + value);
	}
	
	@Override
	public DatabaseFileSystem getFileSystem() {return fs;}
	
	@Override
	public boolean isAbsolute()
	{
		return value.startsWith("/");
	}
	
	@Override
	public Path getRoot()
	{
		return isAbsolute() ? new DatabasePath(fs, "/") : null;
	}
	
	private List<String> parts()
	{
		String s = value.startsWith("/") ? value.substring(1) : value;
		return s.isEmpty() ? new ArrayList<>() : new ArrayList<>(Arrays.asList(s.split("/")));
	}
	
	@Override
	public Path getFileName()
	{
		List<String> p = parts();
		return p.isEmpty() ? null : new DatabasePath(fs, p.get(p.size() - 1));
	}
	
	@Override
	public Path getParent()
	{
		List<String> p = parts();
		if(p.isEmpty())
			return null;
		p.remove(p.size() - 1);
		return new DatabasePath(fs, (isAbsolute() ? "/" : "") + String.join("/", p));
	}
	
	@Override
	public int getNameCount()
	{
		return parts().size();
	}
	
	@Override
	public Path getName(int index)
	{
		return new DatabasePath(fs, parts().get(index));
	}
	
	@Override
	public Path subpath(int begin, int end)
	{
		List<String> p = parts();
		if(begin < 0 || end > p.size() || begin >= end)
			throw new IllegalArgumentException();
		return new DatabasePath(fs, String.join("/", p.subList(begin, end)));
	}
	
	@Override
	public boolean startsWith(Path other)
	{
		if(!(other instanceof DatabasePath p) || p.fs != fs || p.isAbsolute() != isAbsolute())
			return false;
		List<String> a = parts(), b = p.parts();
		return a.size() >= b.size() && a.subList(0, b.size()).equals(b);
	}
	
	@Override
	public boolean startsWith(String other)
	{
		return startsWith(fs.getPath(other));
	}
	
	@Override
	public boolean endsWith(Path other)
	{
		if(!(other instanceof DatabasePath p) || p.fs != fs)
			return false;
		List<String> a = parts(), b = p.parts();
		if(p.isAbsolute() && !isAbsolute())
			return false;
		return a.size() >= b.size() && a.subList(a.size() - b.size(), a.size()).equals(b);
	}
	
	@Override
	public boolean endsWith(String other)
	{
		return endsWith(fs.getPath(other));
	}
	
	@Override
	public Path normalize()
	{
		return new DatabasePath(fs, value);
	}
	
	@Override
	public Path resolve(Path other)
	{
		if(!(other instanceof DatabasePath p) || p.fs != fs)
			throw new ProviderMismatchException();
		return p.isAbsolute() ? p : new DatabasePath(fs, (value.endsWith("/") ? value : value + "/") + p.value);
	}
	
	@Override
	public Path resolve(String other)
	{
		return resolve(fs.getPath(other));
	}
	
	@Override
	public Path resolveSibling(Path other)
	{
		Path p = getParent(); return p == null ? other : p.resolve(other);
	}
	
	@Override
	public Path resolveSibling(String other)
	{
		return resolveSibling(fs.getPath(other));
	}
	
	@Override
	public Path relativize(Path other)
	{
		if(!(other instanceof DatabasePath p) || p.fs != fs || isAbsolute() != p.isAbsolute())
			throw new IllegalArgumentException();
		List<String> a = parts(), b = p.parts();
		int i = 0;
		while(i < a.size() && i < b.size() && a.get(i).equals(b.get(i)))
			i++;
		List<String> r = new ArrayList<>();
		for(int j = i; j < a.size(); j++)
			r.add("..");
		r.addAll(b.subList(i, b.size()));
		return new DatabasePath(fs, String.join("/", r));
	}
	
	@Override
	public URI toUri()
	{
		return fs.provider().getPathUri(this);
	}
	
	@Override
	public Path toAbsolutePath()
	{
		return isAbsolute() ? this : new DatabasePath(fs, "/" + value);
	}
	
	@Override
	public Path toRealPath(LinkOption... options)
			throws java.io.IOException
	{
		Path p = toAbsolutePath().normalize();
		fs.provider().checkExists(p);
		return p;
	}
	
	@Override
	public File toFile()
	{
		throw new UnsupportedOperationException("A database path has no java.io.File representation");
	}
	
	@Override
	public WatchKey register(WatchService watcher, WatchEvent.Kind<?>[] events, WatchEvent.Modifier... modifiers)
	{
		throw new UnsupportedOperationException("WatchService unsupported");
	}
	
	@Override
	public WatchKey register(WatchService watcher, WatchEvent.Kind<?>... events)
	{
		throw new UnsupportedOperationException("WatchService unsupported");
	}
	
	@Override
	public Iterator<Path> iterator()
	{
		List<Path> p = new ArrayList<>();
		for(String s : parts())
			p.add(new DatabasePath(fs, s));
		return p.iterator();
	}
	
	@Override
	public int compareTo(Path other)
	{
		if(!(other instanceof DatabasePath p) || p.fs != fs)
			throw new ClassCastException();
		return value.compareTo(p.value);
	}
	
	@Override
	public boolean equals(Object o)
	{
		return o instanceof DatabasePath p && p.fs == fs && value.equals(p.value);
	}
	
	@Override
	public int hashCode()
	{
		return 31 * System.identityHashCode(fs) + value.hashCode();
	}
	
	@Override
	public String toString()
	{
		return value;
	}
}