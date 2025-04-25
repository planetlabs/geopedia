package com.sinergise.geopedia.db;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.util.HashMap;

import com.sinergise.geopedia.core.entities.walk.FieldPath;
import com.sinergise.geopedia.core.entities.walk.TablePath;

public class QueryPreResult
{
	private final HashMap<TablePath, String> needTables;
	private final HashMap<FieldPath, String> needFields;
	private final StringBuilder sqlSoFar;
	public Object2IntOpenHashMap<FieldPath> fieldIndexes;
	
	public QueryPreResult(HashMap<TablePath, String> tableNames, HashMap<FieldPath, String> fieldNames, Object2IntOpenHashMap<FieldPath> fieldIndexes, StringBuilder sqlSoFar)
	{
		this.needFields = fieldNames;
		this.needTables = tableNames;
		this.fieldIndexes = fieldIndexes;
		this.sqlSoFar = sqlSoFar;
	}

	public String getTableName(TablePath table)
	{
		return needTables.get(table);
	}
	
	public String getFieldName(FieldPath field)
	{
		return needFields.get(field);
	}
	
	public int getFieldIndex(FieldPath field)
	{
		return fieldIndexes.getInt(field);
	}
	
	public StringBuilder sqlSoFar()
	{
		return sqlSoFar;
	}
}
