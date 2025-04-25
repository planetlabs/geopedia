package com.sinergise.geopedia.geometry.height;

import java.util.ArrayList;


class LimitedCurve
{
	CubicSplineInterpolator interpolator;
	double minx, maxx;
	
	public LimitedCurve(CubicSplineInterpolator interpolator, double minx, double maxx)
	{
		this.minx = minx;
		this.maxx = maxx;
		this.interpolator = interpolator;
	}
	
	public double eval(double x)
	{
		return interpolator.eval(x);
	}
	
	public double gradient(double x)
	{
		return interpolator.derivative1(x);
	}
}

class CurveSet
{
	private LimitedCurve[] curves;

	public CurveSet(LimitedCurve[] curves)
	{
		this.curves = curves;
	}

	public boolean isDefinedAt(double x)
    {
		int n = curves.length;
		for (int a=0; a<n; a++) {
			LimitedCurve c = curves[a];
			if (c.minx <= x && c.maxx >= x)
				return true;
		}
		
		return false;
    }

	public double evalAt(double x)
    {
		int n = curves.length;
		for (int a=0; a<n; a++) {
			LimitedCurve c = curves[a];
			if (c.minx <= x && c.maxx >= x)
				return c.interpolator.eval(x);
		}
		
		throw new IllegalStateException();
    }
}
/**
 * assumes DEM z resolution of 0.1m
 * 
 * @author mslenc
 */
public class Interpolator
{
	CurveSet[] horizontals, verticals;
	DMVHolder dmv;
	
	public Interpolator(DMVHolder dmv)
	{
		this.dmv = dmv;
		
		horizontals = new CurveSet[(int) (dmv.maxy-dmv.miny+1)];
		
		for (long y = dmv.miny; y <= dmv.maxy; y++) {
			ArrayList<LimitedCurve> curves = new ArrayList<LimitedCurve>();
			long xpos = dmv.minx;
			while (xpos <= dmv.maxx) {
				if (dmv.get(xpos, y) == Short.MIN_VALUE) {
					xpos++;
					continue;
				}
				int len = 1;
				while (xpos + len <= dmv.maxx && dmv.get(xpos+len, y) != Short.MIN_VALUE)
					len++;
				
				alloced(len);
				
				CubicSplineInterpolator interpolator = new CubicSplineInterpolator();
				
				double[] poss = new double[len];
				double[] vals = new double[len];
				for (int a=0; a<len; a++) {
					poss[a] = (xpos + a) * dmv.step;
					vals[a] = dmv.get(xpos+a, y) * 0.1;
				}
				interpolator.setData(poss, vals);
				
				LimitedCurve curve = new LimitedCurve(interpolator, xpos * dmv.step, (xpos + len - 1) * dmv.step);
				
				curves.add(curve);
				
				xpos += len;
			}
			
			CurveSet set = new CurveSet(curves.toArray(new LimitedCurve[curves.size()]));
			horizontals[(int) (y - dmv.miny)] = set;
		}
		
		verticals = new CurveSet[(int) (dmv.maxx-dmv.minx+1)];
		
		for (long x = dmv.minx; x <= dmv.maxx; x++) {
			ArrayList<LimitedCurve> curves = new ArrayList<LimitedCurve>();
			long ypos = dmv.miny;
			while (ypos <= dmv.maxy) {
				if (dmv.get(x, ypos) == Short.MIN_VALUE) {
					ypos++;
					continue;
				}
				int len = 1;
				while (ypos + len <= dmv.maxy && dmv.get(x, ypos + len) != Short.MIN_VALUE)
					len++;
				
				CubicSplineInterpolator interpolator = new CubicSplineInterpolator();
				
				double[] poss = new double[len];
				double[] vals = new double[len];
				for (int a=0; a<len; a++) {
					poss[a] = (ypos + a) * dmv.step;
					vals[a] = dmv.get(x, ypos+a) * 0.1;
				}
				interpolator.setData(poss, vals);
				
				LimitedCurve curve = new LimitedCurve(interpolator, ypos * dmv.step, (ypos + len - 1) * dmv.step);
				
				curves.add(curve);
				
				ypos += len;
			}
			
			CurveSet set = new CurveSet(curves.toArray(new LimitedCurve[curves.size()]));
			verticals[(int) (x - dmv.minx)] = set;
		}
	}
	
	long total = 0;
	
	private void alloced(int len)
    {
		total += len;
    }

	public boolean eval(double x, double y, double[] out)
	{
		long lleftx = (long)Math.floor(x / dmv.step);
		if (lleftx < dmv.minx || lleftx > dmv.maxx)
			return false;
			
		int leftx = (int) (lleftx - dmv.minx);
		if (!verticals[leftx].isDefinedAt(y))
			return false;
		
		long lrightx = (long)Math.ceil(x / dmv.step);
		if (lrightx > dmv.maxx || lrightx < dmv.minx)
			return false;
		
		int rightx = (int)(lrightx - dmv.minx);
		if (!verticals[rightx].isDefinedAt(y))
			return false;
		
		long ldowny = (long)Math.floor(y / dmv.step);
		if (ldowny < dmv.miny || ldowny > dmv.maxy)
			return false;
		
		int downy = (int) (ldowny - dmv.miny);
		if (!horizontals[downy].isDefinedAt(x))
			return false;
		
		long lupy = (long)Math.ceil(y / dmv.step);
		if (lupy > dmv.maxy || ldowny < dmv.miny)
			return false;
		
		int upy = (int)(lupy - dmv.miny);
		if (!horizontals[upy].isDefinedAt(x))
			return false;

		int nleft = 1;
		while (nleft < 8 && leftx - nleft >= 0 && verticals[leftx - nleft].isDefinedAt(y))
			nleft++;
		
		int nright = 1;
		while (nright < 8 && rightx + nright < verticals.length && verticals[rightx + nright].isDefinedAt(y))
			nright++;
		
		int minx = leftx - nleft + 1;
		int maxx = rightx + nright - 1;

		int numSamplesX = maxx - minx + 1;
		
		double[] horizPos = new double[numSamplesX];
		double[] horizVal = new double[numSamplesX];
		
		for (int a=0; a<numSamplesX; a++) {
			horizPos[a] = (minx + a + dmv.minx) * dmv.step;
			horizVal[a] = verticals[minx + a].evalAt(y);
		}
		
		CubicSplineInterpolator csi = new CubicSplineInterpolator();
		csi.setData(horizPos, horizVal);
		out[0] = csi.eval(x);
		out[1] = csi.derivative1(x);
		
		int ndown = 1;
		while (ndown < 8 && downy - ndown >= 0 && horizontals[downy - ndown].isDefinedAt(x))
			ndown++;
		
		int nup = 1;
		while (nup < 8 && upy + nup < horizontals.length && horizontals[upy + nup].isDefinedAt(x))
			nup++;
		
		int miny = downy - ndown + 1;
		int maxy = upy + nup - 1;

		int numSamplesY = maxy - miny + 1;
		
		double[] vertPos = new double[numSamplesY];
		double[] vertVal = new double[numSamplesY];
		for (int a=0; a<numSamplesY; a++) {
			vertPos[a] = (miny + a + dmv.miny) * dmv.step;
			vertVal[a] = horizontals[miny + a].evalAt(x);
		}
		
		csi.setData(vertPos, vertVal);
		out[2] = csi.derivative1(y);
		
		return true;
	}
}
