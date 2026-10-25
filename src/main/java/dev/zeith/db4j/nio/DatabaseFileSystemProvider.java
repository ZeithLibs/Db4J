package dev.zeith.db4j.nio;

import dev.zeith.db4j.db.AbstractDatabase;
import dev.zeith.db4j.util.*;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.nio.file.spi.FileSystemProvider;
import java.sql.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/** A database-backed NIO provider. File contents are stored as one BLOB per row. */
public final class DatabaseFileSystemProvider
		extends FileSystemProvider
{
	public static final String ENV_TABLE = "table";
	public static final String ENV_READONLY = "readonly";
	private final Map<String, DatabaseFileSystem> fileSystems = new ConcurrentHashMap<>();
	
	@Override
	public String getScheme()
	{
		return "db4j";
	}
	
	public AbstractDatabase database(DatabasePath path)
	{
		return path.fs.table.getDatabase();
	}
	
	@Override
	public FileSystem newFileSystem(URI uri, Map<String, ?> env)
			throws IOException
	{
		Objects.requireNonNull(uri);
		if(!getScheme().equalsIgnoreCase(uri.getScheme()))
			throw new IllegalArgumentException("Expected db4j URI");
		String key = uri.getAuthority();
		if(key == null || key.isBlank())
			key = "default";
		Object tableValue = env.get(ENV_TABLE);
		if(!(tableValue instanceof FileTableSimple table))
			throw new IllegalArgumentException("newFileSystem requires env attribute '" + ENV_TABLE + "' containing a FileTableSimple");
		String tableName = table.getName();
		if(!tableName.matches("[A-Za-z_][A-Za-z0-9_]*"))
			throw new IllegalArgumentException("Unsafe SQL table name: " + tableName);
		boolean readOnly = Boolean.TRUE.equals(env.get(ENV_READONLY));
		DatabaseFileSystem fs = new DatabaseFileSystem(this, key, table, readOnly);
		if(fileSystems.putIfAbsent(key, fs) != null)
			throw new FileSystemAlreadyExistsException(key);
		try
		{
			ensureRoot(fs);
		} catch(IOException e)
		{
			fileSystems.remove(key, fs);
			throw e;
		}
		return fs;
	}
	
	@Override
	public FileSystem getFileSystem(URI uri)
	{
		String key = uri.getAuthority();
		if(key == null || key.isBlank())
			key = "default";
		DatabaseFileSystem fs = fileSystems.get(key);
		if(fs == null)
			throw new FileSystemNotFoundException(key);
		return fs;
	}
	
	@Override
	public Path getPath(URI uri)
	{
		return getFileSystem(uri).getPath(uri.getPath() == null ? "/" : uri.getPath());
	}
	
	void remove(String key, DatabaseFileSystem fs)
	{
		fileSystems.remove(key, fs);
	}
	
	URI getPathUri(DatabasePath path)
	{
		try
		{
			return new URI("db4j", path.fs.key, path.toAbsolutePath().toString(), null);
		} catch(Exception e)
		{
			throw new IllegalArgumentException(e);
		}
	}
	
	private DatabasePath path(Path p)
	{
		if(!(p instanceof DatabasePath dp))
			throw new ProviderMismatchException();
		if(dp.fs.provider != this)
			throw new ProviderMismatchException();
		dp.fs.ensureOpen();
		return dp;
	}
	
	private void writable(DatabasePath p)
			throws IOException
	{
		if(p.fs.isReadOnly())
			throw new ReadOnlyFileSystemException();
	}
	
	private SQLException sqlError(IOException e)
	{
		Throwable c = e;
		while(c != null && !(c instanceof SQLException))
			c = c.getCause();
		return c instanceof SQLException s ? s : new SQLException(e);
	}
	
	private String tableName(DatabasePath p)
	{
		return p.fs.table.getName();
	}
	
	private void ensureRoot(DatabaseFileSystem fs)
			throws IOException
	{
		DatabasePath root = (DatabasePath) fs.getPath("/");
		if(!exists(root))
			insert(root, true, new byte[0], Instant.now(), Instant.now(), true);
	}
	
	private boolean exists(DatabasePath p)
			throws IOException
	{
		try(PreparedStatement s = database(p).prepareStatement(null, "SELECT 1 FROM " + tableName(p) + " WHERE Path = ?"))
		{
			s.setString(1, p.dbPath());
			try(ResultSet r = s.executeQuery())
			{
				return r.next();
			}
		} catch(SQLException e)
		{
			throw new IOException(e);
		}
	}
	
	private record Entry(UUID uid, boolean directory, byte[] data, Instant created, Instant modified) {}
	
	private Entry entry(DatabasePath p)
			throws IOException
	{
		try(PreparedStatement s = database(p).prepareStatement(null, "SELECT IsDirectory, Data, CreatedAt, LastModified, UID FROM " + tableName(p) + " WHERE Path = ?"))
		{
			s.setString(1, p.dbPath());
			try(ResultSet r = s.executeQuery())
			{
				if(!r.next())
					throw new NoSuchFileException(p.toString());
				Timestamp c = r.getTimestamp(3), m = r.getTimestamp(4);
				UUID uid = FileTableSimple.UID.get(r, 5);
				return new Entry(uid, r.getBoolean(1), r.getBytes(2), c == null ? Instant.EPOCH : c.toInstant(), m == null ? Instant.EPOCH : m.toInstant());
			}
		} catch(SQLException e)
		{
			throw new IOException(e);
		}
	}
	
	private void insert(DatabasePath target, boolean dir, byte[] data, Instant created, Instant modified, boolean ignoreExists)
			throws IOException
	{
		String path = target.dbPath();
		try(PreparedStatement s = database(target).prepareStatement(null, "INSERT INTO " + tableName(target) + " (UID, Path, CreatedAt, LastModified, IsDirectory, Data) VALUES (?, ?, ?, ?, ?, ?)"))
		{
			FileTableSimple.UID.set(s, 1, UUIDv7.randomUUID());
			s.setString(2, path);
			s.setTimestamp(3, Timestamp.from(created));
			s.setTimestamp(4, Timestamp.from(modified));
			s.setBoolean(5, dir);
			s.setBytes(6, dir ? null : data);
			s.executeUpdate();
		} catch(SQLException e)
		{
			if(ignoreExists && exists(target))
				return;
			throw new IOException(e);
		}
	}
	
	private void writeEntry(DatabasePath p, byte[] data, boolean createNew)
			throws IOException
	{
		writable(p);
		String name = p.dbPath();
		Instant now = Instant.now();
		try
		{
			Entry old = entry(p);
			if(old.directory)
				throw new FileSystemException(name, null, "Is a directory");
			if(createNew)
				throw new FileAlreadyExistsException(name);
			try(PreparedStatement s = database(p).prepareStatement(null, "UPDATE " + tableName(p) + " SET Data = ?, LastModified = ? WHERE Path = ? AND IsDirectory = ?"))
			{
				s.setBytes(1, data);
				s.setTimestamp(2, Timestamp.from(now));
				s.setString(3, name);
				s.setBoolean(4, false);
				s.executeUpdate();
			}
		} catch(NoSuchFileException e)
		{
			if(!createNew) throw e;
			Path parent = p.getParent();
			if(parent != null && !exists(path((Path) parent)))
				throw new NoSuchFileException(parent.toString());
			insert(p, false, data, now, now, false);
		} catch(SQLException e)
		{
			throw new IOException(e);
		}
	}
	
	void checkExists(Path path)
			throws IOException
	{
		entry(path(path));
	}
	
	@Override
	public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs)
			throws IOException
	{
		DatabasePath p = path(path);
		
		boolean write = false;
		boolean create = false;
		boolean createNew = false;
		boolean truncate = false;
		
		for(OpenOption opt : options)
		{
			if(opt == StandardOpenOption.WRITE || opt == StandardOpenOption.APPEND)
			{
				write = true;
			} else if(opt == StandardOpenOption.CREATE || opt == StandardOpenOption.CREATE_NEW)
			{
				create = true;
				createNew = opt == StandardOpenOption.CREATE_NEW;
			} else if(opt == StandardOpenOption.TRUNCATE_EXISTING)
			{
				truncate = true;
			} else
				throw new UnsupportedOperationException("Unsupported open option: " + opt);
		}
		
		if(write)
			writable(p);
		
		byte[] initial;
		try
		{
			Entry e = entry(p);
			
			if(e.directory)
				throw new FileSystemException(p.toString(), null, "Is a directory");
			if(createNew)
				throw new FileAlreadyExistsException(p.toString());
			
			initial = e.data == null ? new byte[0] : e.data;
			if(write && truncate) initial = new byte[0];
		} catch(NoSuchFileException e)
		{
			if(!create)
				throw e;
			
			Path parent = p.getParent();
			if(parent != null && !exists(path(parent)))
				throw new NoSuchFileException(parent.toString());
			
			initial = new byte[0];
			writeEntry(p, initial, true);
		}
		
		return new DatabaseSeekableByteChannel(p, initial, write, options.contains(StandardOpenOption.APPEND));
	}
	
	private final class DatabaseSeekableByteChannel
			implements SeekableByteChannel
	{
		final DatabasePath p;
		final boolean writable, append;
		byte[] data;
		int position;
		boolean open = true, dirty;
		
		DatabaseSeekableByteChannel(DatabasePath p, byte[] data, boolean writable, boolean append)
		{
			this.p = p;
			this.data = data.clone();
			this.writable = writable;
			this.append = append;
			this.position = append ? data.length : 0;
			this.dirty = writable && !append && data.length == 0;
		}
		
		private void open()
				throws ClosedChannelException
		{
			if(!open)
				throw new ClosedChannelException();
		}
		
		@Override
		public int read(ByteBuffer dst)
				throws IOException
		{
			open();
			if(position >= data.length)
				return -1;
			int n = Math.min(dst.remaining(), data.length - position);
			dst.put(data, position, n);
			position += n;
			return n;
		}
		
		@Override
		public int write(ByteBuffer src)
				throws IOException
		{
			open();
			if(!writable)
				throw new NonWritableChannelException();
			if(append)
				position = data.length;
			int n = src.remaining();
			long end = (long) position + n;
			if(end > Integer.MAX_VALUE)
				throw new IOException("BLOB exceeds Java array limit");
			if(end > data.length)
				data = Arrays.copyOf(data, (int) end);
			src.get(data, position, n);
			position += n;
			dirty = true;
			return n;
		}
		
		@Override
		public long position()
				throws IOException
		{
			open();
			return position;
		}
		
		@Override
		public SeekableByteChannel position(long n)
				throws IOException
		{
			open();
			if(n < 0 || n > Integer.MAX_VALUE)
				throw new IllegalArgumentException("Invalid position: " + n);
			position = (int) n;
			return this;
		}
		
		@Override
		public long size()
				throws IOException
		{
			open();
			return data.length;
		}
		
		@Override
		public SeekableByteChannel truncate(long size)
				throws IOException
		{
			open();
			if(!writable)
				throw new NonWritableChannelException();
			if(size < 0)
				throw new IllegalArgumentException();
			if(size < data.length)
			{
				data = Arrays.copyOf(data, (int) size);
				position = Math.min(position, (int) size);
				dirty = true;
			}
			return this;
		}
		
		@Override
		public boolean isOpen()
		{
			return open;
		}
		
		@Override
		public void close()
				throws IOException
		{
			if(!open)
				return;
			open = false;
			if(writable && (dirty || !exists(p)))
				writeEntry(p, data, false);
		}
	}
	
	@Override
	public void createDirectory(Path dir, FileAttribute<?>... attrs)
			throws IOException
	{
		DatabasePath p = path(dir);
		writable(p);
		if(exists(p))
			throw new FileAlreadyExistsException(p.toString());
		Path parent = p.getParent();
		if(parent != null && !exists(path(parent)))
			throw new NoSuchFileException(parent.toString());
		Instant n = Instant.now();
		insert(p, true, null, n, n, false);
	}
	
	@Override
	public void delete(Path path)
			throws IOException
	{
		DatabasePath p = path(path);
		writable(p);
		Entry e = entry(p);
		if(e.directory && !list(p).isEmpty())
			throw new DirectoryNotEmptyException(p.toString());
		try(PreparedStatement s = database(p).prepareStatement(null, "DELETE FROM " + tableName(p) + " WHERE Path = ?"))
		{
			s.setString(1, p.dbPath());
			if(s.executeUpdate() == 0)
				throw new NoSuchFileException(p.toString());
		} catch(SQLException ex)
		{
			throw new IOException(ex);
		}
	}
	
	private List<String> list(DatabasePath dir)
			throws IOException
	{
		String prefix = dir.dbPath().equals("/") ? "/" : dir.dbPath() + "/";
		List<String> out = new ArrayList<>();
		try(PreparedStatement s = database(dir).prepareStatement(null, "SELECT Path FROM " + tableName(dir) + " WHERE Path LIKE ?"))
		{
			s.setString(1, SQLHelper.escapeSqlLikePattern(prefix) + "%");
			try(ResultSet r = s.executeQuery())
			{
				while(r.next())
				{
					String full = r.getString(1);
					String rest = full.substring(prefix.length());
					if(!rest.isEmpty() && !rest.contains("/"))
						out.add(full);
				}
			}
			return out;
		} catch(SQLException e)
		{
			throw new IOException(e);
		}
	}
	
	@Override
	public void copy(Path source, Path target, CopyOption... options)
			throws IOException
	{
		DatabasePath a = path(source), b = path(target);
		boolean replace = Arrays.asList(options).contains(StandardCopyOption.REPLACE_EXISTING);
		Entry e = entry(a);
		if(e.directory)
		{
			createDirectory(b);
			return;
		}
		if(exists(b))
		{
			if(!replace)
				throw new FileAlreadyExistsException(b.toString());
			delete(b);
		}
		writeEntry(b, e.data == null ? new byte[0] : e.data, true);
	}
	
	@Override
	public void move(Path source, Path target, CopyOption... options)
			throws IOException
	{
		DatabasePath a = path(source), b = path(target);
		if(a.equals(b))
			return;
		boolean replace = Arrays.asList(options).contains(StandardCopyOption.REPLACE_EXISTING);
		if(exists(b))
		{
			if(!replace)
				throw new FileAlreadyExistsException(b.toString());
			delete(b);
		}
		copy(a, b, options);
		delete(a);
	}
	
	@Override
	public boolean isSameFile(Path a, Path b)
			throws IOException
	{
		DatabasePath x = path(a), y = path(b);
		if(x.equals(y))
			return true;
		return exists(x) && exists(y) && x.dbPath().equals(y.dbPath());
	}
	
	@Override
	public boolean isHidden(Path path)
			throws IOException
	{
		DatabasePath p = path(path);
		checkAccess(p);
		Path n = p.getFileName();
		return n != null && n.toString().startsWith(".");
	}
	
	@Override
	public FileStore getFileStore(Path path)
			throws IOException
	{
		DatabasePath p = path(path);
		checkAccess(p);
		return p.fs.getFileStores().iterator().next();
	}
	
	@Override
	public void checkAccess(Path path, AccessMode... modes)
			throws IOException
	{
		DatabasePath p = path(path);
		entry(p);
		for(AccessMode m : modes)
		{
			if(m == AccessMode.WRITE && p.fs.isReadOnly())
				throw new AccessDeniedException(p.toString());
			if(m != AccessMode.READ && m != AccessMode.WRITE && m != AccessMode.EXECUTE)
				throw new UnsupportedOperationException();
			if(m == AccessMode.EXECUTE)
				throw new AccessDeniedException(p.toString(), null, "Execute permission is not implemented");
		}
	}
	
	@Override
	public <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> type, LinkOption... options)
	{
		if(type == BasicFileAttributeView.class)
			return type.cast(new BasicFileAttributeView()
			{
				@Override
				public String name()
				{
					return "basic";
				}
				
				@Override
				public BasicFileAttributes readAttributes()
						throws IOException
				{
					return DatabaseFileSystemProvider.this.readAttributes(path, BasicFileAttributes.class, options);
				}
				
				@Override
				public void setTimes(FileTime m, FileTime a, FileTime c)
						throws IOException
				{
					setModified(path, m);
				}
			});
		return null;
	}
	
	private void setModified(Path path, FileTime time)
			throws IOException
	{
		DatabasePath p = path(path);
		writable(p);
		try(PreparedStatement s = database(p).prepareStatement(null, "UPDATE " + tableName(p) + " SET LastModified = ? WHERE Path = ?"))
		{
			s.setTimestamp(1, time == null ? null : Timestamp.from(time.toInstant()));
			s.setString(2, p.dbPath());
			if(s.executeUpdate() == 0)
				throw new NoSuchFileException(p.toString());
		} catch(SQLException e)
		{
			throw new IOException(e);
		}
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption... options)
			throws IOException
	{
		if(type != BasicFileAttributes.class)
			throw new UnsupportedOperationException("Only BasicFileAttributes are supported");
		Entry e = entry(path(path));
		return (A) new DatabaseFileAttributes(e.uid, e.directory, e.directory ? 0 : e.data == null ? 0 : e.data.length, e.created, e.modified);
	}
	
	@Override
	public Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options)
			throws IOException
	{
		BasicFileAttributes a = readAttributes(path, BasicFileAttributes.class, options);
		Map<String, Object> m = new LinkedHashMap<>();
		for(String k : attributes.split(","))
		{
			switch(k)
			{
				case "size" -> m.put(k, a.size());
				case "creationTime" -> m.put(k, a.creationTime());
				case "lastModifiedTime" -> m.put(k, a.lastModifiedTime());
				case "lastAccessTime" -> m.put(k, a.lastAccessTime());
				case "isRegularFile" -> m.put(k, a.isRegularFile());
				case "isDirectory" -> m.put(k, a.isDirectory());
				case "isSymbolicLink" -> m.put(k, false);
				case "isOther" -> m.put(k, false);
				case "fileKey" -> m.put(k, null);
				case "*" ->
				{
					m.put("size", a.size());
					m.put("creationTime", a.creationTime());
					m.put("lastModifiedTime", a.lastModifiedTime());
					m.put("lastAccessTime", a.lastAccessTime());
					m.put("isRegularFile", a.isRegularFile());
					m.put("isDirectory", a.isDirectory());
					m.put("isSymbolicLink", false);
					m.put("isOther", false);
					m.put("fileKey", null);
				}
				default -> throw new IllegalArgumentException("Unknown basic attribute: " + k);
			}
		}
		return m;
	}
	
	@Override
	public void setAttribute(Path path, String attribute, Object value, LinkOption... options)
			throws IOException
	{
		if(attribute.equals("basic:lastModifiedTime") && value instanceof FileTime t)
		{
			setModified(path, t);
			return;
		}
		throw new UnsupportedOperationException(attribute);
	}
	
	@Override
	public DirectoryStream<Path> newDirectoryStream(Path dir, DirectoryStream.Filter<? super Path> filter)
			throws IOException
	{
		DatabasePath p = path(dir);
		Entry e = entry(p);
		if(!e.directory)
			throw new NotDirectoryException(p.toString());
		List<Path> children = new ArrayList<>();
		for(String child : list(p))
		{
			DatabasePath cp = new DatabasePath(p.fs, child);
			if(filter.accept(cp))
				children.add(cp);
		}
		return new DirectoryStream<>()
		{
			boolean open = true;
			
			@Override
			public Iterator<Path> iterator()
			{
				if(!open) throw new IllegalStateException("closed");
				return Collections.unmodifiableList(children).iterator();
			}
			
			@Override
			public void close()
			{
				open = false;
			}
		};
	}
}