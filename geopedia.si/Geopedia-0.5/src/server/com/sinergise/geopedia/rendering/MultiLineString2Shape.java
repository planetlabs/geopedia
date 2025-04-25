package com.sinergise.geopedia.rendering;

import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;

import com.sinergise.common.geometry.geom.LineString;
import com.sinergise.common.geometry.geom.MultiLineString;
import com.sinergise.geopedia.db.geometry.WkbReader;


public class MultiLineString2Shape extends AbstractSgShape
{
	private MultiLineString data;

	public void setData(MultiLineString polygon, WkbReader wkb)
	{
		data = polygon;
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
		private MultiLineString data;
		int atRing = 0;
		int cpos = -2;
		
		public DirectPathIterator(MultiLineString data)
		{
			this.data = data;
			next();
		}

		public int currentSegment(double[] coords)
        {
			LineString cur = data.get(atRing);
			
			coords[0] = cur.coords[cpos];
			coords[1] = cur.coords[cpos+1];
			
			if (cpos == 0) {
				return SEG_MOVETO;
			} else {
				return SEG_LINETO;
			}
        }

		public int currentSegment(float[] coords)
        {
			LineString cur = data.get(atRing);
			
			coords[0] = (float) cur.coords[cpos];
			coords[1] = (float) cur.coords[cpos+1];
			
			if (cpos == 0) {
				return SEG_MOVETO;
			} else {
				return SEG_LINETO;
			}
        }

		public int getWindingRule()
        {
			return WIND_EVEN_ODD;
        }

		public boolean isDone()
        {
			return atRing >= data.size();
        }

		public void next()
        {
			cpos += 2;
			while (true) {
				if (atRing >= data.size())
					return;
				LineString cur = data.get(atRing);
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
		private MultiLineString data;
		private double m00, m01, m02, m10, m11, m12;
		int atRing = 0;
		int cpos = -2;
		
		public TransformingPathIterator(MultiLineString data, AffineTransform xform)
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
			LineString cur = data.get(atRing);
			
			coords[0] = m02 + m00 * cur.coords[cpos] + m01 * cur.coords[cpos+1];
			coords[1] = m12 + m10 * cur.coords[cpos] + m11 * cur.coords[cpos+1];
			
			if (cpos == 0) {
				return SEG_MOVETO;
			} else {
				return SEG_LINETO;
			}
        }

		public int currentSegment(float[] coords)
        {
			LineString cur = data.get(atRing);
			
			coords[0] = (float) (m02 + m00 * cur.coords[cpos] + m01 * cur.coords[cpos+1]);
			coords[1] = (float) (m12 + m10 * cur.coords[cpos] + m11 * cur.coords[cpos+1]);
			
			if (cpos == 0) {
				return SEG_MOVETO;
			} else {
				return SEG_LINETO;
			}
        }

		public int getWindingRule()
        {
			return WIND_EVEN_ODD;
        }

		public boolean isDone()
        {
			return atRing >= data.size();
        }

		public void next()
        {
			cpos += 2;
			while (true) {
				if (atRing >= data.size())
					return;
				LineString cur = data.get(atRing);
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
