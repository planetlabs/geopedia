/**
 * 
 */
package com.sinergise.gwt.gis.map.ui.actions.measure;


import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.geometry.display.GraphicMeasure;
import com.sinergise.common.geometry.util.GeomUtil;
import com.sinergise.common.util.geom.HasCoordinate;
import com.sinergise.gwt.gis.map.shapes.editor.PolygonShapeEditor;
import com.sinergise.gwt.gis.map.ui.ControlPositioner;
import com.sinergise.gwt.gis.map.ui.IMapComponent;
import com.sinergise.gwt.gis.map.ui.MapComponent;
import com.sinergise.gwt.gis.map.ui.vector.LineMarkerStyle;
import com.sinergise.gwt.gis.map.ui.vector.Marker;
import com.sinergise.gwt.ui.ActionUtilGWT;


/**
 * @author tcerovski
 */
public class Measurer extends PolygonShapeEditor {

	public static final String COLOR_CLOSE = "rgb(64,96,255)";
	protected static final LineMarkerStyle CLOSE_LINE_STYLE = new LineMarkerStyle(COLOR_CLOSE, GraphicMeasure.fixedPixels(1));

	IMeasureResultsPanel results;
	ControlPositioner resultsPos;

	public Measurer(MapComponent map, ControlPositioner resultsPos, String iconURL, MeasureModeAction act) {
		super(map);
		setClosedPoligon(true);
		Widget actionButton = null;

		if (act != null) {
			actionButton = ActionUtilGWT.createActionButton(act);
		}
		this.results = new MeasureResultsPanel(iconURL, actionButton);
		this.resultsPos = resultsPos;
		addValueChangeListener(results);
	}

	public Measurer(IMapComponent map, IMeasureResultsPanel results) {
		super(map);
		setClosedPoligon(true);
		this.results = results;
		this.resultsPos = null;
		addValueChangeListener(results);
	}

	/* (non-Javadoc)
	 * @see com.sinergise.gis.client.map.shapes.editor.ShapeEditor#start()
	 */
	@Override
	public void start() {
		super.start();
		if (resultsPos != null) resultsPos.showControl((Widget)results);
		else results.showControl();
	}

	/* (non-Javadoc)
	 * @see com.sinergise.gis.client.map.shapes.editor.PolygonShapeEditor#addNewPoint(int, int)
	 */
	@Override
	protected void addNewPoint(int x, int y) {
		super.addNewPoint(x, y);
		updateResults();
	}


	/* (non-Javadoc)
	 * @see com.sinergise.gis.client.map.shapes.editor.PolygonShapeEditor#cleanPrevious()
	 */
	@Override
	protected void cleanPrevious() {
		super.cleanPrevious();
		updateResults();
	}

	/* (non-Javadoc)
	 * @see com.sinergise.gis.client.map.shapes.editor.PolygonShapeEditor#cleanup()
	 */
	@Override
	public void cleanup() {
		try {
			super.cleanup();
		} finally {
			if (resultsPos != null) resultsPos.hideControl((Widget)results);
			else results.hideControl();
		}
	}

	private void updateResults() {
		if (startPoint == null) {
			results.updateTotal(0, 0);
			results.clearSegments();
			return;
		}
		double len = 0;
		double area = 0;
		HasCoordinate prev = null;
		results.clearSegments();
		for (int i = 0; i < points.size(); i++) {
			Marker cur = points.get(i);
			HasCoordinate curP = cur.worldPos;
			if (prev != null) {
				double segLen = GeomUtil.distance(prev, curP);
				results.updateSegment(i - 1, segLen);
				len += segLen;
				area += GeomUtil.segArea(prev, curP);
			}
			prev = curP;
		}
		if (points.size() >= 3) {
			area += GeomUtil.segArea(prev, startPoint.worldPos);
		} else {
			area = 0;
		}
		results.updateTotal(len, Math.abs(area));
	}

	public IMeasureResultsPanel getResults() {
		return results;
	}

	public void setAreaEnabled(boolean enabled) {
		setClosedPoligon(enabled);
		if (results != null) {
			results.setAreaEnabled(enabled);
		}
	}
	
	@Override
	protected LineMarkerStyle closeLineStyle() {
		return CLOSE_LINE_STYLE;
	}

}
