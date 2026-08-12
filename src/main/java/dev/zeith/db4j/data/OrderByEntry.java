package dev.zeith.db4j.data;

import dev.zeith.db4j.rows.TableRow;

public record OrderByEntry(TableRow<?> row, boolean desc)
{
	@Override
	public String toString()
	{
		return row.name + " " + (desc ? "DESC" : "ASC");
	}
}