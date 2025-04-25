package com.sinergise.geopedia.rendering;

public class CoordRect
{
	public int SRID;
	
	public double minx, maxx, miny, maxy;
	
	public CoordRect()
	{
		// ...
	}
	
	public CoordRect(int SRID, double minx, double maxx, double miny, double maxy)
	{
		this.SRID = SRID;
		this.minx = minx;
		this.miny = miny;
		this.maxx = maxx;
		this.maxy = maxy;
	}
}
