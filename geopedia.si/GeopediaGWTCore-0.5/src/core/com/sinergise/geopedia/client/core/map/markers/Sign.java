/*
 *
 */
package com.sinergise.geopedia.client.core.map.markers;


import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.sinergise.common.util.geom.EnvelopeI;
import com.sinergise.common.util.geom.PointI;
import com.sinergise.gwt.util.html.CSS;
import com.sinergise.gwt.util.html.ExtDOM;


public class Sign
{
	protected EnvelopeI linkMBR;
	protected PointI anchor;
	
	protected Element initContent(MarkerOld marker) {
		Element textComp=DOM.createAnchor();
		CSS.className(textComp, "gisopedia-markerText");
		CSS.position(textComp, CSS.POS_ABSOLUTE);
		DOM.appendChild(marker.getElement(), textComp);
		return textComp;
	}
	
	public void updateDisplay(MarkerOld marker) {
		Element anchEl=ExtDOM.getChildById(marker.getElement(), "markerSignAnchor");
		
		String text=marker.getText();
		if (text!=null) {
			DOM.setInnerHTML(anchEl, text);
		}
		if (linkMBR!=null) {
			CSS.leftTop(anchEl, linkMBR.minX(), linkMBR.minY());
			CSS.sizePx(anchEl, linkMBR.getWidth(), linkMBR.getHeight());
		} else {
			CSS.leftTop(anchEl, 0, 0);
			CSS.size(anchEl, CSS.PERC_100, CSS.PERC_100);
		}
	}
}
