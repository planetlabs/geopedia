package com.sinergise.common.gis.map.model.layer.system;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sinergise.common.gis.map.model.layer.Layer;
import com.sinergise.common.gis.map.model.layer.LayerSpec;
import com.sinergise.common.gis.map.model.layer.LayersSource;
import com.sinergise.common.util.naming.Identifier;

public class SystemLayersSource extends Object implements LayersSource {
	public static final String ID_SYSTEM="SYSTEM_SOURCE";
	public static final SystemLayersSource INSTANCE=new SystemLayersSource();
	private SystemLayersSource() {
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends LayersSource> void asyncInitialize(AsyncCallback<? super T> cb) {
		cb.onSuccess((T)this);
	}

	@Override
	public Layer createLayer(LayerSpec spec) {
		return null;
	}

	@Override
	public LayerSpec findLayerSpec(String layerID, boolean ignoreCase) {
		return null;
	}

	@Override
	public String getTypeIdentifier() {
		return ID_SYSTEM;
	}

	@Override
	public boolean isInitialized() {
		return true;
	}

	@Override
	public boolean supports(String capability) {
		return false;
	}

	@Override
	public String getLocalID() {
		return ID_SYSTEM;
	}

	@Override
	public Identifier getQualifiedID() {
		return new Identifier(Identifier.ROOT, getLocalID());
	}

}
