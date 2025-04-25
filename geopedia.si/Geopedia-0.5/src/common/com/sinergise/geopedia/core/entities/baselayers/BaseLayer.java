package com.sinergise.geopedia.core.entities.baselayers;

import java.io.Serializable;
import java.util.ArrayList;

import com.sinergise.geopedia.core.config.Copyright;

public abstract class BaseLayer implements Serializable {
	private static final long serialVersionUID = 8311901185461472835L;
	public static enum Type {TILED,WMS}
	
	
	public BaseLayer addCopyright(Copyright copyright, int toLevel) {
		copyrightList.add(copyright);
		copyrightListScales.add(toLevel);
		return this;
		
	}
	public BaseLayer addCopyright(Copyright copyright) {
		addCopyright(copyright,getMaxScaleLevel());
		return this;
		
	}

	protected BaseLayer(Type type) {
		this.type=type;
	}
	protected Type type;
	public int id;
	public String name;
	public String description;
	
	private ArrayList<Copyright> copyrightList = new ArrayList<Copyright>();
	private ArrayList<Integer> copyrightListScales = new ArrayList<Integer>();
	public String[] copyrights;
	public int[] copyrightScales;

	@Override
	public String toString() {
		return name;
	}
	
	
	public abstract int getMaxScaleLevel();

	
	public ArrayList<Copyright> getCopyrights(int scale) {
		ArrayList<Copyright> list = new ArrayList<Copyright>();
 		for (int i=0;i<copyrightListScales.size();i++) {
			if (scale <=copyrightListScales.get(i)) {
				list.add(copyrightList.get(i));
			}
		}
 		return list;
	}
}
