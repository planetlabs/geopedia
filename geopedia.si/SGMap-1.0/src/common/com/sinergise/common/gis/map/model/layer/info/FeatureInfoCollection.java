package com.sinergise.common.gis.map.model.layer.info;


import java.io.Serializable;
import java.util.Collection;

import com.sinergise.common.gis.feature.HasFeatureRepresentations;
import com.sinergise.common.gis.map.model.MapViewContext;
import com.sinergise.common.util.web.MimeType;


public interface FeatureInfoCollection extends Serializable, Iterable<FeatureInfoItem>, HasFeatureRepresentations {
	/**
     * This interface's mime
     */
	public static final MimeType MIME_OBJECT_FEATURE_INFO_COLLECTION = MimeType.getObjectMime(FeatureInfoCollection.class);
	public int getItemCount();
	/**
	 * Number of hits can be higher than number of items in this collection, 
	 * as request can limit the number of returned results.
	 * @return Number of hits returned by query. 
	 */
	public int getHitCount();
	public Collection<FeatureInfoItem> getAll();
	public FeatureInfoItem getItem(int i);
	public void updateTransient(MapViewContext context);
}
