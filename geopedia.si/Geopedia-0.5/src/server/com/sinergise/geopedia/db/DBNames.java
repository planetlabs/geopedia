package com.sinergise.geopedia.db;

import com.sinergise.geopedia.core.entities.Field;
import com.sinergise.geopedia.core.entities.Table;

@Deprecated
public class DBNames
{
	private static final String tbl = "pedicadata.t";
	public static String table(Table t)
	{
		return table(t.id);
	}
	
	public static String table(int tableId)
	{
		return tbl+tableId;
	}
	
	
	private static final String histTbl = "pedicadata.ht";
	public static String histTable(Table t)
	{
		return histTable(t.id);
	}
	
	public static String histTable(int tableId)
	{
		return histTbl+tableId;
	}
	
	private static final String user_field = "f";
	public static String userField(Field f)
	{
		return userField(f.id);
	}
	
	public static String userField(int fieldId)
	{
		return user_field+fieldId;
	}
	
	private static final String fld_timestamp = "ts";
	public static String timestamp(Table t)
	{
		return timestamp(t.id);
	}
	public static String timestamp(int tableId)
	{
		return fld_timestamp+tableId;
	}
	
	private static final String fld_fulltext = "ft";
	public static String fullText(Table t)
	{
		return fullText(t.id);
	}
	public static String fullText(int tableId)
	{
		return fld_fulltext+tableId;
	}
	
	private static final String fld_user = "u";
	public static String user(Table t)
	{
		return user(t.id);
	}
	public static String user(int tableId)
	{
		return fld_user+tableId;
	}
	
	private static final String fld_deleted = "d";
	public static String deleted(Table t)
	{
		return deleted(t.id);
	}
	public static String deleted(int tableId)
	{
		return fld_deleted+tableId;
	}
	
	private static final String fld_geom = "geom";
	public static String geometry(Table t)
	{
		return geometry(t.id);
	}
	public static String geometry(int tableId)
	{
		return fld_geom+tableId;
	}
	
	private static final String fld_id = "id";
	public static String id(Table t)
	{
		return id(t.id);
	}
	public static String id(int tableId)
	{
		return fld_id+tableId;
	}
	
	private static final String fld_minx = "minx";
	public static String minX(Table t)
	{
		return minX(t.id);
	}
	public static String minX(int tableId)
	{
		return fld_minx+tableId;
	}
	
	private static final String fld_maxx = "maxx";
	public static String maxX(Table t)
	{
		return maxX(t.id);
	}
	public static String maxX(int tableId)
	{
		return fld_maxx+tableId;
	}
	
	private static final String fld_miny = "miny";
	public static String minY(Table t)
	{
		return minY(t.id);
	}
	public static String minY(int tableId)
	{
		return fld_miny+tableId;
	}
	
	private static final String fld_maxy = "maxy";
	public static String maxY(Table t)
	{
		return maxY(t.id);
	}
	public static String maxY(int tableId)
	{
		return fld_maxy+tableId;
	}
	
	private static final String fld_area = "area";
	public static String area(Table t)
	{
		return area(t.id);
	}
	public static String area(int tableId)
	{
		return fld_area+tableId;
	}
	
	private static final String fld_len = "len";
	public static String length(Table t)
	{
		return length(t.id);
	}
	public static String length(int tableId)
	{
		return fld_len+tableId;
	}
	
	private static final String fld_drawing = "dwg";
	public static String drawing(Table t)
	{
		return drawing(t.id);
	}
	public static String drawing(int tableId)
	{
		return fld_drawing+tableId;
	}
	
	private static final String fld_centroidX = "cx";
	public static String centroidX(Table t)
	{
		return centroidX(t.id);
	}
	public static String centroidX(int tableId)
	{
		return fld_centroidX+tableId;
	}
	
	private static final String fld_centroidY = "cy";
	public static String centroidY(Table t)
	{
		return centroidY(t.id);
	}
	public static String centroidY(int tableId)
	{
		return fld_centroidY+tableId;
	}
	
	private static final String hfld_id = "hid";
	public static String histId(Table t)
	{
		return histId(t.id);
	}
	public static String histId(int tableId)
	{
		return hfld_id+tableId;
	}

	public static boolean isUserFieldName(String tmpname)
    {
		if (tmpname.length() <= user_field.length())
			return false;
		if (!tmpname.startsWith(user_field))
			return false;
		try {
			return Integer.parseInt(tmpname.substring(user_field.length())) > 0;
		} catch (NumberFormatException e) {
			return false;
		}
    }

	public static int getUserFieldIdFromName(String tmpname)
    {
		return Integer.parseInt(tmpname.substring(user_field.length()));
    }
}
