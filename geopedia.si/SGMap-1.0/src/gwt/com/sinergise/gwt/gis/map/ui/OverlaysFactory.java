/*
 *
 */
package com.sinergise.gwt.gis.map.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.common.gis.geopedia.GeopediaLayersSource;
import com.sinergise.common.gis.map.model.layer.Layer;
import com.sinergise.common.gis.map.model.layer.system.SystemLayersSource;
import com.sinergise.gwt.gis.geopedia.GeopediaOverlaysProvider;
import com.sinergise.gwt.gis.map.ui.overlays.SystemOverlaysProvider;


public class OverlaysFactory {
    public static final OverlaysFactory INSTANCE=new OverlaysFactory();
    public static interface FactoryListener {
    	void providerRegistered(String sourceType, Object provider);
    	void providerDeregistered(String sourceType, Object provider);
    }
    private HashMap<String, OverlayProvider> providers=new HashMap<String, OverlayProvider>();
    
    private ArrayList<FactoryListener> listeners;
    
    public OverlaysFactory() {
    	registerProvider(SystemLayersSource.INSTANCE.getTypeIdentifier(), new SystemOverlaysProvider());
	}
    
    public void addFactoryListener(FactoryListener l) {
    	if (listeners==null) listeners=new ArrayList<FactoryListener>();
    	listeners.add(l);
    }
    public void removeFactoryListener(FactoryListener l) {
    	if (listeners==null) return;
    	listeners.remove(l);
    }
    protected void fireFactoryEvent(Object provider, String sourceType, boolean registered) {
    	if (listeners==null) return;
    	for (int i = listeners.size()-1; i >= 0; i--) {
    		if (registered) listeners.get(i).providerRegistered(sourceType, provider);
    		else listeners.get(i).providerDeregistered(sourceType, provider);
		}
    }
    
    public Set<OverlayProvider> getProviders() {
    	return new HashSet<OverlayProvider>(providers.values());
    }
    
    public void registerProvider(String sourceType, OverlayProvider sourceProvider) {
        providers.put(sourceType, sourceProvider);
        fireFactoryEvent(sourceProvider, sourceType, true);
    }
    
    public void deregisterProvider(String sourceType) {
        Object prov=providers.remove(sourceType);
        fireFactoryEvent(prov, sourceType, false);
    }
    
    public OverlayComponent<?> addLayer(CRS mapCRS, OverlayComponent<?> previous, Layer lyr) {
        if (lyr.getSource() == null) {
        	return previous;
        }
        String sourceType = lyr.getSource().getTypeIdentifier();
		OverlayProvider provider = providers.get(sourceType);
		if (provider == null) {
			provider = registerDefaultProvider(sourceType, lyr);
		}
        if (provider == null) {
        	return previous;
        }
        return provider.addLayer(mapCRS, previous, lyr);
    }

	/**
	 * @param lyr The layer which is being added; might be used to determine parameters for construction of the overlay provider  
	 */
	protected OverlayProvider registerDefaultProvider(String sourceType, Layer lyr) {
		if (GeopediaLayersSource.SOURCE_TYPE_GP.equals(sourceType)) {
			registerProvider(GeopediaLayersSource.SOURCE_TYPE_GP, GeopediaOverlaysProvider.INSTANCE);
			return GeopediaOverlaysProvider.INSTANCE;
		}
		return null;
	}
}
