package com.sinergise.geopedia.geometry.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.image.BufferedImage;

import com.sinergise.common.geometry.geom.Geometry;
import com.sinergise.common.geometry.geom.MultiPolygon;
import com.sinergise.common.geometry.geom.Polygon;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.geopedia.rendering.MultiPolygon2Shape;
import com.sinergise.geopedia.rendering.Polygon2Shape;


public class CentroidFinder
{
	public static double[] calc(Geometry geom)
	{
		if (!(geom instanceof Polygon || geom instanceof MultiPolygon))
			return null;

		Envelope e = geom.getEnvelope();
		if (e.isEmpty())
			return null;

		Shape original;
		if (geom instanceof Polygon) {
			Polygon2Shape poly = new Polygon2Shape();
			poly.setData((Polygon)geom, null);
			original = poly;
		} else {
			MultiPolygon2Shape poly = new MultiPolygon2Shape();
			poly.setData((MultiPolygon)geom, null);
			original = poly;
		}
		
		double origWidth = e.getWidth();
		double origHeight = e.getHeight();
		double coordScale = Math.sqrt(15000.0 / (origWidth * origHeight));
		
		int imgWidth = (int) Math.ceil(coordScale * origWidth);
		int imgHeight = (int) Math.ceil(5 * coordScale * origHeight);
		
		if (imgWidth < 1 || imgHeight < 1)
			return null;
		
		double scaleX = imgWidth / origWidth;
		double scaleY = imgHeight / origHeight;
		double trX = -scaleX * e.getMinX();
		double trY = -scaleY * e.getMinY();
		
		AffineTransform xform = new AffineTransform(scaleX, 0, 0, scaleY, trX, trY);
		
		Shape draw = xform.createTransformedShape(original);
		
		BufferedImage img = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_BYTE_GRAY);
		Graphics2D g = img.createGraphics();
		g.setColor(Color.WHITE);
		
		g.fill(draw);
		
		int biggest = 0;
		int bigX = -1, bigY = -1;
		int[] prevRow = new int[imgWidth];
		int[] thisRow = new int[imgWidth];

		for (int x=0; x<imgWidth; x++) {
			if ((img.getRGB(x, 0) & 255) != 0) {
				prevRow[x] = 1;
				if (biggest == 0) {
					bigX = x;
					bigY = 0;
					biggest = 1;
				}
			}
		}
		for (int y=1; y<imgHeight; y++) {
			if ((img.getRGB(0, y) & 255) != 0) {
				thisRow[0] = 1;
				if (biggest == 0) {
					bigX = 0;
					bigY = y;
					biggest = 1;
				}
			}
			for (int x=1; x<imgWidth; x++) {
				if ((img.getRGB(x, y) & 255) != 0) {
					int min = thisRow[x-1];
					int other;
					if ((other = prevRow[x]) < min) min = other;
					if ((other = prevRow[x-1]) < min) min = other;
					if ((thisRow[x] = 1 + min) > biggest) {
						bigX = x;
						bigY = y;
						biggest = 1 + min;
					}
				} else {
					thisRow[x] = 0;
				}
			}
			int[] tmp = prevRow;
			prevRow = thisRow;
			thisRow = tmp;
		}
		
		if (biggest == 0)
			return null;
		
        double[] out = { bigX - 0.5 * biggest, bigY - 0.5 * biggest };

		AffineTransform inv;
		try {
	        inv = xform.createInverse();
        } catch (NoninvertibleTransformException e1) {
        	return null;
        }
        
        inv.transform(out, 0, out, 0, 1);
        
        return out;
	}
}
