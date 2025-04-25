/*
 *
 */
package com.sinergise.gwt.gis.ogc.wms;

import com.sinergise.common.gis.map.model.layer.Layer;
import com.sinergise.common.gis.map.model.layer.LayerTreeElement;
import com.sinergise.common.gis.map.model.layer.info.FeatureInfoSource;
import com.sinergise.common.gis.map.model.style.NamedStyle;
import com.sinergise.common.gis.ogc.wms.WMSLayerElement;
import com.sinergise.common.gis.ogc.wms.request.StyleEncoderFactory;

/** 
 * Holds data about layers to be used by client-side GUI for remote access & rendering.
 * 
 * @author mkadunc; 
 */
public class WMSLayer extends Layer implements WMSLayerElement {
	/**
     * @param source
     * @param name
     */
    public WMSLayer(WMSLayerSpec source) {
        super(source);
        setTitle(source.getTitle());
        setOpaque(source.isOpaque());
    }
    
    public String getRenderingGroup() {
    	return getGenericProperty(PROP_RENDERING_GROUP, INHERITANCE_CHILD_OVERRIDES);
    }
    
    public WMSLayer setRenderingGroup(String rendGroup) {
    	return (WMSLayer)setGenericProperty(PROP_RENDERING_GROUP, rendGroup);
    }
    
    public void setStyleName(String styleName) {
        styleSettings=new NamedStyle(styleName);
    }
    
    @Override
	public int indexOf(LayerTreeElement child) {
        return -1;
    }
    
    @Override
	public boolean isFeatureInfoEnabled() {
        return !Boolean.FALSE.toString().equals(getGenericProperty(PROP_FEATURE_INFO_ENABLED, INHERITANCE_CHILD_OVERRIDES));
    }
    
    /* (non-Javadoc)
     * @see com.sinergise.gis.client.map.model.layers.Layer#isOpaque()
     */
    @Override
	public boolean isOpaque() {
        return (getSpec()).isOpaque();
    }
    
    @Override
	public FeatureInfoSource getFeaturesSource() {
        return getSource();
    }
    
    public boolean isParentFeatureInfoEnabled() {
        return WMSLayersSource.isParentFeatureInfoEnabled(this);
    }
    
    @Override
	public String getWMSName() {
        return getSpec().getLocalID();
    }
    
    @Override
	public String getWMSStyleName() {
        return StyleEncoderFactory.encodeStyle(getStyle());
    }
    
    @Override
    public WMSLayersSource getSource() {
    	return (WMSLayersSource)super.getSource();
    }
    
    @Override
	public long getLastModified() {
    	return ((WMSLayerSpec)sourceLayerSpec).getLastChanged();
    }
    @Override
	public void setDirty() {
   		((WMSLayerSpec)sourceLayerSpec).setLastModified(System.currentTimeMillis());
   		getModel().nodeChanged(this, PROP_LAST_MODIFIED);
    }
    
    @Override
    public WMSLayerSpec getSpec() {
    	return (WMSLayerSpec)super.getSpec();
    }
    

	public WMSLayer setOpaque(boolean b) {
		((WMSLayerSpec)sourceLayerSpec).setOpaque(b);
		return this;
	}

	public WMSLayer setFeatureInfoEnabled(boolean enabled) {
		return (WMSLayer)setGenericProperty(PROP_FEATURE_INFO_ENABLED, Boolean.toString(enabled));
	}

	public int getSupportedRenderModes() {
		String ret = getGenericProperty(PROP_SUPPORTED_RENDER_MODES, INHERITANCE_CHILD_OVERRIDES);
		if (ret == null) {
			return RENDER_MODE_TILES | RENDER_MODE_UNTILED;
		}
		return Integer.parseInt(ret);
	}
	
	public boolean isTilesEnabled() {
		return (getSupportedRenderModes() & RENDER_MODE_TILES) == RENDER_MODE_TILES;
	}

	public boolean isUntiledEnabled() {
		return (getSupportedRenderModes() & RENDER_MODE_UNTILED) == RENDER_MODE_UNTILED;
	}
	
	public WMSLayer setSupportedRenderModes(int renderModes) {
		return (WMSLayer)setGenericProperty(PROP_SUPPORTED_RENDER_MODES, Integer.toString(renderModes));
	}
    
	/* (non-Javadoc)
	 * @see com.sinergise.gis.client.map.model.layers.LayerTreeElement#getLocalID()
	 */
	@Override
	public String getLocalID() {
		//XXX: This is weird; a layer should be able to have a different localID than its spec
		// (to support more than one client layer linking to the same WMS layer) 
		//WMS client implementation should make sure that it uses spec's ID when communicating with the server
		return getSpec().getLocalID();
	}

	public static void setRenderingGroup(LayerTreeElement node, String group) {
		node.setGenericProperty(PROP_RENDERING_GROUP, group);
	}
}
