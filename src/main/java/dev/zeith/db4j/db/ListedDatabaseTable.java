package dev.zeith.db4j.db;

import dev.zeith.db4j.rows.TableRow;

import java.util.List;

public class ListedDatabaseTable extends DatabaseTable
{
	public ListedDatabaseTable(String name, List<TableRow<? extends Object>> rows)
	{
		super(name, rows);
	}
}