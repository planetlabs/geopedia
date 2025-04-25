package com.sinergise.geopedia.core.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.sinergise.geopedia.core.common.util.PagableHolder;
import com.sinergise.geopedia.core.entities.Feature;
import com.sinergise.geopedia.core.entities.Table;

public class FeaturesQueryResults extends PagableHolder<ArrayList<Feature>> {
	private static final long serialVersionUID = 9029790698208332953L;

	
	public static class UnresolvedReferencesMap {
		// TableID to HashSet of missing identifiers map 
		HashMap<Integer, HashSet<Long>> unresolvedReferencesMap;
		
		public HashMap<Integer,HashSet<Long>> getMap() {
			return unresolvedReferencesMap;
		}
		 public void addUnresolvedReference(Integer tableId, Long id) {
			 if (id==null || tableId==null) return;
			 if (unresolvedReferencesMap==null) {
				 unresolvedReferencesMap = new HashMap<Integer, HashSet<Long>>();
			 }
			 HashSet<Long> idList = unresolvedReferencesMap.get(tableId);
			 if (idList == null) {
				 idList = new HashSet<Long>();
				 unresolvedReferencesMap.put(tableId, idList);
			 }
			 idList.add(id);
		 }
		public boolean hasData() {
			if (unresolvedReferencesMap==null || unresolvedReferencesMap.size()==0)
				return false;
			return true;
		}
	}
	
	public transient UnresolvedReferencesMap unresolvedReferencesMap;
	
	public long totalCount = Long.MIN_VALUE;
	public Table table = null;
	
	
	@Deprecated
	protected FeaturesQueryResults() {		
	}
	
	public FeaturesQueryResults(ArrayList<Feature> collection, int start, int end, boolean hasMoreData) {
		super(collection, start, end, hasMoreData);
	}

	public boolean hasTotalCount() {
		return totalCount != Integer.MIN_VALUE;
	}
}
