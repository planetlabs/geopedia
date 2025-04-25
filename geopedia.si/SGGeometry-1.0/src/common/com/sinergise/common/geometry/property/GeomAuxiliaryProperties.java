package com.sinergise.common.geometry.property;

import com.sinergise.common.geometry.crs.transform.Transform;
import com.sinergise.common.geometry.crs.transform.TransformUtil;
import com.sinergise.common.geometry.crs.transform.Transforms;
import com.sinergise.common.geometry.geom.Geometry;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.util.Util;
import com.sinergise.common.util.auxprops.AbstractAuxiliaryInfo;
import com.sinergise.common.util.crs.CrsIdentifier;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.geom.Position2D;
import com.sinergise.common.util.lang.Function;
import com.sinergise.common.util.state.gwt.DefaultStateOriginator;
import com.sinergise.common.util.state.gwt.StateGWT;


public class GeomAuxiliaryProperties extends DefaultStateOriginator {
	private static final long serialVersionUID = 1L;
    
	public static final String KEY_MBR = "MBR";
	public static final String KEY_POINT_INSIDE = "PointInside";
	public static final String KEY_MAX_INNER_CIRCLE = "MIC";
	public static final String KEY_AREA = "Area";
	public static final String KEY_LENGTH = "Length";

	public static final String KEY_TRANSFORMED_POINT_INSIDE = "TransformedPointInside";
	public static final String KEY_TRANSFORMED_MBR = "TransformedMbr";
	
	private Geometry displayGeometry;
	
    public GeomAuxiliaryProperties() {
        super();
    }
    
    public GeomAuxiliaryProperties(StateGWT st) {
        super(st);
    }
    
    @SuppressWarnings("unchecked")
	@Override
	protected <T> T wrap(String key, StateGWT st) {
		if (KEY_MBR.equals(key)) {
			return (T)new Envelope(st);
		}
		if (KEY_POINT_INSIDE.equals(key)) {
			Position2D ret = new Position2D();
			ret.loadInternalState(st);
			return (T)ret;
		}
		return super.<T>wrap(key, st);
	}

	public Envelope getMBR() {
    	return getWrapped(KEY_MBR);
    }
    
	public void setMBR(Envelope env) {
		setWrapped(KEY_MBR, env);
	}
	
	public Point getPointInside() {
		return getWrapped(KEY_POINT_INSIDE);
	}

	public void setPointInside(Point point) {
		setWrapped(KEY_POINT_INSIDE, point);
	}
	
	public Geometry getDisplayGeometry() {
		return displayGeometry;
	}
	
	public void setDisplayGeometry(Geometry displayGeom) {
		this.displayGeometry = displayGeom;
	}
	
	private Point getTransformedPointInside(){
		return getWrapped(KEY_TRANSFORMED_POINT_INSIDE);
	}
	
	//TODO reshuffle
	public Point getPointInside(CrsIdentifier crsId) {
		Point point = getTransformedPointInside();
		if (point == null) {
			point = getPointInside();
		}
		
		if (point == null) {
			return null; //TODO check all call hierarchy if null is handled properly
		}
		
		if (Util.safeEquals(point.getCrsId(), crsId)) {
			return point;
		}
		
		Transform<?, ?> tr = Transforms.find(point.getCrsId(), crsId);
		if (tr == null) {
			throw new RuntimeException("Cannot find CRS transform from: "+point.getCrsId()+" to: "+ crsId);
		}
		TransformUtil.transformPoint(tr, point);
		setTransormedPointInside(point);
		
		return point;
	}
	
	private void setTransormedPointInside(Point point) {
		setWrapped(KEY_TRANSFORMED_POINT_INSIDE, point);
	}

	private Envelope getTransformedMbr(){
		return getWrapped(KEY_TRANSFORMED_MBR);
	}
	
	public Envelope getMbr(CrsIdentifier crsId) {
		Envelope env = getTransformedMbr();
		if (env == null) {
			env = getMBR();
		}
		
		if (env == null) {
			return null; //check all call hierarchy if null is handled properly
		}
		
		if (Util.safeEquals(env.getCrsId(), crsId)) {
			return env;
		}
		
		Transform<?, ?> tr = Transforms.find(env.getCrsId(), crsId);
		if (tr == null) {
			throw new RuntimeException("Cannot find CRS transform from: "+env.getCrsId()+" to: "+ crsId);
		}
		
		Envelope transformed = TransformUtil.transformEnvelope(tr, env);
		setTransormedMbr(transformed);
		
		return transformed;
	}
	
	private void setTransormedMbr(Envelope env) {
		setWrapped(KEY_TRANSFORMED_MBR, env);
	}

    public static GeomAuxiliaryProperties getFrom(AbstractAuxiliaryInfo parent, String key) {
		return (GeomAuxiliaryProperties)DefaultStateOriginator.getOrCreateWrapper(parent, key, //
			new Function<StateGWT, GeomAuxiliaryProperties>(){
				@Override
				public GeomAuxiliaryProperties execute(StateGWT param) {
					return new GeomAuxiliaryProperties(param);
				}
		});
    }

}
