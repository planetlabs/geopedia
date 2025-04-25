package com.sinergise.geopedia.core.entities.baselayers;

import java.util.HashMap;
import java.util.Map;

public class WMSBaseLayer extends BaseLayer {

	private static final long serialVersionUID = 8913836155537772333L;

	
	public static WMSBaseLayer create(int id, String name, String description, String url, String wmsLayerName) {
		WMSBaseLayer wbl = new WMSBaseLayer();
		wbl.id=id;
		wbl.wmsLayerName = wmsLayerName;
		wbl.wmsBaseURL = url;
		wbl.name=name;
		wbl.description = description;
		return wbl;
	}
	public WMSBaseLayer() {
		super(Type.WMS);
}
	public String wmsBaseURL;
	public HashMap<String,String> wmsParameters; 
	public String wmsLayerName;

	@Override
	public int getMaxScaleLevel() {
		return 0;
	}
	
	private HashMap<String,String> getParameters() {
		if (wmsParameters==null) {
			wmsParameters = new HashMap<String,String>();
		}
		return wmsParameters;
	}
	public WMSBaseLayer setVersion(String version) {
		getParameters().put("VERSION", version);
		return this;
	}
	
	public WMSBaseLayer setParameters(Map<String,String> newParameters){
		getParameters().clear();
		getParameters().putAll(newParameters);
		return this;
	}
}
