package com.sinergise.common.geometry.io;

import com.sinergise.common.geometry.crs.CRS;
import com.sinergise.common.geometry.crs.LatLonCRS;
import com.sinergise.common.geometry.crs.transform.AbstractTransform.Identity;
import com.sinergise.common.geometry.crs.transform.AbstractTransform.SwapLatLon;
import com.sinergise.common.geometry.crs.transform.Transform;
import com.sinergise.common.geometry.crs.transform.Transforms;
import com.sinergise.common.geometry.geom.LinearRing;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.util.geom.Envelope;
/**
 * one instance one thread
 * @author pkolaric
 *
 */
public class CRSTransformable {
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected Transform<?, ?> transform = new Identity(CRS.MAP_PIXEL_CRS);
	
	
	private Point pTransSrc = new Point();


	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void setTransform(Transform<? extends CRS, ? extends CRS> crsTransform) {
		if (crsTransform.getTarget() instanceof LatLonCRS) {			
			SwapLatLon swap = new SwapLatLon((LatLonCRS) crsTransform.getTarget());
			this.transform = Transforms.compose(crsTransform, swap);			
		} else if (crsTransform.getSource() instanceof LatLonCRS){ 
			SwapLatLon swap = new SwapLatLon((LatLonCRS) crsTransform.getSource());
			this.transform = Transforms.compose(swap,crsTransform);			
		} else {
			this.transform = crsTransform;
		}
	}

	public Envelope transform (Envelope e) {
		Point p1 = transform(e.getMinX(), e.getMinY());
		Point p2 = transform(e.getMaxX(), e.getMaxY());
		return new Envelope(p1.x,p1.y, p2.x,p2.y);
	}

	public Point transform(double x, double y) {
		pTransSrc.setLocation(x, y);
		Point pTransDst = new Point();
		return transform.point(pTransSrc, pTransDst);
	}
	
	public Point transform(Point p) {
		Point pTransDst = new Point();
		return transform.point(p, pTransDst);
	}
	
	public double[] transform(double[]coordsIn) {
		double[] coords = new double[coordsIn.length];
		Point pTransDst = new Point();
		for (int i=0;i<(coordsIn.length/2);i++) {
			int xx=i<<1;
			int yy=(i<<1)+1;
			pTransSrc.setLocation(coordsIn[xx], coordsIn[yy]);
			pTransDst=transform.point(pTransSrc, pTransDst);
			coords[xx]=pTransDst.x;
			coords[yy]=pTransDst.y;
		}
		return coords;
	}

	public LinearRing transform(LinearRing ls) {		
		return new LinearRing(transform(ls.coords));
	}
	
	public static void main(String[] args) {
		CRSTransformable tf = new CRSTransformable();
		Point p1 = tf.transform(1, 2);
		System.out.println(p1.toString());
		Point p2 = tf.transform(10, 20);
		System.out.println(p2.toString());
		System.out.println(p1.toString());
	}
}
