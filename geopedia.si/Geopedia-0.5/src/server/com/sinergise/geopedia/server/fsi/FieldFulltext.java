package com.sinergise.geopedia.server.fsi;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;

import com.sinergise.geopedia.core.entities.walk.ExpressionFieldPath;
import com.sinergise.geopedia.core.entities.walk.FieldPath;
import com.sinergise.geopedia.db.QueryPreResult;

public class FieldFulltext extends QueryCond
{
	public FieldPath field;
	public FieldPath exprField;
	public String query;
	
	public FieldFulltext(FieldPath field, String query)
	{
		this.field = field;
		this.query = query;
		this.exprField=new ExpressionFieldPath(new String[]{"MATCH(",null,") AGAINST(?)"}, new FieldPath[]{field});
	}
	
	public void getNeeded(HashSet<FieldPath> needed)
	{
    	needed.add(field);
		needed.add(exprField);
	}
	
	public void toSQL(QueryPreResult qpr, StringBuilder sb)
	{
		String fname = qpr.getFieldName(field);

		sb.append("MATCH(");
    	sb.append(fname);
    	sb.append(") AGAINST(? IN BOOLEAN MODE)");
    	
	}
	
	public void setColumnValues(PreparedStatement ps, int[] currIndex) throws SQLException
	{
		ps.setString(++currIndex[0], query);
	}

	
	public void setValues(PreparedStatement ps, int[] currIndex) throws SQLException
	{
		ps.setString(++currIndex[0], query);
	}

	public FieldPath getExprPath() {
		return exprField;
	}
}
