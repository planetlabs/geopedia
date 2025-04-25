/*
 *
 */
package com.sinergise.common.gis.map.model;

import com.sinergise.common.geometry.crs.CartesianCRS;
import com.sinergise.common.geometry.display.DisplayCoordinateAdapter;
import com.sinergise.common.geometry.display.ScaleLevelsSpec;
import com.sinergise.common.gis.map.model.layer.LayerTreeElement;
import com.sinergise.common.gis.map.model.layer.MapContextLayers;
import com.sinergise.common.gis.map.model.layer.system.SelectionSetLayer;
import com.sinergise.common.util.collections.tree.SourcesTreeEvents;
import com.sinergise.common.util.collections.tree.TreeListener;
import com.sinergise.common.util.collections.tree.TreeListenerAdapter;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.geom.MapViewSpec;
import com.sinergise.common.util.geom.WindowRequest;
import com.sinergise.common.util.state.gwt.PropertyChangeListener;
import com.sinergise.common.util.state.gwt.PropertyMap;
import com.sinergise.common.util.state.gwt.SourcesPropertyChangeEvents;


public class MapViewContext implements SourcesPropertyChangeEvents<Object>, SourcesTreeEvents<LayerTreeElement> {
    public static final String PROP_ACTIVE_LAYER = "activeLayer";
    public final DisplayCoordinateAdapter coords;
    public final MapContextLayers layers;
    protected final PropertyMap properties;
    public LayerTreeElement activeLayer;
    public SelectionSetLayer defaultHighlightLayer;
    
    private WindowRequest initialView;
    
    public MapViewContext(CartesianCRS worldCRS) {
    	this(worldCRS, null);
    }

    public MapViewContext(CartesianCRS worldCRS, ScaleLevelsSpec zooms) {
    		this.coords=new DisplayCoordinateAdapter(worldCRS);
		setPreferredZoomLevels(zooms);
        properties=new PropertyMap(this);
        layers=new MapContextLayers();
        layers.addTreeListener(new TreeListenerAdapter<LayerTreeElement>() {
        	private boolean setting=false;
        	
        	@Override
			public void nodeAdded(LayerTreeElement parent, LayerTreeElement added, int newIndex) {
        		if (defaultHighlightLayer==null && (added instanceof SelectionSetLayer)) {
        			defaultHighlightLayer=(SelectionSetLayer)added;
        		}
        	}
        	
			@Override
			public void nodeRemoved(LayerTreeElement parent, LayerTreeElement removed, int oldIndex) {
				if (removed==activeLayer) {
					setActiveLayer(null);
				}
			}
			@Override
			public void nodeChanged(LayerTreeElement chNode, String propertyName) {
				if (propertyName==LayerTreeElement.PROP_ACTIVE) {
						if (chNode.isActive() && chNode!=activeLayer) {
							setting=true;
							try {
								setActiveLayer(chNode);
							} finally {
								setting=false;
							}
						} else if (!setting && chNode==activeLayer && !chNode.isActive()) {
							setActiveLayer(null);
						}
				}
			}
		});
    }
    
	public void setPreferredZoomLevels(ScaleLevelsSpec zooms) {
		if (zooms != null) {
			this.coords.setPreferredZoomLevels(zooms);
			this.coords.setAllowNonPreferredZooms(false);
		}
	}

	@Override
	public void addPropertyChangeListener(PropertyChangeListener<Object> listener) {
        properties.addPropertyChangeListener(listener);
    }
    
    @Override
	public void removePropertyChangeListener(PropertyChangeListener<Object> listener) {
        properties.removePropertyChangeListener(listener);
    }
    
    public void setProperty(String name, Object value) {
        properties.set(name, value);
    }

    @Override
	public boolean addTreeListener(TreeListener<LayerTreeElement> listener) {
        return layers.addTreeListener(listener);
    }
    @Override
	public boolean removeTreeListener(TreeListener<LayerTreeElement> listener) {
        return layers.removeTreeListener(listener);
    }

    public LayerTreeElement getActiveLayer() {
        return activeLayer;
    }
    
    public void setActiveLayer(LayerTreeElement activeLayer) {
    	LayerTreeElement old=this.activeLayer;
    	if (old == activeLayer) return;
    	if (old!=null) old.setActive(false);
        this.activeLayer = activeLayer;
        if (activeLayer!=null) activeLayer.setActive(true);
        properties.fireChange(PROP_ACTIVE_LAYER, old, activeLayer);
    }

    public void setInitialView(MapViewSpec spec) {
    	setInitialView(spec.worldCenterX, spec.worldCenterY, spec.worldLenPerDisp);
    }
    
    public void setInitialView(double worldCenterX, double worldCenterY, double scale) {
        initialView = new WindowRequest(worldCenterX, worldCenterY, scale);
    }
    
    public void setInitialView(Envelope mbr) {
        initialView = new WindowRequest(mbr);
    }
    
    public boolean hasInitialView() {
    	return initialView != null;
    }
    
    public void applyInitialView() {
    	double cX = Double.NaN;
    	double cY = Double.NaN;
    	double sc = 0;
    	
    	if (initialView != null) {
    		Envelope env = initialView.getEnvelope();
    		cX = env.getCenterX();
    		cY = env.getCenterY();
    		sc  = initialView.getScale();
    		if (Double.isNaN(sc)) sc = coords.scaleForEnvelope(env.getMinX(), env.getMinY(), env.getMaxX(), env.getMaxY());
    	} else {
    		Envelope mbr=null;
    		
    		if (coords.bounds != null) {
    			mbr=coords.bounds.mbr;
    		}
    		
    		if (Envelope.isNullOrEmpty(mbr)) {
    			mbr=coords.worldCRS.bounds2D;
    		}
    		
			if (Double.isInfinite(sc) || !(sc > 0)) { //This will handle NaN
				sc=coords.scaleForEnvelope(mbr.getMinX(), mbr.getMinY(), mbr.getMaxX(), mbr.getMaxY());
			}
    		if (mbr!=null && !mbr.isEmpty()) {
    			cX=mbr.getCenterX();
    			cY=mbr.getCenterY();
    		}
    	}
    	
		coords.setWorldCenterAndScale(cX, cY, coords.getPreferredScaleUp(sc));
    }

	public void invalidateLayers() {
		layers.getRoot().setDirty();
	}
}
