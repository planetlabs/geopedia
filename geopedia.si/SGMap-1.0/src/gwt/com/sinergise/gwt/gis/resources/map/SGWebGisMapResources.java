package com.sinergise.gwt.gis.resources.map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

public interface SGWebGisMapResources  extends ClientBundle{
	
	ImageResource mapCrosshair();
	
	static class Proxy {
        private static synchronized SGWebGisMapResources createInstance() {
            return GWT.create(SGWebGisMapResources.class);
        }
	}
	
	public static SGWebGisMapResources INSTANCE = Proxy.createInstance();
}
