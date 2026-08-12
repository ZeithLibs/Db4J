package dev.zeith.db4j.backup;

import lombok.RequiredArgsConstructor;

import java.io.*;
import java.util.LinkedHashSet;

@RequiredArgsConstructor
public class BackupReader
{
	private final BackupPacket[] PACKETS = BackupPacket.values();
	private final DataInputStream input;
	private final IBackupReceptor<IOException> target;
	
	protected BackupPacket readPacketHeader()
			throws IOException
	{
		return PACKETS[input.readUnsignedByte()];
	}
	
	public void read()
			throws IOException
	{
		while(true)
		{
			switch(readPacketHeader())
			{
				case DROP_TABLES -> dropTables();
				case SET_TABLES -> setTables();
				case SQL_CMD -> sqlCmd();
				case SQL_USE -> sqlUse();
				case SQL_BATCH -> sqlBatch();
				case END_OF_DATA ->
				{
					end();
					return;
				}
			}
		}
	}
	
	protected void dropTables()
			throws IOException
	{
		target.dropTables();
	}
	
	protected void setTables()
			throws IOException
	{
		int entries = input.readInt();
		LinkedHashSet<String> copiedTables = new LinkedHashSet<>(entries);
		for(int i = 0; i < entries; i++) copiedTables.add(input.readUTF());
		target.setTables(copiedTables);
	}
	
	protected void sqlCmd()
			throws IOException
	{
		target.sqlCommand(input.readUTF());
	}
	
	protected void sqlUse()
			throws IOException
	{
		String originCatalog = input.readUTF();
		String sql = input.readUTF();
		target.useSqlCommand(originCatalog, sql);
	}
	
	protected void sqlBatch()
			throws IOException
	{
		IBackupReceptor.Batch<IOException> batch = target.insertSql(input.readUTF());
		SqlObjectSandbox snd = new SqlObjectSandbox();
		while(true)
		{
			int insn = input.readUnsignedByte();
			switch(insn)
			{
				case 88 ->
				{
					batch.addBatch();
				}
				case 89 ->
				{
					int i = input.readInt();
					byte[] data = new byte[input.readInt()];
					input.readFully(data);
					Object object;
					try
					{
						object = snd.deserialize(data);
					} catch(Exception e)
					{
						throw new IOException("Failed to deserialize batch object " + i, e);
					}
					batch.setObject(i, object);
				}
				case 90 ->
				{
					batch.executeBatch();
				}
				case 91 ->
				{
					batch.close();
					return;
				}
				default -> throw new IOException("Unknown batch instruction " + insn);
			}
		}
	}
	
	protected void end()
			throws IOException
	{
		input.close();
		target.close();
	}
}