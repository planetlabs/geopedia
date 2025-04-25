package com.sinergise.java.geometry.algorithm;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import com.sinergise.common.geometry.util.GeomUtil;
import com.sinergise.common.util.lang.Pair;
import com.sinergise.java.util.ui.RunnerHelper;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.index.quadtree.Quadtree;


public class PointClusterBuilder {
	public class CoordXComparator implements Comparator<Coordinate> {
		@Override
		public int compare(Coordinate o1, Coordinate o2) {
			return Double.compare(o1.x, o2.x);
		}
	}
	
	public class CoordYComparator implements Comparator<Coordinate> {
		@Override
		public int compare(Coordinate o1, Coordinate o2) {
			return Double.compare(o1.y, o2.y);
		}
	}
	
	public class Cluster {
		ArrayList<Coordinate> pointsWithin = new ArrayList<Coordinate>();
		Envelope rectEnv = new Envelope();
		Coordinate repPoint = new Coordinate();
		double radius;
		
		public Cluster(Coordinate p1) {
			super();
			addPoint(p1);
		}
		
		public Cluster(Coordinate p1, Coordinate p2) {
			this(p1);
			addPoint(p2);
		}

		public void addCluster(Cluster c) {
			if (c.pointsWithin.size() > pointsWithin.size()) {
				this.repPoint = c.repPoint;
			}
			for (Coordinate p : c.pointsWithin) {
				addPoint(p);
			}
		}
		
		public void addPoint(Coordinate p) {
			pointsWithin.add(p);
			rectEnv.expandToInclude(p);
			
			//!! Use median position
			
//			Pair<Coordinate, Coordinate> cX = CollectionUtil.medianPair(pointsWithin, xComp);
//			Pair<Coordinate, Coordinate> cY = CollectionUtil.medianPair(pointsWithin, yComp);
//			double repX = 0.5 * (cX.first.x + cX.second.x);
//			double repY = 0.5 * (cY.first.y + cY.second.y);
			
			//!! Use center of mbr
			
			double repX = 0.5*(rectEnv.getMinX() + rectEnv.getMaxX());
			double repY = 0.5*(rectEnv.getMinY() + rectEnv.getMaxY());

			//!! Use mean position
			
//			double repX = 0;
//			double repY = 0;
//			for (Coordinate c : pointsWithin) {
//				repX += c.x;
//				repY += c.y;
//			}
//			repX /= pointsWithin.size();
//			repY /= pointsWithin.size();
			
			//!! Keep position fixed where the first point was
//			double repX = repPoint.x == 0 ? p.x : repPoint.x;
//			double repY = repPoint.y == 0 ? p.y : repPoint.y;
			
			
//			if (repPoint.x == 0 && repPoint.y == 0) {
				repPoint.x = repX;
				repPoint.y = repY;
//			}
			radius = curRad(); // 0.5*GeomUtil.distance(env.getMinX(), env.getMinY(), env.getMaxX(), env.getMaxY());
		}
		
		private double curRad() {
			int size = pointsWithin.size();
//			if (size < 2) return 0.5*symbolRadius;
//			else if (size < 4) return 0.75*symbolRadius;
//			else return symbolRadius;
			
			return Math.pow(Math.min(200,Math.max(1,size)), 0.5) * 0.25 * symbolRadius;
//			return Math.pow(Math.max(1,pointsWithin.size()/2), 0.5) * PointClusterBuilder.this.distance/2;
//			return Math.max(0.2* distance, 0.5*GeomUtil.distance(rectEnv.getMinX(), rectEnv.getMinY(), rectEnv.getMaxX(), rectEnv.getMaxY()));
		}

		@Override
		public int hashCode() {
			return System.identityHashCode(this);
		}
		
		@Override
		public boolean equals(Object o) {
			return o == this;
		}

		public double distanceSq(Cluster c2) {
			return GeomUtil.distanceSq(repPoint.x, repPoint.y, c2.repPoint.x, c2.repPoint.y);
		}

		public double getX() {
			return repPoint.x;
		}

		public double getY() {
			return repPoint.y;
		}
	}
	
	CoordXComparator xComp = new CoordXComparator();
	CoordYComparator yComp = new CoordYComparator();
	
	
	Quadtree tree;
	HashSet<Cluster> allPoints;
	Collection<Coordinate> originalPoints;
	double symbolRadius;
	Pair<Cluster, Cluster> closest;
	
	public PointClusterBuilder(double symbolRadius) {
		this.symbolRadius = symbolRadius;
	}
	
	
	public double clusterDistanceSq(final Cluster c1, final Cluster c2) {
		final double a = c1.radius + c2.radius;
		return c1.distanceSq(c2)/(a*a);
	}

	public void init(Collection<Coordinate> points) {
		originalPoints = points;
		tree = new Quadtree();
		allPoints = new HashSet<Cluster>();
		for (Coordinate p : points) {
			Cluster c = new Cluster(p);
			internalAdd(c);
		}
	}


	protected void internalAdd(Cluster c) {
		tree.insert(c.rectEnv, c);
		synchronized(allPoints) {
			allPoints.add(c);
		}
	}
	
	int nIter = 0;
	int nQry = 0;
	int nComp = 0;
	public void build() {
		while (true) {
			nIter++;
			closest = closestPair();
			if (closest == null) break;
			
			final Cluster c1 = closest.getFirst();
			final Cluster c2 = closest.getSecond();
			
			internalRemove(c1);
			internalRemove(c2);
			
			c1.addCluster(c2);
			
			internalAdd(c1);
		}
		System.out.println("Iterations:"+nIter+" Tree queries:"+nQry+" Comparisons:"+nComp);
	}


	protected void internalRemove(Cluster c1) {
		tree.remove(c1.rectEnv, c1);
		synchronized(allPoints) {
			allPoints.remove(c1);
		}
	}
	
	
	
	private Pair<Cluster, Cluster> closestPair() {
		Pair<Cluster, Cluster> curRet = null;
		Envelope env = new Envelope();
		double curDistSq = 1;
		synchronized(allPoints) {
			HashSet<Cluster> visited = new HashSet<Cluster>();
			for (Cluster c : allPoints) {
				// Search around the current cluster
				env.init(c.repPoint);
				// Expand envelope if too small
				env.expandBy(c.radius);
				@SuppressWarnings("unchecked")
				List<Cluster> ret = tree.query(env);
				nQry++;

				for (Cluster c2 : ret) {
					if (c == c2 || visited.contains(c2)) continue;
					double dstSq = clusterDistanceSq(c, c2);
					nComp++;
					if (dstSq < curDistSq) {
						curDistSq = dstSq;
						curRet = Pair.newPair(c, c2);
					}
				}
				visited.add(c);
			}
		}
		return curRet==null || curRet.getFirst() == null ? null : curRet;
	}


	public static void main(String[] args) {
		Random rnd = new Random(1423);
		ArrayList<Coordinate> pt = new ArrayList<Coordinate>();
		for (int i = 0; i < 500; i++) {
			pt.add(new Coordinate(rnd.nextGaussian()*0.15+0.5, rnd.nextGaussian()*0.15+0.5));
		}
		
		
		final PointClusterBuilder pcb = new PointClusterBuilder(0.05);
		pcb.init(pt);

		initDrawer(pcb);
		
		pcb.build();
	}


	protected static void initDrawer(final PointClusterBuilder pcb) {
		RunnerHelper.runDrawer(new RunnerHelper.GraphicsDrawer() {
			@Override
			public void draw(Graphics2D g, int w, int h) {
				final double sc = Math.min(w, h);
				
				g.setColor(Color.GREEN);
				synchronized(pcb.originalPoints) {
					for (Coordinate p : pcb.originalPoints) {
						g.setColor(new Color(0x20FFFF00, true));
						g.fill(new Ellipse2D.Double(sc * (p.x - pcb.symbolRadius), sc * (p.y - pcb.symbolRadius), 2*sc * pcb.symbolRadius, 2*sc * pcb.symbolRadius));
					}
					for (Coordinate p : pcb.originalPoints) {
						g.setColor(Color.GREEN);
						g.draw(new Ellipse2D.Double(sc * p.x - 5, sc * p.y - 5, 10, 10));
					}
				}
				g.setColor(Color.BLACK);
				synchronized(pcb.allPoints) {
					for (Cluster p : pcb.allPoints) {
						if (pcb.closest != null && pcb.closest.contains(p)) {
							g.setColor(Color.RED);
							g.setStroke(new BasicStroke(3));
						} else {
							g.setColor(Color.BLACK);
							g.setStroke(new BasicStroke(1));
						}
						
						double rr = Math.max(p.radius, 5.0/sc);
						g.draw(new Ellipse2D.Double(sc * (p.getX() - rr), sc * (p.getY() - rr), 2 * sc * rr, 2 * sc * rr));
						g.setColor(new Color(0x8000FFFF, true));
						for (Coordinate c : p.pointsWithin) {
							g.draw(new Line2D.Double(sc *p.repPoint.x, sc *p.repPoint.y, sc *c.x, sc *c.y));
						}
					}
				}
			}
		}, 500, 500);
	}
}
