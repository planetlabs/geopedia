package com.sinergise.generics.core;

import static com.sinergise.common.util.lang.TypeUtil.boxI;

import java.util.ArrayList;

public class ArrayValueHolder extends ArrayList<ValueHolder> implements ValueHolder {
	private int entityTypeId = Integer.MIN_VALUE;
	protected static EntityTypeStorage etStorage; 
	
	private boolean hasMoreData = false;
	private int totalDataCount = Integer.MIN_VALUE;
	private int dataLocationStart = Integer.MIN_VALUE;
	private int dataLocationEnd = Integer.MIN_VALUE;
	
	// TODO: track add/delete changes
	protected ArrayValueHolder() {	
	}
	
	public ArrayValueHolder (EntityType entityType) {
		this.entityTypeId = entityType.getId();
	}
	public ArrayValueHolder (int entityTypeId) {
		this.entityTypeId = entityTypeId;
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2822506079453223783L;

	public int getEntityTypeId() {
		return entityTypeId;
	}
	
	public EntityType getType() {
		if (etStorage==null) throw new RuntimeException("No EntityTypeStorage instance is available!");
		return etStorage.getEntityType(boxI(entityTypeId));
	}

	public static void setEntityTypeStorage(EntityTypeStorage ets) {
		etStorage = ets;		
	}
	
	public int getTotalDataCount() {
		return totalDataCount;
	}
	public void setTotalDataCount(int count) {
		this.totalDataCount = count;
	}
	
	public boolean hasMoreData(){
		return hasMoreData;
	}
	
	public void setHasMoreData(boolean hasMore){
		this.hasMoreData = hasMore;
	}
	
	
	public void setDataLocation(int start, int end) {
		dataLocationStart = start;
		dataLocationEnd = end;
	}
	public int getDataLocationStart() {
		return dataLocationStart;
	}
	
	public int getDataLocationEnd() {
		return dataLocationEnd;
	}
	
	
	@Override
	public boolean isNull() {
		if (size()==0)
			return true;
		for (int i=0;i<size();i++) {
			ValueHolder vh = get(i);
			if (!vh.isNull())
				return false;
		}
		return true;
	}	
}
