package com.sinergise.geopedia.app;

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;

class SysTableInfo
{
	public final int tableId;
	private final Object2IntArrayMap<String> fieldIds = new Object2IntArrayMap<String>();
	
	public SysTableInfo(int tableId)
	{
		this.tableId = tableId;
		fieldIds.defaultReturnValue(-1);
	}
	
	void setFieldId(String key, int fieldId)
	{
		fieldIds.put(key, fieldId);
	}
	
	public int getFieldId(String key)
	{
		return fieldIds.get(key);
	}
}
