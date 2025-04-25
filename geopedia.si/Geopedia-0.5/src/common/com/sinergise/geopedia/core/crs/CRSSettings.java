package com.sinergise.geopedia.core.crs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;

import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.common.geometry.crs.LatLonCRS;
import com.sinergise.common.geometry.crs.transform.AbstractTransform.SwapLatLon;
import com.sinergise.common.geometry.crs.transform.Transform;
import com.sinergise.common.geometry.crs.transform.Transforms;
import com.sinergise.common.geometry.tiles.WithBounds;
import com.sinergise.common.util.crs.CrsIdentifier;

public abstract class CRSSettings {
	private HashMap<CrsIdentifier, CRS> coordinateSystems = new LinkedHashMap<CrsIdentifier, CRS>();
	private ArrayList<TransformHolder> transforms = new ArrayList<TransformHolder>();

	

	public CRS getCoordinateSystem(CrsIdentifier crsSrid) {
		return coordinateSystems.get(crsSrid);
	}

	private class TransformHolder {
		public TransformHolder(CrsIdentifier fromCRSsrid, CrsIdentifier toCRSsrid, Transform<?,?> transform) {
			this.toCRSsrid=toCRSsrid;
			this.fromCRSsrid=fromCRSsrid;
			this.transform = transform;
		}
		
		public CrsIdentifier fromCRSsrid;
		public CrsIdentifier toCRSsrid;
		public Transform<?, ?> transform;
		
		public CRS getFromCRS() {
			return coordinateSystems.get(fromCRSsrid);
		}
		
		public CRS getToCRS() {
			return coordinateSystems.get(toCRSsrid);
		}
		
	}
	
	public Collection<CRS> getCoordinateSystems() {
		return coordinateSystems.values();
	}
	
	
	public Collection<CRS> getFromCRSTransformCapabilities (CrsIdentifier fromCRSsrid) {
		ArrayList<CRS> list = new ArrayList<CRS>();
		for (TransformHolder holder:transforms) {
			if (holder.fromCRSsrid.equals(fromCRSsrid)) {
				list.add(holder.getToCRS());
			}
		}
		return list;
	}
	
	public Collection<CRS> getToCRSTransformCapabilities (CrsIdentifier toCRSsrid) {
		ArrayList<CRS> list = new ArrayList<CRS>();
		for (TransformHolder holder:transforms) {
			if (holder.toCRSsrid.equals(toCRSsrid)) {
				list.add(holder.getFromCRS());
			}
		}
		return list;
	}
	
	public Transform<?, ?> getTransform(CRS from, CRS to) {
		return getTransform(from, to, false);
	}	
	//TODO: Remove swap parameter as it doesn't seem to belong here - order of ordinates should be defined by the target CRS alone
	public Transform<?, ?> getTransform(CRS from, CRS to, boolean swapLatLon) {
		return getTransform(from.getDefaultIdentifier(), to.getDefaultIdentifier(), swapLatLon);
	}
	//TODO: Remove swap parameter as it doesn't seem to belong here - order of ordinates should be defined by the target CRS alone
	public Transform<?, ?> getTransform(CrsIdentifier fromCRSsrid, CrsIdentifier toCRSsrid, boolean swapLatLon) {
		for (TransformHolder holder:transforms) {
			if (holder.fromCRSsrid.equals(fromCRSsrid) && holder.toCRSsrid.equals(toCRSsrid)) {
				return holder.transform;
			}
		}
		return null;
	}
	
	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected static Transform swapCoordinates (Transform<? extends CRS, ? extends CRS> crsTransform) {
		if (crsTransform==null) return crsTransform;
		if (crsTransform.getTarget() instanceof LatLonCRS) {			
			SwapLatLon swap = new SwapLatLon((LatLonCRS) crsTransform.getTarget());
			return Transforms.compose(crsTransform, swap);			
		} else if (crsTransform.getSource() instanceof LatLonCRS){ 
			SwapLatLon swap = new SwapLatLon((LatLonCRS) crsTransform.getSource());
			return  Transforms.compose(swap,crsTransform);			
		} else {
			return crsTransform;
		}
	}
	
	
	
	protected void addCRS(CRS crs) {
		assert !(coordinateSystems.containsKey(crs.getDefaultIdentifier())) : "srid "+crs.getDefaultIdentifier()+" already exists for other CRS!";
		coordinateSystems.put(crs.getDefaultIdentifier(), crs);
	}
	
	protected void addTransform(Transform<?,?> transform) {
		addTransform(transform, transform.getSource(), transform.getTarget());
	}
	
	protected void addTransform(Transform<?,?> transform, CRS fromCRS, CRS toCRS) {
		addTransform(transform, fromCRS.getDefaultIdentifier(), toCRS.getDefaultIdentifier());
	}
	protected void addTransform(Transform<?,?> transform, CrsIdentifier fromCRSsrid, CrsIdentifier toCRSsrid) {
		assert (coordinateSystems.get(fromCRSsrid) != null) : "Unable to find CRS for fromCRSsrid";
		assert (coordinateSystems.get(toCRSsrid) != null) : "Unable to find CRS for toCRSsrid";		
		transforms.add(new TransformHolder(fromCRSsrid, toCRSsrid, transform));
	}
	
	
	public abstract WithBounds getMainCRS();
	
	public CrsIdentifier getMainCrsId() {
		return getMainCRS().baseCRS.getDefaultIdentifier();
	}

}
