package com.sinergise.gwt.gis.map.ui.util;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalSplitPanel;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.gwt.gis.map.ui.MapComponent;
import com.sinergise.gwt.gis.map.util.StyleConsts;
import com.sinergise.gwt.ui.maingui.extwidgets.SGSplitLayoutPanel;

/**
 * Horizontal split panel for {@link MapComponent}.
 * Wraps the {@link HorizontalSplitPanel} and adds necessary styling information.
 * 
 * @author tcerovski
 * @deprecated Use {@link SGSplitLayoutPanel} instead
 */
@Deprecated
public class HorizontalMapSplitPanel extends Composite {
	
	private HorizontalSplitPanel wrapped;
	private final boolean mapLeft;

	/** 
	 * Constructs new <code>HorizontalMapSplitPanel</code> with map component on 
	 * the left if <code>mapLeft</code> is <code>true</code> or 
	 * map component on the right if <code>mapLeft</code> is <code>false</code>.
	 * @param map Map component
	 * @param other Other stuff
	 * @param mapLeft Indicates if map component should be placed left or right.
	 */
	public HorizontalMapSplitPanel(Widget map, Widget other, boolean mapLeft) {
		this.mapLeft = mapLeft;
		initWidget(wrapped = new HorizontalSplitPanel());
		init(mapLeft?map:other, mapLeft?other:map);
		wrapped.setStylePrimaryName(mapLeft ? StyleConsts.HMAP_SPLIT_PANEL_LEFTMAP : StyleConsts.HMAP_SPLIT_PANEL_RIGHTMAP);
	}
	
	private void init(Widget left, Widget right) {
		wrapped.setLeftWidget(left);
		wrapped.setRightWidget(right);
		
		// XXX Ugly hack; Check when upgrading GWT version
		Element mainElem = DOM.getChild(wrapped.getElement(), 0);
		Element leftElem = DOM.getChild(mainElem, 0);
		Element splitElem = DOM.getChild(mainElem, 1);
		Element rightElem = DOM.getChild(mainElem, 2);
		DOM.removeChild(mainElem, splitElem);
		DOM.appendChild(mainElem, splitElem);

		UIObject.setStyleName(leftElem, getStylePrimaryName() + "-left", true);
		UIObject.setStyleName(splitElem, getStylePrimaryName() + "-split", true);
		UIObject.setStyleName(rightElem, getStylePrimaryName() + "-right", true);
	}
	
	/**
	 * @return <code>true</code> if map component is placed on the left side of the split panel.
	 */
	public boolean isMapLeft() {
		return mapLeft;
	}
	
	/* (non-Javadoc)
	 * @see com.google.gwt.user.client.ui.HorizontalSplitPanel.setSplitPosition(String)
	 */
	public void setSplitPosition(String pos) {
		wrapped.setSplitPosition(pos);
	}
	
	/* (non-Javadoc)
	 * @see com.google.gwt.user.client.ui.UIObject#setSize(java.lang.String, java.lang.String)
	 */
	@Override
	public void setSize(String width, String height) {
		super.setSize(width, height);
		wrapped.setSize(width, height);
	}
	
	/* (non-Javadoc)
	 * @see com.google.gwt.user.client.ui.HorizontalSplitPanel.getLeftWidget()
	 */
	public Widget getLeftWidget() {
	    return wrapped.getLeftWidget();
	}

	/* (non-Javadoc)
	 * @see com.google.gwt.user.client.ui.HorizontalSplitPanel.getRightWidget()
	 */
	public Widget getRightWidget() {
		return wrapped.getRightWidget();
	}
	
	/* (non-Javadoc)
	 * @see com.google.gwt.user.client.ui.UIObject#addStyleDependentName(java.lang.String)
	 */
	@Override
	public void addStyleDependentName(String styleSuffix) {
		wrapped.addStyleDependentName(styleSuffix);
	}
	
	
	/* (non-Javadoc)
	 * @see com.google.gwt.user.client.ui.UIObject#addStyleName(java.lang.String)
	 */
	@Override
	public void addStyleName(String style) {
		wrapped.addStyleName(style);
	}
	
	/* (non-Javadoc)
	 * @see com.google.gwt.user.client.ui.UIObject#setStyleName(java.lang.String)
	 */
	@Override
	public void setStyleName(String style) {
		wrapped.setStyleName(style);
	}
	
	/* (non-Javadoc)
	 * @see com.google.gwt.user.client.ui.UIObject#setStylePrimaryName(java.lang.String)
	 */
	@Override
	public void setStylePrimaryName(String style) {
		wrapped.setStylePrimaryName(style);
	}
}
