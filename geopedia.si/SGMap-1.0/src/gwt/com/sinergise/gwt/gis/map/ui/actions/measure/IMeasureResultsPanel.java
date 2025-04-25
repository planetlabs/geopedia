package com.sinergise.gwt.gis.map.ui.actions.measure;

import com.sinergise.common.geometry.geom.Geometry;
import com.sinergise.common.util.event.ValueChangeListener;


//TODO: Move this to common after removing reference to RichLabel
public interface IMeasureResultsPanel extends ValueChangeListener<Geometry> {
	
	public void updateTotal(double length, double area);
	public void setNumSegments(int num);
	public void clearSegments();
	public void updateSegment(int i, double segLen);
	public void setAreaEnabled(boolean enabled);
	public void showControl();
	public void hideControl();
	
}
