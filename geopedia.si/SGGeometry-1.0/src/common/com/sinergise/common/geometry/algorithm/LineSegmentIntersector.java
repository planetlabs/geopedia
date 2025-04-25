package com.sinergise.common.geometry.algorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import com.sinergise.common.geometry.util.GeomUtil;
import com.sinergise.common.util.collections.SearchItemReceiver;
import com.sinergise.common.util.collections.tree.BinarySearchTree;
import com.sinergise.common.util.collections.tree.BinarySearchTree.InsertionComparator;
import com.sinergise.common.util.geom.CoordinatePair;
import com.sinergise.common.util.geom.HasCoordinate;
import com.sinergise.common.util.geom.Position2D;


public class LineSegmentIntersector {
	
	public static final class SegmentWrapper implements CoordinatePair {
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
				+ ((original == null) ? 0 : original.hashCode());
			result = prime * result + (reverse ? 1231 : 1237);
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			SegmentWrapper other = (SegmentWrapper) obj;
			if (original == null) {
				if (other.original != null) return false;
			} else if (!original.equals(other.original)) return false;
			if (reverse != other.reverse) return false;
			return true;
		}

		public final CoordinatePair original;
		boolean reverse = false;
		public SegmentWrapper(CoordinatePair pair) {
			this.original = pair;
		}
		public void reverse() {
			reverse = !reverse;
		}

		@Override
		public HasCoordinate c1() {
			return reverse ? original.c2() : original.c1();
		}

		@Override
		public HasCoordinate c2() {
			return reverse ? original.c1() : original.c2();
		}

		@Override
		public double x1() {
			return reverse ? original.x2() : original.x1();
		}

		@Override
		public double y1() {
			return reverse ? original.y2() : original.y1();
		}

		@Override
		public double x2() {
			return reverse ? original.x1() : original.x2();
		}

		@Override
		public double y2() {
			return reverse ? original.y1() : original.y2();
		}

		@Override
		public String toString() {
			return "SegmentWrapper " + x1() + "  " + y1() + "  "+ x2() + "  "+ y2();
		}
	}

	public static final class SegmentsOverlap {
		public HasCoordinate overlapTop;
		public HasCoordinate overlapBottom;
		public SegmentWrapper seg1;
		public SegmentWrapper seg2;

		public SegmentsOverlap(SegmentWrapper seg1, SegmentWrapper seg2) {
			super();
			this.overlapTop = isP1Upper(seg1.c1(), seg2.c2()) ? seg2.c1() : seg1.c1();
			this.overlapBottom = isP1Upper(seg1.c2(), seg2.c2()) ? seg1.c2() : seg2.c2();
			this.seg1 = seg1;
			this.seg2 = seg2;
		}

		public boolean containsSweepLine(HasCoordinate point) {
			if (point.y() < overlapBottom.y() || overlapTop.y() < point.y()) return false;
			if (overlapBottom.y() == overlapTop.y() && point.y() == overlapBottom.y()) {
				return overlapTop.x() <= point.x() && point.x() <= overlapBottom.x();
			}
			return true;
		}

		@Override
		public String toString() {
			return "SegmentsOverlap [overlapTop=" + overlapTop
				+ ", overlapBottom=" + overlapBottom + ", seg1=" + seg1
				+ ", seg2=" + seg2 + "]";
		}
	} 

	public  static class EventPoint implements Comparable<EventPoint>, HasCoordinate {
		final HasCoordinate	point;
		public List<SegmentInEP>	segInEP	= new ArrayList<SegmentInEP>();
		boolean hasOverlap = false;

		EventPoint(HasCoordinate p) {
			this.point = p;
		}

		EventPoint(HasCoordinate p, SegmentWrapper seg) {
			this(p);
			addNew(seg);
		}

		public EventPoint(HasCoordinate iP, List<? extends SegmentWrapper> segments) {
			this(iP);
			for (SegmentWrapper cp : segments) {
				addNew(cp);
			}
		}

		public void addNew(SegmentWrapper seg) {
			if (contains(seg)) return;
			segInEP.add(new SegmentInEP(seg, this));
		}

		public boolean contains(CoordinatePair seg) {
			if (seg == null) return false;
			for (SegmentInEP s : segInEP) if (s.seg.equals(seg)) return true;
			return false;
		}

		public boolean isUpper(CoordinatePair seg)
		{
			return seg!=null && point.x() == seg.x1() && point.y() == seg.y1();
		}


		public boolean isLower(CoordinatePair seg)
		{
			return seg!=null && point.x() == seg.x2() && point.y() == seg.y2();
		}
		
		public boolean hasOverlappingSegments()
		{
			return hasOverlap;
		}

		@Override
		public double x() {
			return point.x();
		}

		@Override
		public double y() {
			return point.y();
		}

		public List<SegmentInEP> getSegments(){
			return segInEP;
		}

		@Override
		public int compareTo(EventPoint o) {
			double dy = o.y() - y();
			if (dy == 0) return (int)(Math.signum(o.x() - x()));
			return (int)(Math.signum(dy));
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((point == null) ? 0 : point.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			EventPoint other = (EventPoint)obj;
			if (point == null) {
				if (other.point != null) return false;
			} else if (!point.equals(other.point)) return false;
			return true;
		}

		@Override
		public String toString() {
			return point.toString() + "	, overlapping " + hasOverlap + ", " + segInEP.size() + " segs: "+ segInEP.toString();
		}

		public List<String> segmentsToString() {

			List<String> segOfInt= new ArrayList<String>();

			for (SegmentInEP cp : segInEP) {
				segOfInt.add("{("+cp.seg.c1().x() + ", " + cp.seg.c1().y()+ "), ("+ cp.seg.c2().x() + ", " + cp.seg.c2().y()+ ")}, ");
			}

			return  segOfInt;
		}


		public int countUI() {
			int cnt=0;
			for (SegmentInEP se : segInEP) {
				if ((se.mask & SegmentInEP.MASK_UI) != 0) cnt++;
			}
			return cnt;
		}

		public int countI() {
			int cnt=0;
			for (SegmentInEP se : segInEP) {
				if ((se.mask & SegmentInEP.MASK_I) != 0) cnt++;
			}
			return cnt;
		}

		public SegmentWrapper filterNotContained(SegmentWrapper seg) {
			return contains(seg) ? null : seg;
		}

	}

	public static class SegmentInEP {
		SegmentWrapper seg;

		public static final int MASK_U = 1;
		public static final int MASK_L = 2;
		public static final int MASK_I = 4;
		public static final int MASK_UI = MASK_U | MASK_I;
		public static final int MASK_LI = MASK_L | MASK_I;

		public final int mask;

		public SegmentInEP(SegmentWrapper seg, EventPoint ep) {
			this.seg = seg;

			int tmpMask = 0;
			if (ep.isUpper(seg)) tmpMask = tmpMask | MASK_U;
			if (ep.isLower(seg)) tmpMask = tmpMask | MASK_L;
			if (tmpMask == 0 || tmpMask == (MASK_U | MASK_L)) {
				tmpMask = tmpMask | MASK_I;
			}

			mask = tmpMask;
		}

		public boolean isInterior() {
			return (mask & MASK_I) != 0;
		}

		public boolean isUpper() {
			return (mask & MASK_U) != 0;
		}
		public boolean isLower() {
			return (mask & MASK_L) != 0;
		}

		public boolean isUorI() {
			return (mask & MASK_UI) != 0;
		}

		public boolean isLorI() {
			return (mask & MASK_LI) != 0;
		}

		@Override
		public String toString() {
			//	return mask + " " + seg.toString();
			//	return mask + " " + seg;
			return mask + " " +  "(" + seg.x1() + "," + seg.y1() + " " + seg.x2()+ "," + seg.y2() + ")";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + mask;
			result = prime * result + ((seg == null) ? 0 : seg.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			SegmentInEP other = (SegmentInEP) obj;
			if (mask != other.mask) return false;
			if (seg == null) {
				if (other.seg != null) return false;
			} else if (!seg.equals(other.seg)) return false;
			return true;
		}

		//TODO: ORDER THE SEGMENTS according to angle
	}

	public class SegmentInsertionComparator implements InsertionComparator<CoordinatePair> {
		@Override
		public int compareForInsert(CoordinatePair segC, CoordinatePair segI) {
			final double maxY = Math.max(segI.y1(), segI.y2());
			if (maxY == currentSweepY) {
				// inserting a segment below or on the sweep line

				int ret = -GeomUtil.orientationIndex(segC.x1(), segC.y1(), segC.x2(), segC.y2(), segI.x1(), segI.y1());
				if (ret != 0) return ret;

				//c1 is on line, try c2
				ret = -GeomUtil.orientationIndex(segC.x1(), segC.y1(), segC.x2(), segC.y2(), segI.x2(), segI.y2());
				if (ret != 0) return ret;

				final double dY = segI.y1() - segC.y1();
				if (dY > 0) return 1;
				if (dY < 0) return -1;

				final double dX = segI.x1() - segC.y1(); 
				if (dX > 0) return 1;
				if (dX > 0) return -1;

				return 0;
			}
			throw new UnsupportedOperationException();
		}
	}

	public static class SortSegmentsInEP implements Comparator<SegmentInEP>
	{
		@Override
		public int compare(SegmentInEP seg1, SegmentInEP seg2){
			if (seg1.seg.y1() < seg2.seg.y1()) return 1;
			if (seg1.seg.y1() > seg2.seg.y1()) return -1;
			if (seg1.seg.y1() == seg2.seg.y1() && seg1.seg.x1() < seg2.seg.x1()) return -1;
			if (seg1.seg.y1() == seg2.seg.y1() && seg1.seg.x1() > seg2.seg.x1()) return 1;
			if (seg1.seg.y1() == seg2.seg.y1() && seg1.seg.x1() == seg2.seg.x1() &&
				seg1.seg.y2() > seg2.seg.y2()) return 1;
			else if (seg1.seg.y1() == seg2.seg.y1() && seg1.seg.x1() == seg2.seg.x1() &&
				seg1.seg.y2() < seg2.seg.y2()) return -1;
			else return 0;
		}
	}

	/**
	 * @param p1
	 * @param p2
	 * @return true if p1 is above p2; if both have same y, returns true if p1 is left of p2; if points are equal, return true; 
	 */
	public static final boolean isP1Upper(HasCoordinate p1, HasCoordinate p2) {
		final double y1 = p1.y();
		final double y2 = p2.y();
		if (y1 > y2) return true;
		if (y1 == y2) return p1.x() <= p2.x();
		return false;
	}

	private PriorityQueue<EventPoint> pQ = new PriorityQueue<EventPoint>();
	private Map<HasCoordinate, EventPoint> pointsMap = new HashMap<HasCoordinate, LineSegmentIntersector.EventPoint>();
	private BinarySearchTree<SegmentWrapper> status = new BinarySearchTree<SegmentWrapper>(new SegmentInsertionComparator());
	double currentSweepY = Double.POSITIVE_INFINITY;
	private final HashMap<CoordinatePair, List<SegmentsOverlap>> overlaps = new HashMap<CoordinatePair, List<SegmentsOverlap>>();

	private Collection<? extends CoordinatePair> segments = null;
	private SearchItemReceiver<EventPoint> output = null;

	public LineSegmentIntersector(Collection<? extends CoordinatePair> segments, SearchItemReceiver<EventPoint> output) {
		this.segments = segments;
		initializeQueue();
		
		this.output  = output;
	}
	
	/**
	 * @return true iff the whole geometry was searched (false when output signaled termination)
	 */

	public boolean findIntersections() {
		try {
			
			while (!pQ.isEmpty()) {

				EventPoint ep = getNextEP();
			
				if(!handleEventPoint(ep)){
					return false;
				}
				
			}
			return true;		
			
		} finally {
			pQ.clear();
			pointsMap.clear();
			status.clear();
			currentSweepY = Double.POSITIVE_INFINITY;
		}
	}

	private EventPoint getNextEP() {
		EventPoint ep = pQ.poll();

		//pQ does not order horizontal segments correctly
		if(pQ.size() >= 1 && ep.y() == pQ.peek().y()){
			ep = reorderOnSweepline(ep);
		}
		
		return ep;
	}

	private EventPoint reorderOnSweepline(EventPoint ep) {
		EventPoint oldEP = ep;
		List<EventPoint> listEP = new ArrayList<LineSegmentIntersector.EventPoint>();	

		while(pQ.size() >= 1 && ep.y() == pQ.peek().y()){
			EventPoint ep1 = pQ.poll();
			listEP.add(ep1);
			ep = ep1;
		}

		pQ.add(oldEP);
		pointsMap.put(oldEP.point, oldEP);

		for (int i = 0; i < listEP.size()-1; i++) {
			EventPoint ept = listEP.get(i);
			pQ.add(ept);
			pointsMap.put(ept.point, ept);
		}
		ep = listEP.get(listEP.size()-1);
		return ep;
	}
	
	private boolean handleEventPoint(final EventPoint ep)
	{
		//System.out.println("===============Event Point=====================");
		//System.out.println(ep.toString());

		currentSweepY = ep.y();
		SegmentWrapper suc = null;
		SegmentWrapper pre = null;

		addOverlappingSegments(ep);

		for (SegmentInEP s : ep.segInEP) {
			if (s.isLower()) {
				if (status.contains(s.seg)) {
					suc = status.successor(s.seg);
					pre = status.predecessor(s.seg);
					status.remove(s.seg);
				} else {
					System.out.println("status does not contain seg " + s);
				}
				overlaps.remove(s.seg);
			}
		}
		if (ep.countUI() == 0 || ep.contains(suc) || ep.contains(pre)) {
			testIntersection(suc, pre, ep);
		}

		swap(ep);

		for (SegmentInEP s : ep.segInEP) {
			if(s.isUpper()) status.insert(s.seg);
		}

		if (status.size() != 0) {
			for (SegmentInEP s : ep.segInEP) {
				if (s.isUorI()) {
					pre = ep.filterNotContained(status.predecessor(s.seg));
					if (pre != null) {
						break;
					}
				}
			}
			for (SegmentInEP s : ep.segInEP) {
				if (s.isUorI()) {
					suc = ep.filterNotContained(status.successor(s.seg));
					if (suc != null) {
						break;
					}
				}
			}

			for (int i = ep.segInEP.size()-1; i >= 0; i--) {
				SegmentInEP s = ep.segInEP.get(i); 
				if (s.isUorI()){
					testIntersection(pre, s.seg, ep);
					testIntersection(s.seg, suc, ep);
				} 
			}
		}

		addOverlappingSegments(ep);
		
		if (ep.segInEP.size() > 1){
			return output.execute(ep).booleanValue();
		}
		
		return true;

	}

	private void addOverlappingSegments(EventPoint ep) {
		for (int i = ep.segInEP.size()-1; i >= 0; i--) {
			SegmentInEP s = ep.segInEP.get(i);
			List<SegmentsOverlap> allOvr = overlaps.get(s.seg);
			if (allOvr == null) {
				for (SegmentInEP s2 : ep.segInEP) {
					if (s==s2) {
						continue;
					}
					if (GeomUtil.isOverlapOrWithin(GeomUtil.lineLineIntersect(s.seg, s2.seg))) {
						ep.hasOverlap = true;
						addOverlap(s.seg, s2.seg);
					}
				}
				continue;
			}
			for (SegmentsOverlap ovr : allOvr) {
				if (ovr.containsSweepLine(ep.point)) {
					ep.addNew(ovr.seg1);
					ep.addNew(ovr.seg2);
					ep.hasOverlap = true;
				}				
			}
		}
	}

	private void swap(EventPoint ep) {
		int numSeg = ep.segInEP.size();
		if(numSeg < 2) return;

		SegmentWrapper cur = null;

		// find one in the status
		for (SegmentInEP s : ep.segInEP) {
			if (status.contains(s.seg)) {
				cur = s.seg;
				break;
			}
		}
		if (cur == null) return;

		// find leftmost
		SegmentWrapper first = null;
		do {
			first = cur;
			cur = status.predecessor(first);
		} while (ep.contains(cur));


		// find rightmost
		SegmentWrapper last = null;
		cur = first;
		do {
			last = cur;
			cur = status.successor(cur);
		} while (ep.contains(cur));

		if (first == last) return;

		//do the swapping
		do {
			status.swap(first, last);
			SegmentWrapper temp = first;
			first = status.successor(last);
			if (first == temp) break;
			last = status.predecessor(temp);
			if (last == first) break;
		} while (true);
	}
	
	private void initializeQueue() {
		for (CoordinatePair seg : segments) {
			SegmentWrapper newSeg = new SegmentWrapper(seg);
			if (!isP1Upper(seg.c1(), seg.c2())) newSeg.reverse();

			addEvenPointFor(newSeg.c1(), newSeg);
			addEvenPointFor(newSeg.c2(), newSeg);
		}
	}

	private void addEvenPointFor(HasCoordinate c, SegmentWrapper newSeg) {

		EventPoint ept = pointsMap.get(c);
		if (ept == null) {
			ept = new EventPoint(c, newSeg);
			pQ.add(ept);
			pointsMap.put(c, ept);
		} else {
			ept.addNew(newSeg);
		}
	}

	public EventPoint testIntersection(SegmentWrapper seg1, SegmentWrapper seg2, EventPoint ep) {
		if (seg1 == null || seg2 == null) return null;

		final double sweepY = ep.y();
		final double px1 = seg1.x1();
		final double py1 = seg1.y1();
		final double px2 = seg1.x2();
		final double py2 = seg1.y2();

		final double qx1 = seg2.x1();
		final double qy1 = seg2.y1();
		final double qx2 = seg2.x2();
		final double qy2 = seg2.y2();

		int intersection = GeomUtil.lineLineIntersect(px1, py1, px2, py2, qx1, qy1, qx2, qy2);
		
		if (intersection == 0) return null;

		HasCoordinate iP = getIntersection(intersection, px1, py1, px2, py2, qx1, qy1, qx2, qy2);

		if (iP.y() > sweepY || (iP.y() == sweepY && iP.x() < ep.x())) return null;

		if (Double.isNaN(iP.x()) || Double.isNaN(iP.y())) {
			throw new RuntimeException("Detected intersection but could not find the point - s1: " + seg1 + " s2:" + seg2);
		}

		if ((intersection & GeomUtil.MASK_LINE_ALL) > 0) {
			// create new EP
			EventPoint newEP = pointsMap.get(iP);
			if (newEP == null) {
				newEP = new EventPoint(iP);
				pQ.add(newEP);
				pointsMap.put(iP, newEP);
			}

			newEP.addNew(seg1);
			newEP.addNew(seg2);

			if (GeomUtil.isOverlapOrWithin(intersection)) {
				
				// OVERLAP !!!
				
				/*
				 * 
				if s1.lower.y < s2.lower.y
					iP = s2.lower
				else if s1.lower.y > s2.lower.y
					iP = s1.lower
				else if s1.lower.y ==  s2.lower.y && s1.lower.x >  s2.lower.x
					iP = s2.lower
				else 
					iP = s1.lower
				 */
				
				//TODO what about P1 and Q1?
				
				if ((intersection & GeomUtil.MASK_LINE_P2) != 0) {
					EventPoint anotherEP = pointsMap.get(seg1.c2());
					anotherEP.addNew(seg2);

					addOverlap(seg1, seg2);
				} else if ((intersection & GeomUtil.MASK_LINE_Q2) != 0) {
					EventPoint anotherEP = pointsMap.get(seg2.c2());
					anotherEP.addNew(seg1);

					addOverlap(seg1, seg2);
				} else if ((intersection & GeomUtil.MASK_LINE_P1) != 0) {
					EventPoint anotherEP = pointsMap.get(seg1.c1());
					anotherEP.addNew(seg2);
					
					addOverlap(seg1, seg2);
				} else if ((intersection & GeomUtil.MASK_LINE_Q1) != 0) {
					EventPoint anotherEP = pointsMap.get(seg2.c1());
					anotherEP.addNew(seg1);
					
					addOverlap(seg1, seg2);
				}

				/*
				 * old version
				 * // OVERLAP !!!
				if ((intersection & GeomUtil.MASK_LINE_P2) != 0) {
					EventPoint anotherEP = pointsMap.get(seg1.c2());
					anotherEP.addNew(seg2);

					addOverlap(iP, seg1.c2(), seg1, seg2);
				}
				if ((intersection & GeomUtil.MASK_LINE_Q2) != 0) {
					EventPoint anotherEP = pointsMap.get(seg2.c2());
					anotherEP.addNew(seg1);

					addOverlap(iP, seg2.c2(), seg1, seg2);
				}
				 */
				
				//				roundP = new Point(roundDecimals(iP.x()), roundDecimals(iP.y()));
//						
//				EventPoint anotherEP = nonDuplicateIntersections.get(roundP);
//						if(anotherEP == null){
//							anotherEP = new EventPoint(iP, new ArrayList<SegmentWrapper>());
//							nonDuplicateIntersections.put(roundP, anotherEP);
//						}
//						if (!anotherEP.contains(newSeg2)) anotherEP.addNew(newSeg2);
//						if (!anotherEP.contains(newSeg1)) anotherEP.addNew(newSeg1);
//						Collections.sort(anotherEP.segInEP, new SortSegmentsInEP());
			
			}
			addOverlappingSegments(newEP);
			return  newEP;
		}
		return null;
	}

	/**
	 * 
	 * @param seg1 first (overlapping) segment
	 * @param seg2 second (overlapping) segment
	 */
	private void addOverlap(SegmentWrapper seg1, SegmentWrapper seg2) {
		SegmentsOverlap ovr = new SegmentsOverlap(seg1, seg2);

		List<SegmentsOverlap> allOvrs = overlaps.get(seg1);
		if (allOvrs == null) overlaps.put(seg1, allOvrs = new ArrayList<SegmentsOverlap>());
		allOvrs.add(ovr);

		List<SegmentsOverlap> allOvrs2 = overlaps.get(seg2);
		if (allOvrs2 == null) overlaps.put(seg2, allOvrs2 = new ArrayList<SegmentsOverlap>());
		allOvrs2.add(ovr);
	}

	/**
	 * 
	 * @param mask
	 * @param px1
	 * @param py1
	 * @param px2
	 * @param py2
	 * @param qx1
	 * @param qy1
	 * @param qx2
	 * @param qy2
	 * @return intersection point with the largest y coordinate (or smallest x if ys are the same)
	 */
	public static HasCoordinate getIntersection(final int mask, final double px1, final double py1, final double px2, final double py2, final double qx1, final double qy1,
		final double qx2, final double qy2) {
		if ((mask & GeomUtil.MASK_LINE_ENDPOINTS) != 0) {
			Position2D ret = new Position2D(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY);
			if ((mask & GeomUtil.MASK_LINE_P1) != 0 && py1 >= ret.y && (py1 > ret.y || px1 < ret.x)) ret.setLocation(px1, py1);
			if ((mask & GeomUtil.MASK_LINE_P2) != 0 && py2 >= ret.y && (py2 > ret.y || px2 < ret.x)) ret.setLocation(px2, py2);
			if ((mask & GeomUtil.MASK_LINE_Q1) != 0 && qy1 >= ret.y && (qy1 > ret.y || qx1 < ret.x)) ret.setLocation(qx1, qy1);
			if ((mask & GeomUtil.MASK_LINE_Q2) != 0 && qy2 >= ret.y && (qy2 > ret.y || qx2 < ret.x)) ret.setLocation(qx2, qy2);
			return ret;
		}
		if (mask != GeomUtil.LINE_Pint_Qint) return null;


		if (px1 < qx1 || (px1 == qx1 && py1 > qy1)) {
			// x -> x - x_avg
			// y -> y - y_avg
			final double offX = 0.25 * (px1 + px2 + qx1 + qx2);
			final double offY = 0.25 * (py1 + py2 + qy1 + qy2);
			return getIntersectionWithOffset(px1, py1, px2, py2, qx1, qy1, qx2, qy2, offX, offY);
		}
		final double offX = 0.25 * (qx1 + qx2 + px1 + px2);
		final double offY = 0.25 * (qy1 + qy2 + py1 + py2);
		return getIntersectionWithOffset(qx1, qy1, qx2, qy2, px1, py1, px2, py2, offX, offY);
	}		
	public static HasCoordinate getIntersectionWithOffset(final double px1, final double py1, final double px2, final double py2, final double qx1, final double qy1,
		final double qx2, final double qy2, final double offX, final double offY) {
		final double dpx = px2 - px1;
		final double dpy = py2 - py1;

		final double dqx = qx2 - qx1;
		final double dqy = qy2 - qy1;

		final double det = dpx * dqy - dqx * dpy;

		final double detp = (px1 - offX) * (py2 - offY) - (px2 - offX) * (py1 - offY);
		final double detq = (qx1 - offX) * (qy2 - offY) - (qx2 - offX) * (qy1 - offY);

		final Position2D point = new Position2D();

		//TODO: implement adaptive arithmetic
		//point.x = roundDecimals(offX + (dpx * detq - dqx * detp) / det); 
		//point.y = roundDecimals(offY + (dpy * detq - dqy * detp) / det);

		point.x = offX + (dpx * detq - dqx * detp) / det; 
		point.y = offY + (dpy * detq - dqy * detp) / det;

		return point;
	}

}
