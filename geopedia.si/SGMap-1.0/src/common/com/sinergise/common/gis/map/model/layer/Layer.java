/*
 *
 */
package com.sinergise.common.gis.map.model.layer;

import com.sinergise.common.geometry.display.DisplayBounds;
import com.sinergise.common.gis.map.model.style.Style;
import com.sinergise.common.util.state.gwt.PropertyChangeListener;


public abstract class Layer extends LayerTreeElement {
    
    protected LayerSpec sourceLayerSpec;
    
    protected Style styleSettings;
    protected DisplayBounds.Disp bounds = null;
    
    public Layer(LayerSpec spec) {
    	this(spec.getLocalID(), spec);
    }
    public Layer(String layerId, LayerSpec spec) {
    	super(layerId);
        this.sourceLayerSpec=spec;
    }
    
    @Override
	public boolean isRenderable() {
        return true;
    }

    @Override
	public String toString() {
        return getTitle();
    }
    
    @Override
	public DisplayBounds.Disp getBounds() {
    	if (bounds == null) {
    		bounds = new DisplayBounds.Disp(sourceLayerSpec.getBoundingBox());
    	}
        return bounds;
    }
    
    @Override
	public String getTitle() {
        if (title==null) return getLocalID();
        return title;
    }

    public LayerSpec getSpec() {
        return sourceLayerSpec;
    }
    
    public LayersSource getSource() {
        if (sourceLayerSpec==null) return null;
        return sourceLayerSpec.getSource();
    }

    public Style getStyle() {
        return styleSettings;
    }

    protected transient PropertyChangeListener<Object> styleListener=new PropertyChangeListener<Object>() {
		@Override
		public void propertyChange(Object sender, String propertyName,
				Object oldValue, Object newValue) 
		{
			notifyChange(PROP_STYLE+"."+propertyName);
		}
	};
    public static final String PROP_STYLE="style";    
    public Layer setStyle(Style style) {
    	if (styleSettings!=null) styleSettings.removePropertyChangeListener(styleListener);
        styleSettings=style;
        if (styleSettings!=null) styleSettings.addPropertyChangeListener(styleListener);
        notifyChange(PROP_STYLE);
        return this;
    }
    
    @Override
    public Layer setOn(boolean on) {
    	super.setOn(on);
    	return this;
    }
}
