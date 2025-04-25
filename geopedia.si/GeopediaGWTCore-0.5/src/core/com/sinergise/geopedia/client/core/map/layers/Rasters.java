package com.sinergise.geopedia.client.core.map.layers;

import java.util.ArrayList;

import com.sinergise.geopedia.client.core.ClientGlobals;
import com.sinergise.geopedia.client.core.ClientSession;
import com.sinergise.geopedia.client.core.events.ClientSessionEvent;
import com.sinergise.geopedia.core.config.Configuration;
import com.sinergise.geopedia.core.config.ThemeBaseLayers;
import com.sinergise.geopedia.core.entities.Permissions;
import com.sinergise.geopedia.core.entities.baselayers.BaseLayer;

public class Rasters
{
	public interface Listener {
		public void rastersChanged(boolean justOnOff);
    }
	
	private ArrayList<Listener> listeners = new ArrayList<Listener>();
	private Configuration conf;
	private ThemeBaseLayers activeThemeBaseLayers;
	private BaseLayer[][] activeBaseLayers;
	
	private BaseLayer selectedBaseLayer = null;
	
	public void addListener(Listener l) {
		listeners.add(l);
	}
	
	
	
	public Rasters()
	{
		this.conf = ClientGlobals.configuration;
		if (conf == null)
			throw new IllegalStateException();
		this.activeThemeBaseLayers = conf.defaultBaseLayers;
		updateBaseLayers();
	
		ClientSessionEvent.register(ClientGlobals.eventBus, new ClientSessionEvent.Handler() {

			@Override
			public void onAutoLoginEvent(ClientSessionEvent event) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onLogin(ClientSessionEvent event) {
				updateBaseLayers();
				
			}

			@Override
			public void onLogout(ClientSessionEvent event) {
				updateBaseLayers();
				
			}
			@Override
			public void onSessionChanged(ClientSessionEvent event) {
			}
			
		});
		
	}
	
	private void updateBaseLayers() {
		if (activeThemeBaseLayers==null)
			return;
		String config = ThemeBaseLayers.SETTING_DEFAULT;		
		if (ClientSession.canPerformOperation(Permissions.PERM_PEDIAPRO)) {
			config = ThemeBaseLayers.SETTING_PRO;
		}
		Integer[][] dsIDs = activeThemeBaseLayers.getBaseLayer(config);
		
		BaseLayer[][] tmpDS = new BaseLayer[dsIDs.length][];
		for (int i=0;i<dsIDs.length;i++) {
			tmpDS[i]= new BaseLayer[dsIDs[i].length];
			for (int j=0;j<dsIDs[i].length;j++) {
				BaseLayer dsc = conf.getBaseLayerConfiguration(dsIDs[i][j]);
				if (dsc==null) return;
				tmpDS[i][j]=dsc;
			}
		}
		activeBaseLayers=tmpDS;
		if (activeThemeBaseLayers.defaultDataset!=null) {
			selectedBaseLayer = hasBaseLayer(activeThemeBaseLayers.defaultDataset);
		} else if (selectedBaseLayer!=null) {
			selectedBaseLayer = hasBaseLayer(selectedBaseLayer.id); // check if active base layers set contains selected base layer
		}
		
		notifyListeners(false);
	}
	
	
	private void notifyListeners(boolean justOnOff)
	{
		int n = listeners.size();
		for (int a=0; a<n; a++) {
			Listener l = listeners.get(a);
			l.rastersChanged(justOnOff);
		}
	}
	
	public BaseLayer[][] getActiveBaseLayers() {
		return activeBaseLayers;
	}
	public BaseLayer getVisibleBaseLayer() {
		return selectedBaseLayer;
	}

	
	public void setEnabled(ThemeBaseLayers baseLayers) {
		if (baseLayers==null)
			baseLayers = conf.defaultBaseLayers;
		this.activeThemeBaseLayers = baseLayers;
		updateBaseLayers();		
	}

	

	
	private BaseLayer hasBaseLayer(Integer baseLayerId) {
		if (baseLayerId == null || activeBaseLayers == null)
			return null;
		// search
		for (int i = 0; i < activeBaseLayers.length; i++) {
			for (int j = 0; j < activeBaseLayers[i].length; j++) {
				if (baseLayerId == activeBaseLayers[i][j].id) {
					return activeBaseLayers[i][j];					
				}
			}
		}
		return null;
	}
	
	public void toggleVisibleBaseLayer(BaseLayer baseLayer) {
		if (baseLayer==null) return;
		
		// deselect selected
		if (selectedBaseLayer !=null && selectedBaseLayer.id == baseLayer.id) {
			selectedBaseLayer = null;
			notifyListeners(true);		
			return;
		}
		BaseLayer dSet = hasBaseLayer(baseLayer.id);
		if (dSet!=null) {
			selectedBaseLayer = dSet;
			notifyListeners(true);			
		}
	}
	public void setVisibleBaseLayer(Integer baseLayerId ) {
		if (baseLayerId==null) return;

		if (selectedBaseLayer !=null && selectedBaseLayer.id == baseLayerId) {
			return;
		}
		BaseLayer dSet = hasBaseLayer(baseLayerId);
		if (dSet!=null) {
			selectedBaseLayer = dSet;
			notifyListeners(true);			
		}
	}
	
}