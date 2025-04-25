/**
 * 
 */
package com.sinergise.gwt.gis.map.shapes.editor;

import java.util.ArrayList;

import com.sinergise.common.geometry.display.DisplayCoordinateAdapter;
import com.sinergise.common.geometry.display.GraphicMeasure;
import com.sinergise.common.geometry.geom.Geometry;
import com.sinergise.common.util.event.SourcesValueChangeEvents;
import com.sinergise.common.util.event.ValueChangeListener;
import com.sinergise.common.util.event.ValueChangeListenerCollection;
import com.sinergise.gwt.gis.map.ui.IMapComponent;
import com.sinergise.gwt.gis.map.ui.MapComponent;
import com.sinergise.gwt.gis.map.ui.vector.LineMarkerStyle;
import com.sinergise.gwt.gis.map.ui.vector.Marker;
import com.sinergise.gwt.gis.map.ui.vector.VectorOverlay;
import com.sinergise.gwt.gis.map.ui.vector.signs.Sign;
import com.sinergise.gwt.ui.core.MouseClickAction;
import com.sinergise.gwt.ui.core.MouseHandler;
import com.sinergise.gwt.ui.core.MouseMoveAction;


/**
 * @author tcerovski
 */
public abstract class ShapeEditor implements SourcesValueChangeEvents<Geometry> {
	
	public static final String COLOR_TEMP = "rgb(255, 128, 255)";
	public static final String COLOR_MID = "rgb(0, 255, 0)";
    
	protected static final LineMarkerStyle TEMP_LINE_STYLE = new LineMarkerStyle(COLOR_TEMP, GraphicMeasure.fixedPixels(1));
    protected static final LineMarkerStyle FIXED_LINE_STYLE = new LineMarkerStyle(COLOR_MID, GraphicMeasure.fixedPixels(2));
	
	protected static final Sign LAST_NODE_SIGN = new Sign("edit-node active", 9);
    protected static final Sign MID_NODE_SIGN = new Sign("edit-node", 7);
	
	protected final IMapComponent map;
    protected final DisplayCoordinateAdapter dca;
    protected final VectorOverlay ovr;
    
    protected ArrayList<Marker> points;
    protected Geometry old = null;
    
    protected MovePointAct moveAct = new MovePointAct();
    protected AddPointAct addAct = new AddPointAct();
    protected FinishAct finishAct = new FinishAct();
    
    public ShapeEditor(IMapComponent map) {
    	this.map = map;
    	this.dca = map.getCoordinateAdapter();
    	this.ovr = new VectorOverlay(map.getCoordinateAdapter());
    }

	public void start() {
		old = getCurrentShape();
		if (points == null) 
			points = new ArrayList<Marker>();
		
        map.getOverlaysHolder().insertOverlay(ovr, MapComponent.Z_TOP_CONSTRUCTION, true);
        map.getMouseHandler().registerAction(moveAct, MouseHandler.MOD_NONE);
        map.getMouseHandler().registerAction(addAct, MouseHandler.BUTTON_LEFT, MouseHandler.MOD_NONE, 1);
        map.getMouseHandler().registerAction(finishAct, MouseHandler.BUTTON_RIGHT, MouseHandler.MOD_NONE, 1);
        map.getMouseHandler().registerAction(finishAct, MouseHandler.BUTTON_LEFT, MouseHandler.MOD_CTRL, 1);
	}
	
	public void finish() {
		fireValueChanged(old, getCurrentShape());
	}
	
	public void cancel() {
		cleanup();
	}
	
	public void cleanup() {
		ovr.clear();
		map.getMouseHandler().deregisterAction(moveAct);
		map.getMouseHandler().deregisterAction(addAct);
		map.getMouseHandler().deregisterAction(finishAct);
	}
	
	public abstract Geometry getCurrentShape();
	
	protected abstract void addNewPoint(int x, int y);
	
	protected abstract void moveLastPoint(int x, int y);
	
	protected abstract void cleanPrevious();
	
	public abstract int getGeometryTypeMask();
	
	protected ValueChangeListenerCollection<Geometry> changeLists;
	
	@Override
	public void addValueChangeListener(ValueChangeListener<? super Geometry> listener) {
        if (changeLists == null) {
        	changeLists = new ValueChangeListenerCollection<Geometry>();
        }
        changeLists.add(listener);
    }
    
    @Override
	public void removeValueChangeListener(ValueChangeListener<? super Geometry> listener) {
        if (changeLists == null) 
        	return;
        changeLists.remove(listener);
    }
    
	protected void fireValueChanged(Geometry oldVal, Geometry newVal) {
        if (changeLists == null) 
        	return;
        changeLists.fireChange(this, oldVal, newVal);
    }
    
    protected class MovePointAct extends MouseMoveAction {
        public MovePointAct() {
            super("Move last point");
        }
        @Override
		protected void mouseMoved(int x, int y) {
            moveLastPoint(x,y);
        }
    }
    
    protected class AddPointAct extends MouseClickAction {
        public AddPointAct() {
            super("Add new point");
            setProperty(ALLOW_DRAG, Boolean.FALSE);
        }
        @Override
		protected boolean mouseClicked(int x, int y) {
            addNewPoint(x, y);
            return false; // Doesn't matter
        }
        @Override
		public boolean allowDrag() {
            return false;
        }
    }
    
    protected class FinishAct extends MouseClickAction {
        public FinishAct() {
            super("Finish shape");
        }
        @Override
		protected boolean mouseClicked(int x, int y) {
            finish();
            return false;
        }
    }
	
}
