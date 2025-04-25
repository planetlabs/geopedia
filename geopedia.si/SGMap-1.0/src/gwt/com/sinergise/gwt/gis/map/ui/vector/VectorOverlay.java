/*
 *
 */
package com.sinergise.gwt.gis.map.ui.vector;

import java.util.ArrayList;

import com.sinergise.common.geometry.display.DisplayCoordinateAdapter;
import com.sinergise.common.gis.map.render.RenderInfo;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.geom.HasCoordinateMutable;
import com.sinergise.gwt.gis.ui.gfx.Canvas;


public class VectorOverlay extends MarkerOverlay
{
	protected ArrayList<IOverlayShape> shapes;
	protected Canvas vectorCanvas;
	
    public VectorOverlay(DisplayCoordinateAdapter dca)
    {
	    super(dca);
	    shapes = new ArrayList<IOverlayShape>();
	    vectorCanvas = Canvas.create();
	    pnl.add(vectorCanvas, 0, 0);
    }
    
    @Override
	public void clear() {
    	super.clear();
    	for (IOverlayShape s : shapes) {
			removeFromCanvas(s);
		}
    	shapes.clear();
    }
    
    @Override
	public boolean isEmpty() {
    	return super.isEmpty() && shapes.isEmpty();
    }
    
    public void removeShapes(IOverlayShape[] toRemove) {
    	for (int i=0; i<toRemove.length; i++) {
    		removeShape(toRemove[i]);
    	}
    }

    public void removeShape(IOverlayShape shape) {
        if (shape==null) return;
        if (!shapes.contains(shape)) {
        	throw new IllegalArgumentException("Can't remove shape that is not contained in the overlay");
        }
        shapes.remove(shape);
        removeFromCanvas(shape);
    }

	private void removeFromCanvas(IOverlayShape shape) {
		if (shape.getElement() != null) {
			vectorCanvas.removeElement(shape.getElement());
	        shape.setElement(null);
		}
	}
    
    @Override
	public void reposition(RenderInfo info) {
   	    vectorCanvas.setSize(info.getDisplaySize());		
    	super.reposition(info);
    }

    @Override
    public void updateDisplay() {
    	super.updateDisplay();
		for (IOverlayShape s : shapes) {
			updateShapeGeometry(s);
	    }
    }

	private void updateShapeGeometry(IOverlayShape shp) {
		if (shp instanceof LineMarker) {
			updateLineLocation((LineMarker)shp);	
			
		} else if (shp instanceof CircleMarker) {
			updateCircleGeometry((CircleMarker)shp);
			
		} else if (shp instanceof RectangleMarker) {
			updateRectangleGeometry((RectangleMarker)shp);
			
		} else if (shp instanceof AbstractTextMarker) {
			updateTextLocation((AbstractTextMarker)shp);
		}
	}
    
    public void updateShapeStyle(IOverlayShape[] sh, LineMarkerStyle style){
		final float computedStrokeWidth = style.getStrokeWidthPx(dca);
    	for (IOverlayShape s : sh) {
    		vectorCanvas.updateElementStyle(s.getElement(), computedStrokeWidth, style);
    	}
    }

	public void updateClosedShapeStyle(IOverlayShape sh, ClosedMarkerStyle style) {
		vectorCanvas.updateClosedElementStyle(sh.getElement(), style.getStrokeWidthPx(dca), style);
	}
    
	//## TEXTS ########
	
	public void addText(AbstractTextMarker textm){
		shapes.add(textm);
		// Will add to canvas
		updateTextLocation(textm);
	}

	public void addToCanvas(AbstractTextMarker textm) {
		textm.el = vectorCanvas.addText(0, 0, textm);
		updateTextLocation(textm);
	}
	
	public void updateTextLocation(AbstractTextMarker textm) {
		textm.updateStyle(dca, vectorCanvas);
		if (textm.isVisible()) {
			if (textm.el == null) {
				textm.el = vectorCanvas.addText(0, 0, textm);
				if (textm.el == null) {
					return;
				}
			}
			double x=pxFromW.x(textm.getCoordinates().x());
			double y=pxFromW.y(textm.getCoordinates().y());
			vectorCanvas.updateText(textm.el, x, y, textm);
		} else if (textm.el != null) {
			removeFromCanvas(textm);
		}
	}
    
	
    //## LINES ########
    
	public void addLines(LineMarker[] lineMrks) {
		for (int i = 0; i < lineMrks.length; i++) {
			addLine(lineMrks[i]);
		}
	}

	public void addLine(LineMarker lineMrk) {
        shapes.add(lineMrk);
        addToCanvas(lineMrk);
    }

	public void addToCanvas(LineMarker lineMrk) {
		double x1=pxFromW.x(lineMrk.x1());
        double y1=pxFromW.y(lineMrk.y1());
        double x2=pxFromW.x(lineMrk.x2());
        double y2=pxFromW.y(lineMrk.y2());
        
        lineMrk.el = vectorCanvas.addLine(x1, y1, x2, y2, lineMrk.style.getStrokeWidthPx(dca), lineMrk.style);
	}
	
    public void updateLinesLocations(LineMarker[] lns) {
    	for(int i=0; i<lns.length; i++) {
    		updateLineLocation(lns[i]);
    	}
    }

    public void updateLineLocation(LineMarker ln) {
        final double x1=pxFromW.x(ln.x1());
        final double y1=pxFromW.y(ln.y1());
        final double x2=pxFromW.x(ln.x2());
        final double y2=pxFromW.y(ln.y2());
        vectorCanvas.updateLine(ln.el, x1, y1, x2, y2);
    }
    
    public void setLineEndPix(LineMarker line, int x, int y) {
        HasCoordinateMutable loc=(HasCoordinateMutable)line.c2();
        loc.setLocation(wFromPx.point(x, y));
        updateLineLocation(line);
    }
    
    //## CIRCLES ########
    
	public void addCircle(CircleMarker cm) {
		shapes.add(cm);
		int cx = pxFromW.xInt(cm.centerPoint.x());
		int cy = pxFromW.yInt(cm.centerPoint.y());
		double r = cm.radius().sizeInPixels(dca);//apxFromW.lengthInt(cm.radius());

		cm.el = vectorCanvas.addCircle(cx, cy, r, cm.style.getStrokeWidthPx(dca), cm.getStyle());
	}
    
	public void updateCircleGeometry(CircleMarker cm) {
		double cx = pxFromW.x(cm.centerPoint.x());
		double cy = pxFromW.y(cm.centerPoint.y());
		double r = cm.radius().sizeInPixels(dca);
		vectorCanvas.updateCircle(cm.el, cx, cy, r);
	}
	
	public void updateCircleSize(CircleMarker cm) {
		double cx = pxFromW.x(cm.centerPoint.x());
		double cy = pxFromW.y(cm.centerPoint.y());
		double r = cm.radius().sizeInPixels(dca);
		vectorCanvas.updateCircleSize(cm.el, cx, cy, r);
	}
	
	public void updateCircleCenter(CircleMarker cm) {
		double cx = pxFromW.x(cm.centerPoint.x());
		double cy = pxFromW.y(cm.centerPoint.y());
		double r = cm.radius().sizeInPixels(dca);
		vectorCanvas.updateCirclePos(cm.el, cx, cy, r);
	}

    //## RECTANGLES ########
	public void addRectangle(RectangleMarker cm) {
		shapes.add(cm);
		Envelope mbr = cm.getPositionData();
		int x1 = pxFromW.xInt(mbr.getMinX());
		int y1 = pxFromW.yInt(mbr.getMaxY());
		int x2 = pxFromW.xInt(mbr.getMaxX());
		int y2 = pxFromW.yInt(mbr.getMinY());
		cm.el = vectorCanvas.addRectangle(x1, y1, x2, y2, cm.style.getStrokeWidthPx(dca), cm.getStyle());
	}
    
	public void updateRectangleGeometry(RectangleMarker cm) {
		Envelope mbr = cm.getPositionData();
		int x1 = pxFromW.xInt(mbr.getMinX());
		int y1 = pxFromW.yInt(mbr.getMaxY());
		int x2 = pxFromW.xInt(mbr.getMaxX());
		int y2 = pxFromW.yInt(mbr.getMinY());
		vectorCanvas.updateRectangle(cm.el, x1, y1, x2, y2);
	}
}
