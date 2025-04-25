package com.sinergise.common.gis.ogc.wms.response;

import java.util.Collection;
import java.util.Iterator;

import com.sinergise.common.gis.feature.RepresentsFeature;
import com.sinergise.common.gis.map.model.MapViewContext;
import com.sinergise.common.gis.map.model.layer.info.FeatureInfoCollection;
import com.sinergise.common.gis.map.model.layer.info.FeatureInfoItem;
import com.sinergise.common.gis.map.model.layer.info.MultipleFeatureCollection;
import com.sinergise.common.gis.map.ui.info.FeatureInfoUtil;
import com.sinergise.common.gis.ogc.wfs.response.WFSGetFeatureResponse;
import com.sinergise.common.util.web.MimeType;


public class DefaultFeatureInfoResponse implements WMSFeatureInfoResponse, WFSGetFeatureResponse {
	private static final long serialVersionUID = 1L;

	public static final DefaultFeatureInfoResponse createFrom(FeatureInfoCollection collection, boolean hasData) {
		if (collection==null) return null;
		if (hasData) {
			return new DefaultFeatureInfoResponse(collection.getAll(), collection.getHitCount());
		}
		return new DefaultFeatureInfoResponse(collection.getItemCount());
	}
	public static final DefaultFeatureInfoResponse createFrom(WFSGetFeatureResponse resp) {
		return createFrom(resp, resp.hasData());
	}
	private MultipleFeatureCollection results;
	private int hits;
	
	/**
	 * @deprecated serialization only
	 */
	@Deprecated
	public DefaultFeatureInfoResponse() {
	}
	
	public DefaultFeatureInfoResponse(Collection<FeatureInfoItem> results) {
		this(results,results==null?0:results.size());
	}
	
	public DefaultFeatureInfoResponse(Collection<FeatureInfoItem> results, int hits) {
		this.results=new MultipleFeatureCollection();
		this.results.addAll(results);
		this.hits = hits;
		clearTransient();
	}
	
	public DefaultFeatureInfoResponse(int hits) {
		this.results = null;
		this.hits = hits;
	}

	@Override
	public Iterator<FeatureInfoItem> iterator() {
		return results.iterator();
	}
	
	protected void clearTransient() {
		if (results==null) return;
		for (FeatureInfoItem itm : results) {
			itm.layer=null;
			itm.f.getIdentifier().getParent().unbind();
		}
	}
	
	@Override
	public boolean hasData() {
		return results!=null;
	}
	
	@Override
	public void updateTransient(MapViewContext context) {
		FeatureInfoUtil.updateTransient(results, context);
	}
	
	@Override
	public int getItemCount() {
		return results==null?0:results.getItemCount();
	}
	
	@Override
	public int getHitCount() {
		return hits;
	}

	@Override
	public Collection<FeatureInfoItem> getAll() {
		return results.getAll();
	}
	
	@Override
	public Collection<? extends RepresentsFeature> getFeatures() {
		return getAll();
	}
	
	@Override
	public FeatureInfoItem getItem(int i) {
		return results.getItem(i);
	}

	@Override
	public MimeType getMimeType() {
		return FeatureInfoCollection.MIME_OBJECT_FEATURE_INFO_COLLECTION;
	}

}
