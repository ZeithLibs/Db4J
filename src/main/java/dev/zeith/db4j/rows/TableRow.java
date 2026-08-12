package dev.zeith.db4j.rows;

import dev.zeith.db4j.data.RowConstraints;

import java.sql.*;
import java.util.EnumSet;
import java.util.stream.Collectors;

public class TableRow<DATA>
{
	protected final RowType<DATA> type;
	
	public final String name;
	public final EnumSet<RowConstraints> constraints;
	
	public final String references;
	
	public TableRow(String name, RowType<DATA> type)
	{
		this(name, type, EnumSet.noneOf(RowConstraints.class));
	}
	
	public TableRow(String name, RowType<DATA> type, EnumSet<RowConstraints> constraints)
	{
		this.name = name;
		this.type = type;
		this.constraints = constraints;
		this.references = "";
	}
	
	public TableRow(String name, RowType<DATA> type, EnumSet<RowConstraints> constraints, String referenceTable, String referenceColumn)
	{
		this.name = name;
		this.type = type;
		this.constraints = constraints;
		this.references = "FOREIGN KEY (" + name + ") REFERENCES " + referenceTable + "(" + referenceColumn + ")";
	}
	
	public String toPreparedSQLString()
	{
		return name + " " + getSQLTypeStr() + " " + String.join(" ", constraints.stream().filter(rc -> rc != RowConstraints.FOREIGN_KEY).map(RowConstraints::sql).collect(Collectors.toList()));
	}
	
	public String getSQLTypeStr()
	{
		return type.type.getSQLDataType();
	}
	
	public String getSQLTypeStrNoArgs()
	{
		return type.type.getSQLDataTypeNoArgs();
	}
	
	public String getExtraSQLCreationFlags()
	{
		return references;
	}
	
	public Class<DATA> javaType()
	{
		return type.javaType;
	}
	
	public void set(PreparedStatement statement, int columnIndex, DATA value) throws SQLException
	{
		type.set(statement, columnIndex, value);
	}
	
	public DATA get(ResultSet set, int columnIndex) throws SQLException
	{
		return type.get(set, columnIndex);
	}
	
	public DATA get(ResultSet set, String columnLabel) throws SQLException
	{
		return type.get(set, columnLabel);
	}
}