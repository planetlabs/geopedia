package com.sinergise.geopedia.client.core.util;


import java.util.ArrayList;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.sinergise.common.geometry.display.DisplayCoordinateAdapter;
import com.sinergise.geopedia.client.core.ClientGlobals;
import com.sinergise.geopedia.client.core.ClientSession;
import com.sinergise.geopedia.client.core.entities.ThemeHolder;
import com.sinergise.geopedia.client.core.i18n.Messages;
import com.sinergise.geopedia.client.core.map.HighlightTilesProvider;
import com.sinergise.geopedia.client.ui.map.MapComponent;
import com.sinergise.geopedia.client.ui.map.MapWidget;
import com.sinergise.geopedia.core.config.Copyright;
import com.sinergise.geopedia.core.config.IsURLProvider;
import com.sinergise.geopedia.core.constants.Globals;
import com.sinergise.geopedia.core.entities.Theme;
import com.sinergise.geopedia.core.entities.baselayers.BaseLayer;

public class Print {

	private MapWidget mapWidget;
	public Print (MapWidget mapWidget) {
		this.mapWidget = mapWidget;
		
	}

	
	public void go() {
		String URL = GWT.getModuleBaseURL()+"printtemplates/lite.html";
		RequestBuilder tmplBuilder = new RequestBuilder(RequestBuilder.GET, URL);
		try {
			tmplBuilder.sendRequest("", new RequestCallback() {

				@Override
				public void onResponseReceived(Request request, Response response) {
					internalPrint(response.getText());
				}

				@Override
				public void onError(Request request, Throwable exception) {
				}
				
			});
		} catch (RequestException e) {
			e.printStackTrace();
		}
	}
	public void internalPrint(String template) {
		
		ThemeHolder th = mapWidget.getMapLayers().getDefaultTheme();
		
		Theme thm = th.getEntity();
		if (thm==null) return;
		
		
		
		String mapHTML = "<div class=\"label\">"+Messages.INSTANCE.printGraphicalView()+"<b>"+thm.getName()+"</b></div><img src='"+imageURL(mapWidget).toString()+"'/>";
		String sidebarHTML = "";
		String headerHTML = "";
		
		headerHTML+="<div>";
		headerHTML+=thm.getName();
		headerHTML+="</div>";

		StringBuffer buf = new StringBuffer();
		buf.append("<div class=\"output\">");
		buf.append(thm.descDisplayableHtml);
		buf.append("</div>");
		sidebarHTML+=buf.toString();
		
		String footerHTML = generateFooterPanel();
		
		String btnHTML = "";
		StringBuffer btnPanel = new StringBuffer();
		btnPanel.append("<div id='buttons'><a class='btn icon print' onclick='window.print();return false;'><span>" + Messages.INSTANCE.printBtnPrint());
		btnPanel.append("</span></a></div>");
		btnHTML+=btnPanel.toString();
		
		generatePrintWindow(GWT.getModuleBaseURL(),template, sidebarHTML, mapHTML,  footerHTML, headerHTML, btnHTML);
	}
	
	private String generateFooterPanel() {
		ArrayList<Copyright> copyrights = mapWidget.getCopyrights();
		
		StringBuffer buf = new StringBuffer();
		for (Copyright copy:copyrights) {
			if (buf.length()==0) {
				buf.append("<div>&copy; "+Globals.COPYRIGHT_DATE+"</div>");				
			} else {
				buf.append("");
			}
			buf.append("<div>"+copy.name);			
			buf.append("<img src='"+GWT.getModuleBaseURL()+copy.getImageURL(true)+"'/></div>");
		}
		return buf.toString();
	}
	
	public static StringBuffer imageURL(MapWidget mapWidget) {
		return imageURL(mapWidget, null, null, null, null, null, null);
	}
	
   public static StringBuffer imageURL(MapWidget mapWidget, Double centerX, Double centerY, Integer scale, Integer baseLayerId, Integer width, Integer height) {
       StringBuffer buf = new StringBuffer();
       MapComponent mapComponent = mapWidget.getMapComponent();
       IsURLProvider publicRendererURLProvider = ClientGlobals.configuration.publicRenderers;
       buf.append(publicRendererURLProvider.getBaseURL());
       buf.append("prnt");
       buf.append('?');          
       buf.append("drw=");
       mapComponent.getMapLayers().appendThemeLayers(buf);
       HighlightTilesProvider htp = mapWidget.getFeatureHighlighter();
       buf.append("&hlt=");
       htp.appendHighlightFeatures(buf);

       DisplayCoordinateAdapter coords = mapComponent.getCoordinateAdapter();
       
       buf.append("&x=");
       if(centerX == null) buf.append(Math.round(coords.worldCenterX));
       else buf.append(Math.round(centerX));
       
       buf.append("&y=");
       if(centerY == null) buf.append(Math.round(coords.worldCenterY));
       else buf.append(Math.round(centerY));
       
       buf.append("&s=");
       if(scale == null) buf.append(String.valueOf(mapComponent.getZoomLevel()));
       else buf.append(String.valueOf(scale));
       
       buf.append("&w=");
       if(width == null) buf.append("1100");
       else buf.append(width);
       
       buf.append("&h=");
       if(height == null) buf.append("725");
       else buf.append(height);
       // ideal size for landscape A4 ?? 1719x1133

       buf.append("&type=image/jpeg");
       
       if(baseLayerId == null){
           BaseLayer baseDS = mapWidget.getRasters().getVisibleBaseLayer();
           if (baseDS!=null) {
        	   buf.append("&b="+String.valueOf(baseDS.id));
           }
       } else {
    	   buf.append("&b="+String.valueOf(baseLayerId));
       }
       
		String sidParam = null;
		String session = ClientSession.getSessionValue();
		if (session != null && session.length() > 0) {
			sidParam = "&sid=" + session;
			buf.append(sidParam);
		}

       return buf;
   }
	
	public static native void generatePrintWindow(String baseURL,String template, String sidebarHTML,String mapHTML, String footerHTML, String headerHTML, String btnHTML) /*-{
		var printW = window.open(baseURL+"print.html");
		var doc = printW.document;
		doc.open();
		doc.write(template);
		doc.close();
		var mapHolder = doc.getElementById('mapPrint');
		mapHolder.innerHTML=mapHTML;
		var sidebarHolder = doc.getElementById('sidebarPrint');
		sidebarHolder.innerHTML=sidebarHTML;
		var footerHolder = doc.getElementById('footerPrint');
		footerHolder.innerHTML=footerHTML;
		var headerHolder = doc.getElementById('title');
		headerHolder.innerHTML=headerHTML;
		var btnHolder = doc.getElementById('buttons');
		btnHolder.innerHTML=btnHTML;	
	}-*/;
}
