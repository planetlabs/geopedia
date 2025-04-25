/*
 *
 */
package com.sinergise.gwt.gis.map.ui.actions;


import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.sinergise.gwt.gis.map.util.StyleConsts;
import com.sinergise.gwt.util.html.CSS;


public class ZoomBox {
    private ZoomBox() {
    }
    /**
     * 
     * @param stylePrefix prefix (followed by dependent styles "-outer" and "-inner")
     * @return
     */
    public static Element createZoomBox(String stylePrefix) {
        Element el=DOM.createDiv();
        CSS.className(el,stylePrefix+"-"+StyleConsts.MAP_ZOOM_BOX_OUTER_SUFFIX);
        CSS.fontSize(el, "0");
        Element innerDiv=DOM.createDiv();
        CSS.className(innerDiv, stylePrefix+"-"+StyleConsts.MAP_ZOOM_BOX_INNER_SUFFIX);
        CSS.fontSize(innerDiv, "0");
        DOM.appendChild(el, innerDiv);
        CSS.position(innerDiv, CSS.POS_ABSOLUTE);
        CSS.leftTop(innerDiv, 0, 0);
        CSS.size(innerDiv, CSS.PERC_100, CSS.PERC_100);
        return el;
    }
}
