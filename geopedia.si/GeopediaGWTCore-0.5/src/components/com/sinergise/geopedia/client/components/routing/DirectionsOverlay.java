/**
 * 
 */
package com.sinergise.geopedia.client.components.routing;

import java.util.Collection;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.sinergise.common.geometry.display.DisplayCoordinateAdapter;
import com.sinergise.common.geometry.display.GraphicMeasure;
import com.sinergise.common.geometry.geom.LineString;
import com.sinergise.common.gis.map.render.RenderInfo;
import com.sinergise.geopedia.client.components.routing.entities.Destination;
import com.sinergise.geopedia.client.components.routing.entities.Direction;
import com.sinergise.gwt.gis.map.ui.OverlayComponent;
import com.sinergise.gwt.gis.map.ui.vector.LineMarkerStyle;
import com.sinergise.gwt.gis.ui.gfx.Canvas;


/**
 * @author tcerovski
 */

public class DirectionsOverlay extends OverlayComponent<RenderInfo> {
	
	private DisplayCoordinateAdapter lastDCA;
	protected AbsolutePanel pnl;
	Direction direction = null;
	Destination[] suggestions = null;
	Canvas canvas = null;
	Element[] lineElems = null;
	public double minX=Double.MAX_VALUE;
	public double minY=Double.MAX_VALUE;
	public double maxX=Double.MIN_VALUE;
	public double maxY=Double.MIN_VALUE;
	protected RenderInfo myInfo = new RenderInfo();
	final static LineMarkerStyle STYLE_DIRECTIONS = new LineMarkerStyle("rgb(85, 127, 255)", GraphicMeasure.fixedPixels(4));
	
	public DirectionsOverlay(DisplayCoordinateAdapter dca) {		
		super(new AbsolutePanel());
		lastDCA = dca;
		pnl = (AbsolutePanel)getWidget();
	}

	
	private void updateDirectionPosition(DisplayCoordinateAdapter dca) {
		if(direction == null) {
			clearDirections();
			return;
		}

		LineString line = direction.getLine();
		for(int i=0; i<line.getNumCoords()-1; i++) {
			
			double x1 = dca.pixFromWorld.x(line.getX(i));
			double y1 = dca.pixFromWorld.y(line.getY(i));
			double x2 = dca.pixFromWorld.x(line.getX(i+1));
			double y2 = dca.pixFromWorld.y(line.getY(i+1));
			
			if (lineElems[i] == null) {
				lineElems[i] = canvas.addLine(x1, y1, x2, y2,  STYLE_DIRECTIONS.getStrokeWidthPx(dca), STYLE_DIRECTIONS);
			} else {
				canvas.updateLine(lineElems[i], x1, y1, x2, y2);
			}
		}
	}
	
	private void clearDirections() {
		direction = null;
		if(canvas != null) {
			pnl.remove(canvas);
			canvas = null;
		}
		lineElems = null;
	}
	
	public void clearSuggestions() {
	}
	
	public void clear() {
		clearSuggestions();
		clearDirections();
		pnl.clear();
		minX=Double.MAX_VALUE;
		minY=Double.MAX_VALUE;
		maxX=Double.MIN_VALUE;
		maxY=Double.MIN_VALUE;

	}
	
	private void initDirection() {
		canvas = Canvas.create(lastDCA.getDisplaySize());
		pnl.add(canvas);
	    
	    lineElems = new Element[direction.getLine().getNumCoords()-1];
	    
	}
	
	private void initSuggestions() {
		if(suggestions == null)
			return;
	}
	
	public void setDirection(Direction direction) {
		clear();
		LineString line = direction.getLine();
		for(int i=0; i<line.getNumCoords(); i++) {
			if (line.getX(i)<minX)
				minX=line.getX(i);
			if (line.getX(i)>maxX)
				maxX=line.getX(i);
			if (line.getY(i)<minY)
				minY=line.getY(i);
			if (line.getY(i)>maxY)
				maxY=line.getY(i);
		}

		this.direction = direction;
		initDirection();
		updateDirectionPosition(lastDCA);
	}
	
	public void setSuggestions(Collection<Destination> suggestions) {
		clearSuggestions();
		if(suggestions == null || suggestions.size() == 0)
			return;
		
		this.suggestions = suggestions.toArray(new Destination[suggestions.size()]);
		initSuggestions();
	}
	
	
	@Override
	public void reposition(RenderInfo info) {
		updateDirectionPosition(info.dca);
	}
	@Override
	public RenderInfo prepareToRender(DisplayCoordinateAdapter dca,
			boolean trans, boolean quick) {
		lastDCA=dca;
		 myInfo.quick=quick;
		 if (direction==null)
		    myInfo.hasAnything=false;
		 else
			myInfo.hasAnything=true;
		 myInfo.isTransparent=true;
		 return myInfo;
	}	
}
