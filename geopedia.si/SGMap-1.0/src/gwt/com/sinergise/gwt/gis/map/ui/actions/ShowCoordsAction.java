package com.sinergise.gwt.gis.map.ui.actions;


import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.common.geometry.display.DisplayCoordinateAdapter;
import com.sinergise.common.util.geom.Position2D;
import com.sinergise.gwt.gis.map.ui.ControlPositioner;
import com.sinergise.gwt.util.html.ExtDOM;


public class ShowCoordsAction extends MouseMoveActionW
{
    private final static class StatusBarCoordsSink implements CoordsSink {
		CRS myCS;
		private String curCoords;
		private String curScale;

		@Override
		public void setCRS(CRS coordsCS) {
			this.myCS=coordsCS;
		}

		@Override
		public void updateCoords(double worldX, double worldY) {
			curCoords = myCS.getCoordName(0)+": " + worldX + " "+myCS.getCoordName(1)+": " + worldY;
			updateStatusBar();
		}

		private void updateStatusBar() {
			ExtDOM.setStatusBarText(curScale + " " + curCoords);
		}
	}

	public static interface CoordsSink {
    	void setCRS(CRS coordsCS);
        void updateCoords(double worldX, double worldY);
    }
    
	private CoordsSink sink;
	
	
	public ShowCoordsAction(DisplayCoordinateAdapter dca)
	{
		this(dca, new StatusBarCoordsSink());
	}

	public ShowCoordsAction(DisplayCoordinateAdapter dca, final ControlPositioner coordPositioner)
	{
		this(dca, new CoordinatesLabel());
		coordPositioner.showControl((Widget)sink);
	}
	
	public ShowCoordsAction(DisplayCoordinateAdapter dca, CoordsSink sink)
	{
		super(dca, "Show Coords");
		this.sink = sink;
		sink.setCRS(dca.worldCRS);
	}

	@Override
	protected void mouseMovedW(Position2D curPos) {
		if (sink != null) {
		    sink.updateCoords(curPos.x, curPos.y);
		}
	}

    public void setSink(CoordsSink sink) {
        this.sink = sink;
        sink.setCRS(dca.worldCRS);
    }

    static final NumberFormat NUM_FORMAT=NumberFormat.getFormat("0.00");
}
