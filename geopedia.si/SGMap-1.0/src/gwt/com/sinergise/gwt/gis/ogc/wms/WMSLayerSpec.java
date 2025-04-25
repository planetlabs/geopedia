/*
 *
 */
package com.sinergise.gwt.gis.ogc.wms;

import com.sinergise.common.gis.feature.descriptor.CFeatureDescriptor;
import com.sinergise.common.gis.ogc.wms.AbstractWmsLayerSpec;
import com.sinergise.common.util.state.gwt.StateGWT;

public class WMSLayerSpec extends AbstractWmsLayerSpec {
	private static final long serialVersionUID = 2L;
	
    /**
     * @deprecated serialization only
     */
    @Deprecated
    protected WMSLayerSpec() {
    	super();
    }
    
    protected WMSLayerSpec(WMSLayersSource src, String name) {
        super(src, name);
		src.mySpecs.put(getLocalID(), this);
    }
    
	protected WMSLayerSpec(WMSLayersSource src, StateGWT properties) {
        this(src, properties.getString(PROP_NAME, null));
        sourceSt.setFrom(properties, false);
    }

    @Override
    public WMSLayersSource getSource() {
    	return (WMSLayersSource)super.getSource();
    }
    
	protected void setDescriptor(CFeatureDescriptor d) {
		descriptor = d;
	}
}

