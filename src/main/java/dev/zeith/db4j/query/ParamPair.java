package dev.zeith.db4j.query;

import dev.zeith.db4j.rows.TableRow;

import java.sql.*;

record ParamPair<T>(TableRow<T> row, T data)
{
	public void set(PreparedStatement stmt, int idx)
			throws SQLException
	{
		row.set(stmt, idx, data);
	}
	
	@Override
	public String toString()
	{
		return "ParamPair[" + "row=" + row + ", " + "data=" + data + ']';
	}
}
