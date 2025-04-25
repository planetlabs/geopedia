/*
 *
 */
package com.sinergise.common.gis.ogc.wms;

import java.util.ArrayList;
import java.util.List;

import com.sinergise.common.gis.feature.descriptor.CFeatureDescriptor;
import com.sinergise.common.gis.map.model.layer.LayerSpec;
import com.sinergise.common.gis.ogc.base.OGCLayersSource;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.naming.Identifier;
import com.sinergise.common.util.state.gwt.StateGWT;

public class AbstractWmsLayerSpec extends LayerSpec {
	
	private static final long serialVersionUID = 1L;
	
    /**
     * Longer description of the layer
     */
	public static final String PROP_ABSTRACT = "Abstract";

    public static final String PROP_TITLE = "title";
    public static final String PROP_NAME = "name";
    public static final String PROP_QUERYABLE = "queryable";
    public static final String PROP_OPAQUE = "opaque";
    
    public static final String PROP_BOUNDINGBOX = "BoundingBox";
    public static final String PROP_LASTMODIFIED = "lastModified";
    
    
    protected StateGWT sourceSt;
    private Envelope bb;
	protected CFeatureDescriptor descriptor = null;

	List<AbstractWmsLayerSpec> children;
	
    @Deprecated
    public AbstractWmsLayerSpec() {
    	super();
	}
    
    /**
	 * @param src
	 * @param name
	 */
	public AbstractWmsLayerSpec(OGCLayersSource src, String name) {
		super(src, name);
        sourceSt=new StateGWT();
        sourceSt.putString(PROP_NAME, name);
	}
	
	@Override
	public List<AbstractWmsLayerSpec> getChildren() {
		return children;
	}
	
	public void addChild(AbstractWmsLayerSpec layer) {
		if (children == null) {
			children = new ArrayList<AbstractWmsLayerSpec>();
		}
		// Replace if we already have the child
		for (int i = 0; i < children.size(); i++) {
			AbstractWmsLayerSpec child = children.get(i);
			if (child.getLocalID().equals(layer.getLocalID())) {
				children.set(i, layer);
				return;
			}
		}

		// Otherwise just add
		children.add(layer);
	}
	
    public boolean isOpaque() {
        return sourceSt.getBoolean(PROP_OPAQUE, false);
    }
    
    public void setOpaque(boolean opaque) {
        sourceSt.putBoolean(PROP_OPAQUE, opaque);
    }
    
	public boolean isQueryable() {
		return sourceSt.getBoolean(PROP_QUERYABLE, false);
	}

	public void setQueryable(boolean queryable) {
		sourceSt.putBoolean(PROP_QUERYABLE, queryable);
	}

    public void setName(String name) {
        sourceSt.putString(PROP_NAME, name);
        layerSpecID=new Identifier(source.getQualifiedID(), name);
    }
    
    public String getTitle() {
        String ret = sourceSt.getString(PROP_TITLE, null);
        if (ret!=null) return ret;
        return getLocalID();
    }
	
	@Override
	public Envelope getBoundingBox() {
		if (bb==null) {
			StateGWT bbState = sourceSt.getState(PROP_BOUNDINGBOX);
			if (bbState != null) {
				bb = new Envelope(bbState);
			}
		}
		return bb;
	}
	
	public void setBoundingBox(Envelope bb) {
		this.bb = bb;
	}
	
	public void setProperty(String propName, String value) {
		sourceSt.putString(propName, value);
	}
	
	public String getProperty(String propName, String defaultValue) {
		return sourceSt.getString(propName, defaultValue);
	}
	
	public CFeatureDescriptor getDescriptor() {
		return descriptor;
	}

	public void setFrom(AbstractWmsLayerSpec other) {
		super.setFrom(other);
		bb = other.getBoundingBox();
		descriptor = other.descriptor;
		sourceSt.setFrom(other.sourceSt, false);
	}
	
	@Override
	public OGCLayersSource getSource() {
		return (OGCLayersSource)super.getSource();
	}
	
	public long getLastChanged() {
		return sourceSt.getLong(PROP_LASTMODIFIED, 0);
	}
	
	public void setLastModified(long lastModified) {
		sourceSt.putLong(PROP_LASTMODIFIED, lastModified);
	}

}

