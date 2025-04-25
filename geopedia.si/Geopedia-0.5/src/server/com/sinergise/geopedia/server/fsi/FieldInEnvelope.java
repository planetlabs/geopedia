package com.sinergise.geopedia.server.fsi;

import java.sql.PreparedStatement;
import java.util.HashSet;

import com.sinergise.common.util.geom.Envelope;
import com.sinergise.geopedia.core.entities.walk.FieldPath;
import com.sinergise.geopedia.core.entities.walk.MetaFieldPath;
import com.sinergise.geopedia.db.QueryPreResult;


public class FieldInEnvelope extends QueryCond
{
	MetaFieldPath geomField;
	Envelope e;
	
	public FieldInEnvelope(MetaFieldPath geomField, Envelope e)
	{
		this.geomField = geomField;
		this.e = e;
	}
	
	public void getNeeded(HashSet<FieldPath> needed)
	{
		needed.add(geomField);
	}
	
	public void toSQL(QueryPreResult qpr, StringBuilder sb)
	{
		String fname = qpr.getFieldName(geomField);
		
		sb.append("MBRIntersects(geomfromtext('linestring(");
		sb.append(e.getMinX());
		sb.append(' ');
		sb.append(e.getMinY());
		sb.append(',');
		sb.append(e.getMaxX());
		sb.append(' ');
		sb.append(e.getMaxY());
		sb.append(")'), ");
		sb.append(fname);
		sb.append(")");
	}
	
	public void setValues(PreparedStatement ps, int[] currIndex)
	{
		// nothibng to do
	}
	public void setColumnValues(PreparedStatement ps, int[] currIndex) {
		// nothing to do
	}
}
