/**
 * 
 */
package com.sinergise.gwt.gis.map.ui.actions;


import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.common.geometry.crs.transform.Transform;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.geometry.util.CoordinateFormat;
import com.sinergise.gwt.gis.map.ui.actions.ShowCoordsAction.CoordsSink;
import com.sinergise.gwt.gis.map.util.StyleConsts;


public class CoordinatesLabel extends Composite implements CoordsSink {
	protected FlowPanel 		vp;
	private Label			lblProj;
	private Label			lblLatLon;
	private CRS				myCRS;
	private Transform<?, ?>	projTransform;
	
	private CoordinateFormat projectedCoordFormat = new CoordinateFormat("XA YA: XC{0.###} YC{0.###}");
	private CoordinateFormat latlonCoordFormat = new CoordinateFormat("Xd°m''S{0.00}\" w Yd°m''S{0.00}\" w");
	
	

	public CoordinatesLabel() {
		vp = new FlowPanel();
		lblProj = new Label("");
		lblLatLon = new Label("");
		//DummyWidget spacer = new DummyWidget(12, 12);
		
		
		vp.add(lblProj);
		//vp.add(spacer);
		vp.add(lblLatLon);
		initWidget(vp);
		
		setStyleName(StyleConsts.COORDS_LABEL);
		lblProj.setStyleName(StyleConsts.COORDS_LABEL_PROJLABEL);
		lblLatLon.setStyleName(StyleConsts.COORDS_LABEL_LATLONLABEL);
		
//		UIObject.setStyleName(DOM.getParent(lblProj.getElement()), StyleConsts.COORDS_LABEL_PROJLABEL, true);
//		UIObject.setStyleName(DOM.getParent(spacer.getElement()), StyleConsts.COORDS_LABEL+"-spacer", true);
//		UIObject.setStyleName(DOM.getParent(lblLatLon.getElement()), StyleConsts.COORDS_LABEL_LATLONLABEL, true);
	}
	
	@Override
	public void setCRS(CRS arg0) {
		this.myCRS=arg0;
	}
	
	public void setTransform(Transform<?, ?> projTransform) {
		this.projTransform=projTransform;
	}
	
	public void setProjectedCoordFormat(CoordinateFormat format) {
		projectedCoordFormat = format;
	}
	
	public void setLatLonCoordFormat(CoordinateFormat format) {
		latlonCoordFormat = format;
	}
	
	@Override
	public void updateCoords(double x, double y) {
		Point p = new Point(x,y);
		lblProj.setText(projectedCoordFormat.format(myCRS, p));
		
		if (projTransform != null) {
			lblLatLon.setText(latlonCoordFormat.format(projTransform.getTarget(), projTransform.point(p, new Point())));
		}
	}
	
}