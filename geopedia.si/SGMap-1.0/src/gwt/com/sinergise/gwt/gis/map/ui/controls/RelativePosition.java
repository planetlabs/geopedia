/**
 * 
 */
package com.sinergise.gwt.gis.map.ui.controls;

import com.google.gwt.user.client.ui.Widget;

/**
 * @author tcerovski
 */
public class RelativePosition {
	public static enum HorizontalAlignment {LEFT_RIGHT, CENTER, RIGHT_RIGHT, NONE}
	public static enum VerticalAlignment {TOP_UP, MIDDLE, BOTTOM_DOWN, NONE}
	
	Widget relativeTo;
	int offsetX;
	int offsetY;
	VerticalAlignment vAlign;
	HorizontalAlignment hAlign;
	
	public RelativePosition(Widget relativeTo, int offsetX, int offsetY, 
			VerticalAlignment vAlign, HorizontalAlignment hAlign) 
	{
		this.relativeTo = relativeTo;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		this.vAlign = vAlign;
		this.hAlign = hAlign;
	}
	
}
