package com.sinergise.gwt.gis.map.shapes.editor;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.FocusPanel;
import com.sinergise.common.geometry.display.DisplayCoordinateAdapter;
import com.sinergise.common.geometry.geom.Geometry;
import com.sinergise.common.geometry.geom.MultiPoint;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.ui.core.KeyCodes;
import com.sinergise.common.util.geom.DimI;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.geom.EnvelopeBuilder;
import com.sinergise.common.util.math.MathUtil;
import com.sinergise.gwt.gis.map.ui.IMapComponent;
import com.sinergise.gwt.gis.map.ui.MapComponent;
import com.sinergise.gwt.gis.map.ui.vector.Marker;
import com.sinergise.gwt.gis.map.ui.vector.VectorOverlay;
import com.sinergise.gwt.gis.map.ui.vector.signs.Sign;
import com.sinergise.gwt.ui.core.MouseClickAction;
import com.sinergise.gwt.ui.core.MouseDragAction;
import com.sinergise.gwt.ui.core.MouseHandler;

public class PointEditor  {
	
	public static interface GeomChangedListener {
		void geometryChanged();
	}
	
	protected static final Sign POINT_SIGN = new Sign("pointEditor-marker",DimI.create(13, 13),3);
	protected static final String SELECTED_POINT_STYLE = "selected";
	protected static final int POINT_SELECTION_MAX_DIST_SQ = 10*10;
	
	private List<Marker> points = new ArrayList<Marker>();
	Marker selectedPoint = null;
	private boolean isEditing = false;
	
	final IMapComponent map;
	final DisplayCoordinateAdapter dca;
	final VectorOverlay ovr;
	private boolean allowMultiPoints;
	
	private List<GeomChangedListener> changeListeners = new ArrayList<PointEditor.GeomChangedListener>();
	
	public PointEditor(IMapComponent map) {
		this.map = map;
		this.dca = map.getCoordinateAdapter();
		this.ovr = new VectorOverlay(map.getCoordinateAdapter());
	}
	
	public PointEditor(IMapComponent map, boolean allowMultiPoints) {
		this(map);
		setAllowMultiPoints(allowMultiPoints);
	}
	
	public void setAllowMultiPoints(boolean allowMultiPoints) {
		this.allowMultiPoints = allowMultiPoints;
	}

	public void setGeometry(Geometry geom) {
		clearGeometry();
				
		if (geom == null) {
			return;
		} else if (geom instanceof Point) {
			addPoint((Point)geom, true);
		} else if (geom instanceof MultiPoint && allowMultiPoints) {
			MultiPoint mp = (MultiPoint)geom;
			for (int i=0; i<mp.size(); i++) {
				addPoint(mp.get(i), true);
			}
		} else {
			throw new IllegalArgumentException("Invalid geometry type: "+geom.getClass().getName());
		}
		fireGeomChanged();
	}
	
	public Geometry getGeometry() {
		if (points.size() == 1) {
			Point point = new Point(points.get(0).getWorldPosition());
			point.setCrsId(dca.worldCRS.getDefaultIdentifier());
			return point;
		} else if (points.size() > 1 && allowMultiPoints) {
			Point[] pts = new Point[points.size()];
			for (int i=0; i<points.size(); i++) {
				pts[i] = new Point(points.get(i).getWorldPosition());
			}
			MultiPoint multiPoint = new MultiPoint(pts);
			multiPoint.setCrsId(dca.worldCRS.getDefaultIdentifier());
			return multiPoint;
		}
		return null;
	}
	
	private void clearGeometry() {
		points.clear();
		selectedPoint = null;
		ovr.clear();
	}
	
	public boolean hasValidGeometry() {
		return !points.isEmpty();
	}
	
	
	protected void addPoint(Point p) {
		addPoint(p, false);
	}
			
	private void addPoint(Point p, boolean silent) {
		if (!allowMultiPoints && points.size() > 0) {
			clearGeometry();
		}
		Marker m = new Marker(POINT_SIGN, p);
		points.add(m);
		ovr.addPoint(m);
		setSelectedPoint(m);
		if (!silent) {
			fireGeomChanged();
		}
	}
	
	void deletePoint(Marker m) {
		points.remove(m);
		ovr.removePoint(m);
		if (selectedPoint == m) {
			setSelectedPoint(null);
		}
		fireGeomChanged();
	}
	
	void setSelectedPoint(Marker m) {
		if (selectedPoint != null) {
			selectedPoint.removeStyleDependentName(SELECTED_POINT_STYLE);
		}
		selectedPoint = m;
		if (selectedPoint != null) {
			selectedPoint.addStyleDependentName(SELECTED_POINT_STYLE);
		}
	}
	
	Marker getNearestPoint(int px, int py, int maxDistSq) {
		Marker nearest = null;
		int nearestDistSq = maxDistSq;
		
		for (Marker m : points) {
			int mpx = dca.pixFromWorld.xInt(m.getWorldPosition().x());
			int mpy = dca.pixFromWorld.yInt(m.getWorldPosition().y());
			int distSq = MathUtil.distSq(px, py, mpx, mpy);
			if (distSq <= nearestDistSq) {
				nearest = m;
				nearestDistSq = distSq;
			}
		}
		
		return nearest;
	}
	
	public boolean isEditing() {
		return isEditing;
	}
	
	public void startEditing() {
		map.getOverlaysHolder().insertOverlay(ovr, MapComponent.Z_TOP_CONSTRUCTION, true);
        map.getMouseHandler().registerAction(dragAct, MouseHandler.BUTTON_LEFT, MouseHandler.MOD_NONE);
        map.getMouseHandler().registerAction(leftClickAct, MouseHandler.BUTTON_LEFT, MouseHandler.MOD_NONE, 1);
        map.getMouseHandler().registerAction(rightClickAct, MouseHandler.BUTTON_RIGHT, MouseHandler.MOD_NONE, 1);
        if (map.getOverlaysHolder()!=null) {
        	deletePointHandlerRegistration  = map.getOverlaysHolder().addDomHandler(deletePointHandler, KeyDownEvent.getType());
        	map.getOverlaysHolder().setFocus(true);
        }
        isEditing = true;
	}
	
	public void stopEditing() {
		cleanup();
		isEditing = false;
	}
	
	private void cleanup() {
		ovr.clear();
		map.getMouseHandler().deregisterAction(dragAct);
		map.getMouseHandler().deregisterAction(leftClickAct);
		map.getMouseHandler().deregisterAction(rightClickAct);
        if (deletePointHandlerRegistration != null) {
        	deletePointHandlerRegistration.removeHandler();
        }
	}

	private MouseClickAction leftClickAct = new MouseClickAction("lclick") {
		@Override
		protected boolean mouseClicked(int x, int y) {
			Marker nearest = getNearestPoint(x, y, POINT_SELECTION_MAX_DIST_SQ);
			if (nearest != null) {
				setSelectedPoint(nearest);
			} else {
				addPoint(new Point(dca.worldFromPix.x(x), dca.worldFromPix.y(y)));
			}
			if (map.getParent() instanceof FocusPanel) {
				((FocusPanel)map.getParent()).setFocus(true);
			}
			return false;
		}
	};
	
	private MouseClickAction rightClickAct = new MouseClickAction("rclick") {
		@Override
		protected boolean mouseClicked(int x, int y) {
			setSelectedPoint(null);
			return false;
		}
	};
	
	private MouseDragAction dragAct = new MouseDragAction("drag") {
		
		@Override
		protected boolean dragStart(int x, int y) {
			Marker nearest = getNearestPoint(x, y, POINT_SELECTION_MAX_DIST_SQ);
			if (nearest != null) {
				setSelectedPoint(nearest);
				return true;
			}
			return false;
		}
		
		@Override
		protected void dragMove(int x, int y) {
			if (selectedPoint != null) {
				ovr.setPointLocationPix(selectedPoint, x, y);
			}
		}
		
		@Override
		protected void dragEnd(int x, int y) { 
			fireGeomChanged();
		}
	};
	
	HandlerRegistration deletePointHandlerRegistration = null;
	private KeyDownHandler deletePointHandler = new KeyDownHandler() {
		@Override
		public void onKeyDown(KeyDownEvent event) {
			int code = event.getNativeKeyCode();
			if(selectedPoint != null && (code == KeyCodes.KEY_DELETE || code == KeyCodes.KEY_D)) {
				deletePoint(selectedPoint);
			}
		}
	};
	
	void fireGeomChanged() {
		for (GeomChangedListener listener : changeListeners) {
			listener.geometryChanged();
		}
	}
	
	public void addGeomChangedListener(GeomChangedListener listener) {
		changeListeners.add(listener);
	}
	
	public void removeGeomChangedListener(GeomChangedListener listener) {
		changeListeners.remove(listener);
	}

	public Envelope getEnvelope() {
		EnvelopeBuilder bld = new EnvelopeBuilder(dca.worldCRS.getDefaultIdentifier());
		for (Marker m : points) {
			bld.expandToInclude(m.getWorldPosition());
		}
		return bld.getEnvelope();
	}
	
}
