package com.sinergise.java.geometry.j2d;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

import com.sinergise.common.util.math.MathUtil;
import com.sinergise.java.util.ui.RunnerHelper;
import com.sinergise.java.util.ui.RunnerHelper.GraphicsDrawer;

public class ErrorBand {
	public static void main(String[] args) {
		RunnerHelper.runDrawer(new GraphicsDrawer() {
			@Override
			public void draw(Graphics2D g, int w, int h) {
				//Color clr = new Color(0x00FFFFFF & Color.BLUE, true);
				Color clr = new Color(0, 0, 1f, 0.1f);
				//g.setColor(Color.BLUE);
				//g.fill(computeEdgeErrorBand(150, 200, 450, 200, 29, 50));
				g.setColor(clr);
				for (int i = 0; i < 30; i+=5) {
					g.fill(computePointError(150, 200, 80, i));
				}
				
				
			}
		}, 800, 600);
	}
	
	public static Shape computePointError(double centerx, double centery, double radius, double width)
	{
		GeneralPath gp = new GeneralPath();
	    GeneralPath dummy = new GeneralPath(); // used to find arc endpoints
	    double start = 0;
	    double extent = 360;
		double left, top;
		left = centerx - radius;
		top = centery - radius;
					 
		Shape outer = new Arc2D.Double(left, top, 2 * radius, 2 * radius, start, extent, Arc2D.OPEN);
		Shape inner = new Arc2D.Double(left + width, top + width, 2 * radius - 2 * width
					                        , 2 * radius - 2 * width, start+extent, -extent, Arc2D.OPEN);
		gp.append(outer, false);
		dummy.append(new Arc2D.Double(left + width, top + width, 2 * radius - 2 * width
					                , 2 * radius - 2 * width, start, extent, Arc2D.OPEN),false);
					 
		Point2D point = dummy.getCurrentPoint();
					 
		if(point!=null)gp.lineTo((float)point.getX(), (float)point.getY());
		gp.append(inner, false);
		dummy.append(new Arc2D.Double(left, top, 2 * radius, 2 * radius, start+extent, -extent, Arc2D.OPEN),false);
					 
		point = dummy.getCurrentPoint();
		gp.lineTo((float)point.getX(), (float)point.getY());
		return gp;		
	}


	public static Shape computeEdgeErrorBand(double x1, double y1, double x2, double y2, double r1, double r2) {
		final double dx = x2 - x1;
		final double dy = y2 - y1;
		final double len = MathUtil.hypot(dx, dy);
		final double s1 = r1 / len;
		final double s2 = r2 / len;
		final double ang = MathUtil.RAD_IN_DEGREES * MathUtil.atan2(dy, dx);
		
		final double sumSigSq = (s1*s1 + s2*s2);
		final double sqrtSumSigSq = Math.sqrt(sumSigSq);
		final double midP = s1 * s1 / sumSigSq;
		final double midOff = s1 * s2 / sqrtSumSigSq;
		final double offP1 = 0.5*s1;
		final double offP2 = 0.5*s2; 
		final double midOffP1 = 1.0 - s2 / sqrtSumSigSq;
		final double midOffP2 = s1 / sqrtSumSigSq;
		
		Arc2D c1 = new Arc2D.Double(x1 - r1, y1 - r1, 2 * r1, 2 * r1, -ang+90, 180, Arc2D.OPEN);
		Arc2D c2 = new Arc2D.Double(x2 - r2, y2 - r2, 2 * r2, 2 * r2, -ang-90, 180, Arc2D.OPEN);
		GeneralPath gpath = new GeneralPath(Path2D.WIND_NON_ZERO);
		gpath.append(c1, true);
		gpath.curveTo(
				(float)(x1 + dx * offP1 - dy * s1),(float)(y1 + dy * offP1 + dx * s1), 
				(float)(x1 + dx * midOffP1 - dy * midOff),(float)(y1 + dy * midOffP1 + dx * midOff), 
				(float)(x1 + dx * midP - dy*midOff), (float)(y1 + dy * midP + dx*midOff));
		gpath.curveTo(
				(float)(x1 + dx * midOffP2 - dy * midOff),(float)(y1 + dy * midOffP2 + dx * midOff), 
				(float)(x2 - dx * offP2 - dy * s2), (float)(y2 - dy * offP2 + dx * s2),
				(float)(x2 - dy * s2), (float)(y2 + dx * s2));
		gpath.append(c2, true);
		gpath.curveTo(
				(float)(x2 - dx * offP2 + dy * s2), (float)(y2 - dy * offP2 - dx * s2),
				(float)(x1 + dx * midOffP2 + dy * midOff),(float)(y1 + dy * midOffP2 - dx * midOff), 
				(float)(x1 + dx * midP + dy*midOff), (float)(y1 + dy * midP - dx*midOff));
		gpath.curveTo(
				(float)(x1 + dx * midOffP1 + dy * midOff),(float)(y1 + dy * midOffP1 - dx * midOff), 
				(float)(x1 + dx * offP1 + dy * s1),(float)(y1 + dy * offP1 - dx * s1),
				(float)(x1 + dy * s1), (float)(y1 - dx * s1)
		); 
//		gpath.lineTo((float)(x1 + dx * midP + dy*midOff), (float)(y1 + dy * midP - dx*midOff));
//		gpath.lineTo((float)(x1 - dy * s1), (float)(y1 + dx * s1));
//		gpath.lineTo((float)(x2 - dy * s2), (float)(y2 + dx * s2));
//		gpath.lineTo((float)(x2 + dy * s2), (float)(y2 - dx * s2));
		gpath.closePath();
		return gpath;
	}
}
