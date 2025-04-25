package com.sinergise.geopedia.rendering;

import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;

import com.sinergise.common.geometry.geom.LinearRing;
import com.sinergise.common.geometry.geom.Polygon;
import com.sinergise.geopedia.db.geometry.WkbReader;


public class Polygon2Shape extends AbstractSgShape
{
	private Polygon data;

	public void setData(Polygon polygon, WkbReader wkb)
	{
		data = polygon;
		if (wkb!=null) {
			setEnvelope(wkb.getBounds());
		}
	}
	
	public PathIterator getPathIterator(AffineTransform at, double flatness)
	{
		return getPathIterator(at);
	}
	
	public PathIterator getPathIterator(AffineTransform addAt)
	{
		if (addAt == null || addAt.isIdentity())
			return new DirectPathIterator(data);
		
		return new TransformingPathIterator(data, addAt);
	}
	
	public static class DirectPathIterator implements PathIterator
	{
		private Polygon data;
		int atRing = -1;
		int cpos = -2;
		
		public DirectPathIterator(Polygon data)
		{
			this.data = data;
			next();
		}

		public int currentSegment(double[] coords)
        {
			LinearRing cur = atRing < 0 ? data.outer : data.getHole(atRing);
			
			coords[0] = cur.coords[cpos];
			coords[1] = cur.coords[cpos+1];
			
			if (cpos == 0) {
				return SEG_MOVETO;
			} else
			if (cpos == cur.coords.length-2) {
				return SEG_CLOSE;
			} else {
				return SEG_LINETO;
			}
        }

		public int currentSegment(float[] coords)
        {
			LinearRing cur = atRing < 0 ? data.outer : data.getHole(atRing);
			
			coords[0] = (float) cur.coords[cpos];
			coords[1] = (float) cur.coords[cpos+1];
			
			if (cpos == 0) {
//				System.out.println("MOVE TO "+coords[0]+" "+coords[1]+"    (from "+cur.coords[cpos]+" "+cur.coords[cpos+1]+")");
				return SEG_MOVETO;
			} else
			if (cpos == cur.coords.length-2) {
//				System.out.println("CLOSE TO "+coords[0]+" "+coords[1]+"    (from "+cur.coords[cpos]+" "+cur.coords[cpos+1]+")");
				return SEG_CLOSE;
			} else {
//				System.out.println("LINE TO "+coords[0]+" "+coords[1]+"    (from "+cur.coords[cpos]+" "+cur.coords[cpos+1]+")");
				return SEG_LINETO;
			}
        }

		public int getWindingRule()
        {
			return WIND_EVEN_ODD;
        }

		public boolean isDone()
        {
			return atRing >= data.getNumHoles();
        }

		public void next()
        {
			cpos += 2;
			while (true) {
				if (atRing >= data.getNumHoles())
					return;
				LinearRing cur = atRing < 0 ? data.outer : data.getHole(atRing);
				if (cpos >= cur.coords.length) {
					cpos = 0;
					atRing++;
				} else {
					return;
				}
			}
        }
	}

	public static class TransformingPathIterator implements PathIterator
	{
		private Polygon data;
		private double m00, m01, m02, m10, m11, m12;
		int atRing = -1;
		int cpos = -2;
		
		public TransformingPathIterator(Polygon data, AffineTransform xform)
		{
			this.data = data;
			this.m00 = xform.getScaleX();
			this.m01 = xform.getShearX();
			this.m02 = xform.getTranslateX();
			this.m10 = xform.getShearY();
			this.m11 = xform.getScaleY();
			this.m12 = xform.getTranslateY();
			next();
		}

		public int currentSegment(double[] coords)
        {
			LinearRing cur = atRing < 0 ? data.outer : data.getHole(atRing);
			
			coords[0] = m02 + m00 * cur.coords[cpos] + m01 * cur.coords[cpos+1];
			coords[1] = m12 + m10 * cur.coords[cpos] + m11 * cur.coords[cpos+1];
			
			if (cpos == 0) {
				return SEG_MOVETO;
			} else
			if (cpos == cur.coords.length-2) {
				return SEG_CLOSE;
			} else {
				return SEG_LINETO;
			}
        }

		public int currentSegment(float[] coords)
        {
			LinearRing cur = atRing < 0 ? data.outer : data.getHole(atRing);
			
			coords[0] = (float) (m02 + m00 * cur.coords[cpos] + m01 * cur.coords[cpos+1]);
			coords[1] = (float) (m12 + m10 * cur.coords[cpos] + m11 * cur.coords[cpos+1]);
			
			if (cpos == 0) {
				//System.out.println("MOVE TO "+coords[0]+" "+coords[1]+"    (from "+cur.coords[cpos]+" "+cur.coords[cpos+1]+")");
				return SEG_MOVETO;
			} else
			if (cpos == cur.coords.length-2) {
				//System.out.println("CLOSE TO "+coords[0]+" "+coords[1]+"    (from "+cur.coords[cpos]+" "+cur.coords[cpos+1]+")");
				return SEG_CLOSE;
			} else {
				//System.out.println("LINE TO "+coords[0]+" "+coords[1]+"    (from "+cur.coords[cpos]+" "+cur.coords[cpos+1]+")");
				return SEG_LINETO;
			}
        }

		public int getWindingRule()
        {
			return WIND_EVEN_ODD;
        }

		public boolean isDone()
        {
			return atRing >= data.getNumHoles();
        }

		public void next()
        {
			cpos += 2;
			while (true) {
				if (atRing >= data.getNumHoles())
					return;
				LinearRing cur = atRing < 0 ? data.outer : data.getHole(atRing);
				if (cpos >= cur.coords.length) {
					cpos = 0;
					atRing++;
				} else {
					return;
				}
			}
        }
	}
}
