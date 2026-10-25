package dev.zeith.db4j.nio;

import dev.zeith.db4j.data.RowConstraints;
import dev.zeith.db4j.db.DatabaseTable;
import dev.zeith.db4j.rows.*;

import java.time.Instant;
import java.util.*;

public class FileTableSimple
		extends DatabaseTable
{
	public static final TableRow<UUID> UID = new TableRow<>("UID", RowTypes.UUID_AS_BINARY, EnumSet.of(RowConstraints.NOT_NULL, RowConstraints.UNIQUE, RowConstraints.PRIMARY_KEY));
	public static final TableRow<String> PATH = new TableRow<>("Path", RowTypes.VARCHAR_512, EnumSet.of(RowConstraints.NOT_NULL, RowConstraints.UNIQUE));
	public static final TableRow<Instant> CREATED_AT = new TableRow<>("CreatedAt", RowTypes.INSTANT_AS_TIMESTAMP);
	public static final TableRow<Instant> LAST_MODIFIED = new TableRow<>("LastModified", RowTypes.INSTANT_AS_TIMESTAMP);
	public static final TableRow<Boolean> IS_DIRECTORY = new TableRow<>("IsDirectory", RowTypes.BOOLEAN);
	
	public FileTableSimple(String name, RowType<byte[]> data)
	{
		super(name, UID, PATH, CREATED_AT, LAST_MODIFIED, IS_DIRECTORY, new TableRow<>("Data", data));
	}
	
	/** Reserved for custom schemas; include every column used by the provider plus Data. */
	protected FileTableSimple(String name, TableRow<?>... rows)
	{
		super(name, rows);
	}
}