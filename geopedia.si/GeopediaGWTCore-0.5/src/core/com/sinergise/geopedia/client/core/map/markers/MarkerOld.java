package com.sinergise.geopedia.client.core.map.markers;

import com.google.gwt.user.client.Element;
import com.sinergise.common.util.geom.HasCoordinate;
import com.sinergise.gwt.gis.map.ui.vector.AbstractMarker;
import com.sinergise.gwt.ui.DummyWidget;
import com.sinergise.gwt.util.html.CSS;

public class MarkerOld extends AbstractMarker {
	private Sign sign;
	private String text;

	public Element activeContent;
	private HasCoordinate worldPos;

	public MarkerOld(Sign sign, String text, HasCoordinate position) {
		this.worldPos = position;
		DummyWidget simplePanel = new DummyWidget();
		setElement(simplePanel.getElement());
		this.sign = sign;
		this.text = text;
		activeContent = sign.initContent(this);
		CSS.position(getElement(), CSS.POS_ABSOLUTE);
	}
	
	@Override
	public void prepareToRender() {
	}

	public void updateSign() {
		sign.updateDisplay(this);
	}

	public String getText() {
		return text;
	}

	public Element getActiveContent() {
		return activeContent;
	}

	@Override
	public void positionPx(double l, double t) {
		CSS.leftTop(getElement(), l - sign.anchor.x, t - sign.anchor.y);
	}

	@Override
	public HasCoordinate getWorldPosition() {
		return worldPos;
	}
	
	public void setWorldPosition(HasCoordinate position) {
		this.worldPos = position;
	}
}