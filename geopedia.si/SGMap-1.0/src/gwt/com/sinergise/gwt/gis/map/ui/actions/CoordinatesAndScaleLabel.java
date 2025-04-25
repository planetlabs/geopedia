package com.sinergise.gwt.gis.map.ui.actions;

import com.google.gwt.user.client.ui.Label;
import com.sinergise.common.geometry.display.DisplayCoordinateAdapter;
import com.sinergise.common.geometry.display.event.CoordinatesListener;
import com.sinergise.common.geometry.util.CoordUtil;
import com.sinergise.gwt.gis.i18n.Labels;
import com.sinergise.gwt.gis.map.util.StyleConsts;

public class CoordinatesAndScaleLabel extends CoordinatesLabel{
	
	private Label 			lblScale;
	
	public CoordinatesAndScaleLabel(DisplayCoordinateAdapter coords){
		super();
		
		assert lblScale == null;

		lblScale = new Label("");
		lblScale.setStyleName(StyleConsts.COORDS_LABEL_PROJLABEL);
		vp.insert(lblScale, 0);
		
		coords.addCoordinatesListener(new CoordinatesListener() {
			@Override
			public void displaySizeChanged(int newWidthPx, int newHeightPx) {}
			
			@Override
			public void coordinatesChanged(double newX, double newY, double newScale, boolean coordsChanged, boolean scaleChanged) {
				if (scaleChanged) {
					updateScale(newScale);
				}
			}
		});
	}
	
	public void updateScale(double scale) {
		lblScale.setText(CoordUtil.formatScale(scale, Labels.INSTANCE.scale() + ": "));
	}

}
