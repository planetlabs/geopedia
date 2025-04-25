/*
 *
 */
package com.sinergise.common.geometry.tiles;

import java.util.ArrayList;
import java.util.Iterator;

import com.sinergise.common.geometry.crs.hr.HRTransforms;
import com.sinergise.common.geometry.crs.mu.MauritiusTransforms;


public class TiledCRSMapping {
	public static final TiledCRSMapping INSTANCE = new TiledCRSMapping();
	static {
		INSTANCE.put(TiledCRS.GP_SLO);
		INSTANCE.put(HRTransforms.TILES_HR);
		INSTANCE.put(TiledCRS.WGS84_PPC_MIN);
		INSTANCE.put(TiledCRS.IMAGE_CRS_TILES);
		INSTANCE.put(MauritiusTransforms.TILES_MUS_SAMPLE);
	}
    private final ArrayList<TiledCRS> data = new ArrayList<TiledCRS>();
    public TiledCRSMapping() {
    }
    public TiledCRSMapping(TiledCRS crs) {
    		put(crs);
    }
    public TiledCRSMapping(TiledCRSMapping other) {
    		data.addAll(other.data);
    }
    public TiledCRSMapping(TiledCRS[] values) {
        for (int i = 0; i < values.length; i++) {
            put(values[i]);
        }
    }
    public boolean put(TiledCRS crs) {
        if (data.contains(crs)) return false;
        return data.add(crs);
    }
    public TiledCRS getByName(String name) {
    	for (TiledCRS c : data) {
				if (name.equals(c.name)) return c;
			}
      return null;
    }
    
    public TiledCRS getByPrefix(char prefix) {
    	for (TiledCRS c : data) {
				if (prefix == c.getTilePrefixChar()) return c;
			}
      return null;
    }
    
    public boolean containsName(String name) {
    	for (TiledCRS c : data) {
				if (name.equals(c.name)) return true;
			}
      return false;
    }
    
    public boolean contains(TiledCRS crs) {
    	return data.contains(crs);
    }

    public void intersectWith(TiledCRSMapping other) {
        for (Iterator<TiledCRS> it = data.iterator(); it.hasNext();) {
            TiledCRS haveIt = it.next();
            if (!other.contains(haveIt)) {
                it.remove();
            }
        }
    }
    
    public boolean isEmpty() {
        return data.isEmpty();
    }
    
    public void clear() {
        data.clear();
    }
}
