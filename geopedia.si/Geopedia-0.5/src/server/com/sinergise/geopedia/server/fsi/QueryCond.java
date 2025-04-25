package com.sinergise.geopedia.server.fsi;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;

import com.sinergise.geopedia.core.entities.walk.FieldPath;
import com.sinergise.geopedia.db.QueryPreResult;

public abstract class QueryCond
{
	public abstract void getNeeded(HashSet<FieldPath> needed);
	public abstract void toSQL(QueryPreResult qpr, StringBuilder sb);
	public abstract void setValues(PreparedStatement ps, int[] currIndex) throws SQLException;
	public abstract void setColumnValues(PreparedStatement ps, int[] currIndex) throws SQLException;
}
