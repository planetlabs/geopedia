package com.sinergise.gwt.gis.ogc.combined;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sinergise.common.gis.feature.CFeatureDataSource;
import com.sinergise.common.gis.feature.FeatureAccessException;
import com.sinergise.common.gis.feature.descriptor.CFeatureDescriptor;
import com.sinergise.common.gis.filter.FilterCapabilities;
import com.sinergise.common.gis.map.model.layer.FeatureDataLayer;
import com.sinergise.common.gis.map.model.layer.info.FeatureInfoSource;
import com.sinergise.common.util.state.gwt.StateGWT;
import com.sinergise.gwt.gis.ogc.wfs.WFSFeatureSource;
import com.sinergise.gwt.gis.ogc.wms.WMSLayerSpec;


public class OGCCombinedLayerSpec extends WMSLayerSpec implements FeatureDataLayer {
	private static final long serialVersionUID = 1L;

	protected static final String PROP_FEATURE_TYPE_NAME = "FeatureTypeName";
	protected static final String PROP_EDITABLE = "editable";
	protected static final String PROP_TOPO_TYPE = "topoType";
	
	protected FilterCapabilities filterCapabilities = new FilterCapabilities();
	
    /**
     * @deprecated serialization only
     */
    @Deprecated
	public OGCCombinedLayerSpec() {
    	super();
	}
	
	public OGCCombinedLayerSpec(OGCCombinedLayersSource src, String name) {
		super(src, name);
	}
	
	public OGCCombinedLayerSpec(OGCCombinedLayersSource src, StateGWT st) {
		super(src, st);
	}

	@Override
	public WFSFeatureSource getFeaturesSource() {
		return ((OGCCombinedLayersSource)source).getFeatureDataSource();
	}
	
	public void updateDescriptor(final AsyncCallback<CFeatureDescriptor> cb) {
		getFeaturesSource().getDescriptor(new String[] { getLocalID() }, new CFeatureDataSource.FeatureDescriptorCallback() {
			@Override
			public void onError(FeatureAccessException error) {
				cb.onFailure(error);
			}

			@Override
			public void onSuccess(CFeatureDescriptor[] result) {
				OGCCombinedLayerSpec.this.setDescriptor(result[0]);
				cb.onSuccess(result[0]);
			}
		});
	}
	
	@Override
	protected void setDescriptor(CFeatureDescriptor newDescriptor) {
		super.setDescriptor(newDescriptor);
	}

	@Override
	public void appendFilterCapabilities(FilterCapabilities filterCaps) {
		this.filterCapabilities.addCapability(filterCaps);
	}
	
	@Override
	public FilterCapabilities getFilterCapabilities() {
		return filterCapabilities;
	}
	
	@Override
	public boolean isFeatureDataQueryEnabled(FilterCapabilities filterCaps) {
		return this.filterCapabilities.supports(filterCaps);
	}
	
	//TODO: WMS / SLD specification indicates that a single WMS layer may actually contain/render more than one feature type
	@Override
	public String getFeatureTypeName() {
		return getProperty(PROP_FEATURE_TYPE_NAME, getLocalID());
	}
	
	public void setFeatureTypeName(String featureTypeName) {
		setProperty(PROP_FEATURE_TYPE_NAME, featureTypeName);
	}
	
	public boolean isFeatureInfoEnabled() {
		return true;
	}

	public FeatureInfoSource getFeatureInfoSource() {
		return getSource();
	}
	
	@Override
	public boolean isEditable() {
		return sourceSt.getBoolean(PROP_EDITABLE, false);
	}
	
	@Override
	public int getTopoType() {
		return sourceSt.getInt(PROP_TOPO_TYPE, FeatureDataLayer.TYPE_UNKNOWN);
	}
}
