package com.sinergise.gwt.gis.map.ui.vector;

import com.sinergise.common.util.geom.HasCoordinate;
import com.sinergise.common.util.geom.Position2D;

public class TextPositionFromLine implements TextPosition{
		public LineMarker lineMarker;
		TextPositionFromLine(LineMarker lm){
			lineMarker = lm;
		}

		@Override
		public HasCoordinate getCoordinates() {
			double x = 0.5*(lineMarker.x1()+lineMarker.x2());
			double y = 0.5*(lineMarker.y1()+lineMarker.y2());
			return new Position2D(x,y);
		}

		@Override
		public float getRotation() {
			
			boolean flipRotation = ( lineMarker.x2()-lineMarker.x1() > 0);
			if(flipRotation){
				return (float) Math.toDegrees(-Math.atan2(lineMarker.y2()-lineMarker.y1(), lineMarker.x2()-lineMarker.x1()));
			}
			return (float) Math.toDegrees(-Math.atan2(lineMarker.y1()-lineMarker.y2(), lineMarker.x1()-lineMarker.x2()));			
		}

}
