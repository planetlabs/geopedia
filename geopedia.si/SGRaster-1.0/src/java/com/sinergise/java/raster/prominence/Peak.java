package com.sinergise.java.raster.prominence;

import java.util.Comparator;

public class Peak implements Comparator<Peak>{
	long x;
	long y;
	int height;
	int prominence = 0;
	Peak parent = null;
	
	public Peak(long x, long y, int v0){
		this.x = x;
		this.y = y;
		this.height = v0;
	}
	
	public Peak(long x, long y, int height, int prominence){
		this(x, y, height);
		this.prominence = prominence;
	}
	
	public Peak(long x, long y, int height, int prominence, Peak parent){
		this(x, y, height, prominence);
		this.parent = parent;
	}
	
	public long getX(){
		return this.x;
	}
	
	public long getY(){
		return this.y;
	}
	
	public int getHeight(){
		return this.height;
	}
	
	public int getProminence(){
		return this.prominence;
	}
	
	public void setProminence(int prom){
		this.prominence = prom;
	}
	
	public Peak getParent(){
		return this.parent;
	}
	
	public void setParent(Peak peak){
		this.parent = peak;
	}

	@Override
	public int compare(Peak o1, Peak o2) {
		return (o2.getHeight() < o1.getHeight()) ? 1 : -1;
	}
}
