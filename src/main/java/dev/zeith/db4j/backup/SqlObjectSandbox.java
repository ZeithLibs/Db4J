package dev.zeith.db4j.backup;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;

public class SqlObjectSandbox
{
	private final ByteArrayOutputStream baos = new ByteArrayOutputStream(128);
	private final DataOutputStream dos = new DataOutputStream(baos);
	
	public byte[] serialize(Object obj)
			throws IOException
	{
		baos.reset();
		
		if(obj == null)
		{
			dos.writeByte(0); // null marker
		} else if(obj instanceof Integer i)
		{
			dos.writeByte(1);
			dos.writeInt(i);
		} else if(obj instanceof Long l)
		{
			dos.writeByte(2);
			dos.writeLong(l);
		} else if(obj instanceof String s)
		{
			dos.writeByte(3);
			byte[] strBytes = s.getBytes(StandardCharsets.UTF_8);
			dos.writeInt(strBytes.length);
			dos.write(strBytes);
		} else if(obj instanceof Double d)
		{
			dos.writeByte(4);
			dos.writeDouble(d);
		} else if(obj instanceof Boolean b)
		{
			dos.writeByte(5);
			dos.writeBoolean(b);
		} else if(obj instanceof byte[] bytes)
		{
			dos.writeByte(6);
			dos.writeInt(bytes.length);
			dos.write(bytes);
		} else if(obj instanceof Timestamp ts)
		{
			dos.writeByte(7);
			dos.writeLong(ts.getTime());
		} else if(obj instanceof Date date)
		{
			dos.writeByte(8);
			dos.writeLong(date.getTime());
		} else if(obj instanceof Float flt)
		{
			dos.writeByte(9);
			dos.writeFloat(flt);
		} else
		{
			throw new IllegalArgumentException("Unsupported SQL object type: " + obj.getClass());
		}
		
		dos.flush();
		return baos.toByteArray();
	}
	
	/** Deserialize a SQL-compatible object from byte[] */
	public Object deserialize(byte[] data)
			throws IOException
	{
		var dis = new DataInputStream(new ByteArrayInputStream(data));
		
		byte type = dis.readByte();
		return switch(type)
		{
			case 0 -> null;
			case 1 -> dis.readInt();
			case 2 -> dis.readLong();
			case 3 ->
			{
				int len = dis.readInt();
				byte[] strBytes = new byte[len];
				dis.readFully(strBytes);
				yield new String(strBytes, StandardCharsets.UTF_8);
			}
			case 4 -> dis.readDouble();
			case 5 -> dis.readBoolean();
			case 6 ->
			{
				int len = dis.readInt();
				byte[] bytes = new byte[len];
				dis.readFully(bytes);
				yield bytes;
			}
			case 7 -> new Timestamp(dis.readLong());
			case 8 -> new Date(dis.readLong());
			case 9 -> dis.readFloat();
			default -> throw new IOException("Unknown type marker: " + type);
		};
	}
}