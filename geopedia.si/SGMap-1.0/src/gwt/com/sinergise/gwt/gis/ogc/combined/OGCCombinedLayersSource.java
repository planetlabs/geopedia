package com.sinergise.gwt.gis.ogc.combined;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sinergise.common.gis.map.model.layer.Layer;
import com.sinergise.common.gis.map.model.layer.LayerSpec;
import com.sinergise.common.gis.map.model.layer.LayersSource;
import com.sinergise.common.util.naming.Identifier;
import com.sinergise.common.util.state.gwt.StateGWT;
import com.sinergise.gwt.gis.ogc.wfs.WFSFeatureSource;
import com.sinergise.gwt.gis.ogc.wms.WMSLayerSpec;
import com.sinergise.gwt.gis.ogc.wms.WMSLayersSource;


public class OGCCombinedLayersSource extends WMSLayersSource {
	protected WFSFeatureSource wfsSource;
	public OGCCombinedLayersSource(String id, String wmsURL, String wfsURL) {
		super(id, wmsURL);
		wfsSource = createWFSSource(wfsURL);
	}
	
	protected WFSFeatureSource createWFSSource(String wfsURL) {
		return new WFSFeatureSource(wfsURL, this);
	}
	
	@Override
	public <T extends LayersSource> void asyncInitialize(final AsyncCallback<? super T> cb) {
		wfsSource.asyncInitialize(new AsyncCallback<WFSFeatureSource>() {
			@Override
			public void onFailure(Throwable caught) {
				cb.onFailure(caught);
			}

			@SuppressWarnings("unchecked")
			@Override
			public void onSuccess(WFSFeatureSource result) {
				cb.onSuccess((T)OGCCombinedLayersSource.this);
			}
		});
	}
	
	@Override
	public OGCCombinedLayer createLayer(String name) {
		return (OGCCombinedLayer)super.createLayer(name);
	}

	public WFSFeatureSource getFeatureDataSource() {
		return wfsSource;
	}
	@Override
	public Identifier getFeaturesSourceId() {
		return wfsSource.getQualifiedID();
	}
	
	@Override
	public Layer createLayer(LayerSpec spec) {
		return new OGCCombinedLayer((WMSLayerSpec) spec);
	}
	@Override
	public WMSLayerSpec createLayerSpec(StateGWT st) {
		return new OGCCombinedLayerSpec(this, st);
	}
	@Override
	public WMSLayerSpec createLayerSpec(String layerId) {
		return new OGCCombinedLayerSpec(this, layerId);
	}

	public String[] getDataLayerIDs() {
		ArrayList<String> ret = new ArrayList<String>();
		for (WMSLayerSpec spec : mySpecs.values()) {
			if (spec instanceof OGCCombinedLayerSpec) {
				ret.add(spec.getLocalID());
			}
		}
		return ret.toArray(new String[ret.size()]);
	}
}
