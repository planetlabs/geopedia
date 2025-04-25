/*
 *
 */
package com.sinergise.gwt.gis.map.ui.vector;


import com.sinergise.common.geometry.util.GeomUtil;
import com.sinergise.common.util.geom.CoordinatePair;
import com.sinergise.common.util.geom.CoordinatePairMutable;
import com.sinergise.common.util.geom.HasCoordinate;
import com.sinergise.common.util.geom.LineSegment2D;

/**
 * This is only mutable if the provided CoordinatePair (data) is mutable
 * @author Miha
 */
public class LineMarker extends AbstractOverlayShape implements CoordinatePairMutable {
    private CoordinatePair data;
    
    public LineMarker(HasCoordinate start, HasCoordinate end, LineMarkerStyle style) {
        this(new LineSegment2D(start, end), style);
    }
    
    public LineMarker(CoordinatePair data, LineMarkerStyle style) {
    	super(style);
        this.data = data;
    	this.style = style;
	}
    
    public CoordinatePair getLocationData() {
    	return data;
    }

	public void updateLocation(HasCoordinate st, HasCoordinate en) {
    	((CoordinatePairMutable)data).setCoordinate1(st);
    	((CoordinatePairMutable)data).setCoordinate2(en);
    }
    
    @Override
	public HasCoordinate c1() {
    	return data.c1();
    }

	@Override
	public CoordinatePairMutable setCoordinate1(HasCoordinate c1) {
		((CoordinatePairMutable)data).setCoordinate1(c1);
		return this;
	}

	@Override
	public double x1() {
		return data.x1();
	}

	@Override
	public double y1() {
		return data.y1();
	}

	@Override
	public double x2() {
		return data.x2();
	}

	@Override
	public double y2() {
		return data.y2();
	}

	@Override
	public CoordinatePairMutable setCoordinate2(HasCoordinate c2) {
		((CoordinatePairMutable)data).setCoordinate2(c2);
		return this;
	}

	@Override
	public HasCoordinate c2() {
		return data.c2();
	}

	public double getLengthSq() {
		return GeomUtil.distanceSq(data.c1(), data.c2());
	}

	public double getLength() {
		return GeomUtil.distance(data.c1(), data.c2());
	}
    
}
