package dev.zeith.db4j.backup;

import lombok.RequiredArgsConstructor;

import java.io.*;
import java.util.LinkedHashSet;
import java.util.concurrent.atomic.AtomicBoolean;

@RequiredArgsConstructor
public class BackupWriter
		implements IBackupReceptor<IOException>
{
	private final DataOutputStream output;
	
	private final AtomicBoolean locked = new AtomicBoolean();
	
	private Closeable writePacketHeader(BackupPacket packet)
			throws IOException
	{
		if(locked.get()) throw new IOException("Can not acquire packet write access.");
		output.writeByte(packet.ordinal());
		output.flush();
		locked.set(true);
		return () -> locked.set(false);
	}
	
	@Override
	public void dropTables()
			throws IOException
	{
		writePacketHeader(BackupPacket.DROP_TABLES).close();
	}
	
	@Override
	public void setTables(LinkedHashSet<String> copiedTables)
			throws IOException
	{
		Closeable finish = writePacketHeader(BackupPacket.SET_TABLES);
		output.writeInt(copiedTables.size());
		for(String t : copiedTables) output.writeUTF(t);
		finish.close();
	}
	
	@Override
	public void useSqlCommand(String originCatalog, String sql)
			throws IOException
	{
		Closeable finish = writePacketHeader(BackupPacket.SQL_USE);
		output.writeUTF(originCatalog);
		output.writeUTF(sql);
		finish.close();
	}
	
	@Override
	public void sqlCommand(String sql)
			throws IOException
	{
		Closeable finish = writePacketHeader(BackupPacket.SQL_CMD);
		output.writeUTF(sql);
		finish.close();
	}
	
	@Override
	public Batch<IOException> insertSql(String sql)
			throws IOException
	{
		Closeable finish = writePacketHeader(BackupPacket.SQL_BATCH);
		output.writeUTF(sql);
		SqlObjectSandbox snd = new SqlObjectSandbox();
		return new Batch<>()
		{
			@Override
			public void addBatch()
					throws IOException
			{
				output.writeByte(88);
			}
			
			@Override
			public void setObject(int i, Object object)
					throws IOException
			{
				output.writeByte(89);
				
				output.writeInt(i);
				
				byte[] data = snd.serialize(object);
				output.writeInt(data.length);
				output.write(data);
				
				output.flush();
			}
			
			@Override
			public void executeBatch()
					throws IOException
			{
				output.writeByte(90);
			}
			
			@Override
			public void close()
					throws IOException
			{
				output.writeByte(91);
				finish.close();
			}
		};
	}
	
	@Override
	public void close()
			throws IOException
	{
		writePacketHeader(BackupPacket.END_OF_DATA).close();
		output.flush();
		output.close();
	}
}