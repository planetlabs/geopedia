package com.sinergise.geopedia.rendering;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import com.sinergise.common.util.geom.Envelope;
import com.sinergise.java.geometry.util.APIMapping;

public abstract class AbstractSgShape implements Shape {
	private Envelope env;

	@Override
	public Rectangle getBounds()
	{
		return APIMapping.toJ2D(env.roundOutside());
	}

	@Override
	public Rectangle2D getBounds2D()
	{
		return APIMapping.toJ2D(env);
	}

	@Override
	public boolean contains(double x, double y)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean contains(double x, double y, double w, double h)
	{
		return env.contains(x, y, x+w, y+h);
	}

	@Override
	public boolean contains(Point2D p)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean contains(Rectangle2D r)
	{
		return env.contains(r.getMinX(), r.getMinY(), r.getMaxX(), r.getMaxY());
	}

	@Override
	public boolean intersects(double minX, double minY, double w, double h)
	{
		return env.intersects(minX, minY, minX+w, minY+h);
	}

	@Override
	public boolean intersects(Rectangle2D r)
	{
		return env.intersects(r.getMinX(), r.getMinY(), r.getMaxX(), r.getMaxY());
	}
	
	protected void setEnvelope(Envelope envelope) {
		this.env = envelope;
	}
}
