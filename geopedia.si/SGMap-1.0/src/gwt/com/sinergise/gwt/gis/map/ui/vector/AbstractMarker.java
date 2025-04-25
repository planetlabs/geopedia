package com.sinergise.gwt.gis.map.ui.vector;

import java.util.LinkedList;

import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.FocusWidget;
import com.sinergise.common.util.geom.HasCoordinate;

public abstract class AbstractMarker extends FocusWidget implements HasCoordinate {
	protected Element el;
	private boolean visible = true;
	private LinkedList<String> styles = new LinkedList<String>();
	
	public abstract void prepareToRender();
	public abstract void positionPx(double l, double t) ;
	public abstract HasCoordinate getWorldPosition();
	
	@Override
	public double x() {
		return getWorldPosition().x();
	}
	
	@Override
	public double y() {
		return getWorldPosition().y();
	}
	
	@Override
	protected void setElement(Element elem) {
		setVisible(elem, visible);
		this.el = elem;
		super.setElement(elem);
		for (String st : styles) {
			addStyleName(st);
		}
	}
	
	@Override
	public void setVisible(boolean visible) {
		if (this.visible == visible) {
			return;
		} 
		this.visible = visible;
		if (el != null) {
			super.setVisible(visible);
		}
	}
	
	@Override
	public void addStyleName(String style) {
		if (!styles.contains(style)) {
			styles.add(style);
		}
		if (el != null) {
			super.addStyleName(style);
		}
	}
	
	@Override
	public void removeStyleName(String style) {
		styles.remove(style);
		if (el != null) {
			super.removeStyleName(style);
		}
	}
	
	@Override
	public boolean isVisible() {
		return el==null ? visible : super.isVisible();
	}
	
	public boolean isDisplayed() {
		if (el == null) {
			return false;
		}
		return getParent() != null;
	}
}
