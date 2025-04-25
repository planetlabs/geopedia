/*
 *
 */
package com.sinergise.gwt.gis.map.ui.vector.signs;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.util.geom.DimI;
import com.sinergise.gwt.gis.map.ui.vector.Marker;
import com.sinergise.gwt.gis.map.util.StyleConsts;
import com.sinergise.gwt.util.html.CSS;


public class Sign
{
	public Point anchor=new Point(0,0);
	protected String className=StyleConsts.MAP_DEFAULT_IMAGE_SIGN;
	protected DimI size;
	protected String background=null;
	protected double borderWidth = 1;
	
    public Sign(int size) {
    	this(DimI.create(size, size));
	}

	public Sign(DimI size) {
        this.size = size;
        if (!size.isEmpty()) {
        	anchor.x = size.w()/2;
        	anchor.y = size.h()/2;
        }
	}
	
	public Sign(String className, int size) {
		this(className, DimI.create(size, size));
	}
	
	public Sign(String className, DimI size) {
	    this(size);
	    this.className=className;
    }
	
	public Sign(String className, DimI size, int borderW) {
		this(className, size);
		this.borderWidth = borderW;
	}
	
	public Sign(String className, int size, int borderW) {
		this(className, size);
		this.borderWidth = borderW;
	}
	
	/**
	 * @param marker The marker for which the content is being created; Useful is marker attributes are reflected in the sign's styling or when the sign contains active content that should have access to marker properties  
	 */
    public Element createContent(Marker marker) {
        Element div=DOM.createDiv();
        CSS.className(div, className);
        CSS.size(div, size);
        if (background!=null) {
            CSS.background(div, background);
        }
        CSS.borderWidth(div, borderWidth+"px");
        return div;
    }
    
    public void setAnchor(int aX, int aY) {
    	anchor.x=aX;
    	anchor.y=aY;
    }
    
    public void setBackground(String background) {
        this.background = background;
    }
    public void position(Element content, double l, double t) {
    	final double pL = l-anchor.x-borderWidth;
    	final double pT = t-anchor.y-borderWidth;
    	//Round because IE handles fractionals differently than FF
    	CSS.leftTop(content, (int)(pL+0.5), (int)(pT+0.5));
    }
    public void positionActiveContent(Element el) {
        CSS.position(el, CSS.POS_ABSOLUTE);
        CSS.size(el, "100%", "100%");
        CSS.leftTop(el, 0, 0);
    }
    
	public int getHeight() {
		return size.h();
	}
		
	public int getWidth() {
		return size.w();
	} 
}
