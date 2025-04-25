package com.sinergise.geopedia.server.fsi;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;

import com.sinergise.geopedia.core.entities.walk.FieldPath;
import com.sinergise.geopedia.db.QueryPreResult;
import com.sinergise.geopedia.server.service.FeatureServiceImpl;

public class FieldCmp extends QueryCond
{
	FieldPath field;
	Object value;
	String op;
	
	public FieldCmp(FieldPath field, String op, Object value)
	{
		this.field = field;
		this.op = op;
		this.value = value;
	}
	
	public void getNeeded(HashSet<FieldPath> needed)
	{
		needed.add(field);
	}
	
	public void toSQL(QueryPreResult qpr, StringBuilder sb)
	{
		String fname = qpr.getFieldName(field);
		
		if (value == null) {
			if (op == "=") {
				sb.append(fname);
				sb.append(" IS NULL");
			} else
			if (op == "<>" || op == "!=") {
				sb.append(fname);
				sb.append(" IS NOT NULL");
			} else {
				throw new IllegalStateException("Can't "+op+" null");
			}
		} else {
			sb.append(fname);
			sb.append(op);
			sb.append('?');
		}
	}
	
	public void setValues(PreparedStatement ps, int[] currIndex) throws SQLException
	{
		if (value != null) {
			FeatureServiceImpl.setStandardValue(ps, ++currIndex[0], value);
		}
	}
	
	public void setColumnValues(PreparedStatement ps, int[] currIndex) {
		// nothing to do
	}
}
