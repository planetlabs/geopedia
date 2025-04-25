/**
 * 
 */
package com.sinergise.common.geometry.crs;

import java.util.ArrayList;
import java.util.HashMap;

import com.sinergise.common.geometry.crs.transform.Transform;
import com.sinergise.common.geometry.crs.transform.Transforms;
import com.sinergise.common.geometry.util.CoordinateFormat;


public class CrsDescriptor {
	public final CRS system;
	public String name;
	public CoordinateFormat coordFormat;
	private HashMap<CRS, Transform<?, ?>> transforms;
	
	public CrsDescriptor(String name, CRS system) {
		this.name=name;
		this.system=system;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends CRS> Transform<?, T>[] getTransforms(T[] targets) {
		if (transforms==null) {
			transforms = new HashMap<CRS, Transform<?,?>>();
		}
		ArrayList<Transform<?,T>> ret=new ArrayList<Transform<?, T>>(targets.length);
		for (T tgtCS : targets) {
			Transform<?, T> tr = (Transform<?, T>) transforms.get(tgtCS);
			if (tr == null) {
				tr = Transforms.find(system, tgtCS);
				if (tr!=null) {
					transforms.put(tgtCS, tr);
				}
			}
			if (tr!=null) {
				ret.add(tr);
			}
		}
		return ret.toArray(new Transform[ret.size()]);
	}
	
	@Override
	public String toString() {
		if (name != null && name.length() > 0) {
			return name;
		}
		return String.valueOf(system);
	}
}