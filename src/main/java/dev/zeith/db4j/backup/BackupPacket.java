package dev.zeith.db4j.backup;

public enum BackupPacket
{
	DROP_TABLES,
	SET_TABLES,
	SQL_USE,
	SQL_CMD,
	SQL_BATCH,
	END_OF_DATA
}