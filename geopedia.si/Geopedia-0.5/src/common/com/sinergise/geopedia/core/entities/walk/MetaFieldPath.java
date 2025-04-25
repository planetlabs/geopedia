package com.sinergise.geopedia.core.entities.walk;

//import java.util.HashMap;

//import com.cosylab.gisopedia.db.QueryBuilder;

public class MetaFieldPath extends FieldPath
{
	public static final int MF_TIMESTAMP = 1;
	public static final int MF_FULLTEXT = 2;
	public static final int MF_USER = 3;
	public static final int MF_DELETED = 4;
	public static final int MF_GEOMETRY = 5;
	public static final int MF_ID = 6;
	public static final int MF_MINX = 7;
	public static final int MF_MAXX = 8;
	public static final int MF_MINY = 9;
	public static final int MF_MAXY = 10;
	public static final int MF_AREA = 11;
	public static final int MF_LENGTH = 12;
	public static final int MF_DRAWING = 13;
	public static final int MF_CENTROID_X = 14;
	public static final int MF_CENTROID_Y = 15;
	
	public static final int MF_MAX=16;
	public int whichOne;
	
	public MetaFieldPath()
    {
		// needed for serialization :-/
    }
	
	public MetaFieldPath(TablePath table, int whichOne)
	{
		this.table = table;
		this.whichOne = whichOne;
	}
	
	// don't change - needed for encoding styles
		public void toStringJS(StringBuffer sb)
	    {
			throw new IllegalStateException();
			
	    }
		
	// don't change - needed for encoding styles
	public void toString(StringBuffer sb)
    {
		if (table.tableIds.length == 1) {
			sb.append('$');
		} else {
			table.toString(sb);
			sb.append('.');
		}
		switch(whichOne) {
		case MF_AREA:
			sb.append("area"); break;
		case MF_CENTROID_X:
			sb.append("centX"); break;
		case MF_CENTROID_Y:
			sb.append("centY"); break;
		case MF_DELETED:
			sb.append("deleted"); break;
		case MF_ID:
			sb.append("id"); break;
		case MF_LENGTH:
			sb.append("length"); break;
		case MF_MAXX:
			sb.append("maxX"); break;
		case MF_MAXY:
			sb.append("maxY"); break;
		case MF_MINX:
			sb.append("minX"); break;
		case MF_MINY:
			sb.append("minY"); break;
		case MF_TIMESTAMP:
			sb.append("time"); break;
		
		case MF_DRAWING:
		case MF_FULLTEXT:
		case MF_GEOMETRY:
		case MF_USER:
		default:
			throw new IllegalStateException();
		}
    }
	
	public int hashCode()
	{
		return table.hashCode() * 31 + whichOne * 7919;
	}
	
	public boolean equals(Object obj)
	{
		if (obj instanceof MetaFieldPath) {
			MetaFieldPath other = (MetaFieldPath) obj;
			
			return table.equals(other.table) && whichOne == other.whichOne;
		}
		
		return false;
	}
	
	/*
	public String getSelectExpression(HashMap tableAliasMap) {
		return tableAliasMap.get(table)+"."+QueryBuilder.metaFieldDbName(table.lastTableId(), whichOne);
	}
	
	public String getAlias(HashMap tableAliasMap) {
		return getSelectExpression(tableAliasMap);
	}
	*/
}
