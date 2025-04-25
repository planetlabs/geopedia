package com.sinergise.geopedia.core.entities.walk;

import java.io.Serializable;

public class TablePath implements Serializable
{
	
	private static final long serialVersionUID = 1L;
	
	public int[] tableIds;
	public int[] walkedFieldIds;
	
	public static final int[] emptyIntArray = new int[0];
	
	private transient TablePath _oneLess;
	
    public TablePath()
    {
    	// needed for serialization
    }
    
	public TablePath(int[] tableIds, int[] walkedFieldIds)
	{
		this.tableIds = tableIds;
		this.walkedFieldIds = walkedFieldIds;
	}
	
	public TablePath(int tableId)
	{
		this.tableIds = new int[] { tableId };
		this.walkedFieldIds = emptyIntArray;
	}
	
	public int lastTableId()
    {
		return tableIds[tableIds.length - 1];
    }

	public void toStringJS(StringBuffer sb) {
		if (walkedFieldIds.length == 0) {
			sb.append("$this");
			return;
		}
		
		sb.append("f");
		for (int a=0; a<walkedFieldIds.length; a++) {
			if (a > 0)
				sb.append("_f");
			sb.append(walkedFieldIds[a]);
		}
	}
	public void toString(StringBuffer sb)
    {
		if (walkedFieldIds.length == 0) {
			sb.append("$this");
			return;
		}
		
		sb.append("$f");
		for (int a=0; a<walkedFieldIds.length; a++) {
			if (a > 0)
				sb.append(".f");
			sb.append(walkedFieldIds[a]);
		}
    }
	
	public int hashCode()
	{
		int res = 1;

		res = res * 31 + tableIds[0];

		for (int a=0; a<walkedFieldIds.length; a++)
			res = res * 31 + walkedFieldIds[a];
		
		return res;
	}
	
	public boolean equals(Object obj)
	{
		if (obj instanceof TablePath) {
			TablePath other = (TablePath) obj;
			if (other.tableIds[0] != tableIds[0])
				return false;
			if (other.walkedFieldIds.length != walkedFieldIds.length)
				return false;

			for (int a=0; a<walkedFieldIds.length; a++)
				if (walkedFieldIds[a] != other.walkedFieldIds[a])
					return false;
			
			return true;
		}
		return false;
	}

	public TablePath oneLess()
    {
		if (walkedFieldIds.length == 0)
			return null;
		
		if (_oneLess != null)
			return _oneLess;
		
		if (walkedFieldIds.length == 1)
			return _oneLess = new TablePath(tableIds[0]);
		
		int[] ts = new int[tableIds.length-1];
		for (int a=0; a<ts.length; a++)
			ts[a] = tableIds[a];
		int[] fs = new int[walkedFieldIds.length-1];
		for (int a=0; a<fs.length; a++)
			fs[a] = walkedFieldIds[a];
		
		return _oneLess = new TablePath(ts, fs);
    }
}
