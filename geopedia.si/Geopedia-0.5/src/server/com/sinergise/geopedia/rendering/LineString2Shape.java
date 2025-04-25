package com.sinergise.geopedia.rendering;

import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;

import com.sinergise.common.geometry.geom.LineString;
import com.sinergise.geopedia.db.geometry.WkbReader;


public class LineString2Shape extends AbstractSgShape 
{
	private LineString data;
	
	public void setData(LineString linestring, WkbReader wkb)
	{
		data = linestring;
		if (wkb!=null) {
			setEnvelope(wkb.getBounds());
		}
	}
	
	public void setData(LineString ls) {
		data = ls;
		setEnvelope(ls.getEnvelope());
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
		private LineString data;
		int cpos = 0;
		
		public DirectPathIterator(LineString data)
		{
			this.data = data;
		}

		public int currentSegment(double[] coords)
        {
			LineString cur = data;
			
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
			LineString cur = data;
			
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
			return cpos >= data.coords.length;
        }

		public void next()
        {
			cpos += 2;
        }
	}

	public static class TransformingPathIterator implements PathIterator
	{
		private LineString data;
		private double m00, m01, m02, m10, m11, m12;
		int cpos = 0;
		
		public TransformingPathIterator(LineString data, AffineTransform xform)
		{
			this.data = data;
			this.m00 = xform.getScaleX();
			this.m01 = xform.getShearX();
			this.m02 = xform.getTranslateX();
			this.m10 = xform.getShearY();
			this.m11 = xform.getScaleY();
			this.m12 = xform.getTranslateY();
		}

		@Override
		public int currentSegment(double[] coords)
        {
			LineString cur = data;
			
			coords[0] = m02 + m00 * cur.coords[cpos] + m01 * cur.coords[cpos+1];
			coords[1] = m12 + m10 * cur.coords[cpos] + m11 * cur.coords[cpos+1];
			
			if (cpos == 0) {
				return SEG_MOVETO;
			}
			return SEG_LINETO;
        }

		@Override
		public int currentSegment(float[] coords)
        {
			LineString cur = data;
			
			coords[0] = (float) (m02 + m00 * cur.coords[cpos] + m01 * cur.coords[cpos+1]);
			coords[1] = (float) (m12 + m10 * cur.coords[cpos] + m11 * cur.coords[cpos+1]);
			
			if (cpos == 0) {
				return SEG_MOVETO;
			}
			return SEG_LINETO;
        }

		@Override
		public int getWindingRule()
        {
			return WIND_EVEN_ODD;
        }

		@Override
		public boolean isDone()
        {
			return cpos >= data.coords.length;
        }

		@Override
		public void next()
        {
			cpos += 2;
        }
	}
}
