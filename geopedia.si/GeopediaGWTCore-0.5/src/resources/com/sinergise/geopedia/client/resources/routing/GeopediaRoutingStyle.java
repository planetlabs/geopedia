package com.sinergise.geopedia.client.resources.routing;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.resources.client.ImageResource.RepeatStyle;

public interface GeopediaRoutingStyle extends ClientBundle {
	public static interface GeopediaRouteCss extends CssResource {
	}
	GeopediaRouteCss geopediaRoutingStyles();
	
	@ImageOptions(repeatStyle=RepeatStyle.Both)
	ImageResource routingBg();
	ImageResource travelmode1();
	ImageResource travelmode1On();
	ImageResource travelmode2();
	ImageResource travelmode2On();
	
	ImageResource markerBg();
	ImageResource markerShadow();
	ImageResource marker1();
	ImageResource marker2();
	ImageResource marker3();
	ImageResource marker4();
	
	ImageResource markA();
	ImageResource markB();
	ImageResource markC();
	ImageResource markD();
	ImageResource markE();
	ImageResource markF();
	ImageResource markG();
	ImageResource markH();
	
	ImageResource close();
	ImageResource closeOver();
	
	ImageResource routeArrow();
	ImageResource routeNavL();
	ImageResource routeNavLOn();
	ImageResource routeNavR();
	ImageResource routeNavROn();
	
	ImageResource google();
	@ImageOptions(repeatStyle=RepeatStyle.Both)
	ImageResource info16();
	
	ImageResource avoid();
	ImageResource avoidOn();
	
	ImageResource distance();
	ImageResource time();
	ImageResource zoomto();
	ImageResource zoomtoOver();
	
	public static class App {
        private static synchronized GeopediaRoutingStyle createInstance() {
            return GWT.create(GeopediaRoutingStyle.class);
        }
	}
	
	public static GeopediaRoutingStyle INSTANCE = App.createInstance();
}
