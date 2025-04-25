package com.sinergise.gwt.gis.map.ui.controls;

import com.google.gwt.user.client.ui.Composite;
import com.sinergise.common.geometry.display.DisplayCoordinateAdapter;
import com.sinergise.common.util.geom.DimI;
import com.sinergise.gwt.gis.map.ui.actions.CoordinatesLabel;
import com.sinergise.gwt.ui.core.MouseHandler;
import com.sinergise.gwt.ui.core.MouseMoveAction;

public class MousePositionBox extends Composite {
	MPBAction                mpb;
	CoordinatesLabel         coordsLabel;
	DisplayCoordinateAdapter adapter;
	
	class MPBAction extends MouseMoveAction {

		public MPBAction() {
			super("Coordinate move");
		}

		@Override
		protected void mouseMoved(int x, int y) {
			update (x, y);
		}
		
		public void updateInitial() {
			DimI dims = adapter.getDisplaySize();
			update(dims.w()/2, dims.h()/2);
		}
		
		public void update(int x, int y) {
			/* calculate new point and set text accordingly */
			coordsLabel.updateCoords(adapter.worldFromPix.x(x),
					                 adapter.worldFromPix.y(y));
		}
	}
	
	public MousePositionBox(MouseHandler mh, DisplayCoordinateAdapter adapter) {
		this.adapter = adapter;
		initWidget(coordsLabel = new CoordinatesLabel());
		coordsLabel.setCRS(adapter.worldCRS);
		mh.registerAction(mpb = new MPBAction(), MouseHandler.MOD_ANY);
		mpb.updateInitial();
	}
}