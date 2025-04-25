package com.sinergise.gwt.gis.ogc.combined;

import com.sinergise.common.gis.feature.descriptor.CFeatureDescriptor;
import com.sinergise.common.gis.filter.FilterCapabilities;
import com.sinergise.common.gis.map.model.layer.FeatureDataLayer;
import com.sinergise.gwt.gis.ogc.wfs.WFSFeatureSource;
import com.sinergise.gwt.gis.ogc.wms.WMSLayer;
import com.sinergise.gwt.gis.ogc.wms.WMSLayerSpec;


public class OGCCombinedLayer extends WMSLayer implements FeatureDataLayer {
	public OGCCombinedLayer(WMSLayerSpec spec) {
		super(spec);
	}
	
	@Override
	public WFSFeatureSource getFeaturesSource() {
		return ((OGCCombinedLayerSpec)sourceLayerSpec).getFeaturesSource();
	}
	
	@Override
	public void appendFilterCapabilities(FilterCapabilities filterCaps) {
		((OGCCombinedLayerSpec)sourceLayerSpec).appendFilterCapabilities(filterCaps);
	}
	
	@Override
	public FilterCapabilities getFilterCapabilities() {
		return ((OGCCombinedLayerSpec)sourceLayerSpec).getFilterCapabilities();
	}
	
	@Override
	public boolean isFeatureDataQueryEnabled(FilterCapabilities filterCaps) {
		return ((OGCCombinedLayerSpec)sourceLayerSpec).isFeatureDataQueryEnabled(filterCaps);
	}
	
	//TODO: WMS / SLD specification indicates that a single WMS layer may actually contain/render more than one feature type
	@Override
	public String getFeatureTypeName() {
		return ((OGCCombinedLayerSpec)sourceLayerSpec).getFeatureTypeName();
	}

	@Override
	public CFeatureDescriptor getDescriptor() {
		return ((WMSLayerSpec) sourceLayerSpec).getDescriptor();
	}
	
	@Override
	public boolean isEditable() {
		return ((OGCCombinedLayerSpec)sourceLayerSpec).isEditable();
	}
	
	@Override
	public int getTopoType() {
		return ((OGCCombinedLayerSpec)sourceLayerSpec).getTopoType();
	}
	
}
