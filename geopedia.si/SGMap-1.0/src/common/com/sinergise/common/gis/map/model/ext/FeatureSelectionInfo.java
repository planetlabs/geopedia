package com.sinergise.common.gis.map.model.ext;

import static com.sinergise.common.util.ArrayUtil.arraycopy;
import static com.sinergise.common.util.ArrayUtil.isNullOrEmpty;

import java.io.Serializable;

import com.sinergise.common.gis.feature.CFeatureIdentifier;
import com.sinergise.common.gis.query.Query;

public class FeatureSelectionInfo implements Serializable {
	
	private CFeatureIdentifier[] selectedIds;
	private Query[] queries;
	
	@Deprecated /** Serialization only */
	protected FeatureSelectionInfo() { }
	
	public FeatureSelectionInfo(CFeatureIdentifier ...selection) {
		this.selectedIds = arraycopy(selection, new CFeatureIdentifier[selection.length]);
	}
	
	public FeatureSelectionInfo(Query ...queries) {
		this.queries = arraycopy(queries, new Query[queries.length]);
	}
	
	public CFeatureIdentifier[] getSelectedIds() {
		if (selectedIds != null) {
			return  arraycopy(selectedIds, new CFeatureIdentifier[selectedIds.length]);
		}
		return selectedIds;
	}
	
	public boolean hasQueries() {
		return !isNullOrEmpty(queries);
	}
	
	public Query[] getQueries() {
		if (queries != null) {
			return arraycopy(queries, new Query[queries.length]);
		}
		return queries;
	}
	
}
