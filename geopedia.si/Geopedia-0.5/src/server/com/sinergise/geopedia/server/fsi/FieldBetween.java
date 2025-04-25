package com.sinergise.geopedia.server.fsi;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;

import com.sinergise.geopedia.core.entities.walk.FieldPath;
import com.sinergise.geopedia.db.QueryPreResult;
import com.sinergise.geopedia.server.service.FeatureServiceImpl;

public class FieldBetween extends QueryCond
{
	FieldPath fp;
	Object min, max;
	
	public FieldBetween(FieldPath fp, Object min, Object max)
    {
		this.fp = fp;
		this.min = min;
		this.max = max;
    }
	
	public void getNeeded(HashSet<FieldPath> needed)
	{
		needed.add(fp);
	}
	
	public void toSQL(QueryPreResult qpr, StringBuilder sb)
	{
		sb.append(qpr.getFieldName(fp));
		sb.append(" BETWEEN ? AND ?");
	}
	
	public void setValues(PreparedStatement ps, int[] currIndex) throws SQLException
	{
		FeatureServiceImpl.setStandardValue(ps, ++currIndex[0], min);
		FeatureServiceImpl.setStandardValue(ps, ++currIndex[0], max);
	}
	
	public void setColumnValues(PreparedStatement ps, int[] currIndex) {
		//nothing to do
	}
}
