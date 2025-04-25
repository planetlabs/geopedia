package com.sinergise.common.geometry.algorithm;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sinergise.common.geometry.algorithm.LineSegmentIntersector.EventPoint;
import com.sinergise.common.geometry.algorithm.LineSegmentIntersector.SegmentInEP;
import com.sinergise.common.geometry.algorithm.LineSegmentIntersector.SegmentWrapper;
import com.sinergise.common.geometry.algorithm.LineSegmentIntersector.SortSegmentsInEP;
import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.geometry.util.GeomUtil;
import com.sinergise.common.util.collections.SearchItemReceiver;
import com.sinergise.common.util.geom.CoordinatePair;
import com.sinergise.common.util.geom.HasCoordinate;
import com.sinergise.common.util.lang.Predicate;

public class LineSegmentIntersectorUtil {

	public final static Predicate<EventPoint> CROSSING_FILTER = new Predicate<EventPoint>(){
		@Override
		public boolean eval(EventPoint ep) {
			return ep.countI()>=2;
		}
	};

	public final static Predicate<EventPoint> BORDER_FILTER = new Predicate<EventPoint>(){
		@Override
		public boolean eval(EventPoint ep) {
			for (SegmentInEP seg : ep.segInEP){
				if (seg.isLower() || seg.isUpper()) {
					return true;
				}
			}
			return false;
		}
	};

	public final static Predicate<EventPoint> INTERIOR_FILTER = new Predicate<EventPoint>(){
		@Override
		public boolean eval(EventPoint ep) {
			int nInterior = ep.countI();
			return nInterior>=1 || ep.hasOverlappingSegments();
		}

	};

	public static class SortIntersections implements Comparator<EventPoint>
	{
		@Override
		public int compare(EventPoint i1, EventPoint i2){
			if (i1.y() < i2.y()) return 1;
			if  (i1.y() > i2.y()) return -1;
			if(i1.y() ==  i2.y() && i1.x() <  i2.x()) return -1;
			else if(i1.y() ==  i2.y() && i1.x() >  i2.x()) return 1;
			else return 0;

		}
	}
	
	public static class SortSegments implements Comparator<CoordinatePair>
	{
		@Override
		public int compare(CoordinatePair seg1, CoordinatePair seg2){
			if (seg1.y1() < seg1.y2()) return 1;
			if  (seg1.y1() > seg2.y1()) return -1;
			if (seg1.y1() == seg2.y1() && seg1.x1() < seg2.x1()) return -1;
			else if  (seg1.y1() == seg2.y1() && seg1.x1() > seg2.x1()) return 1;
			else return 0;
		}
	}

	/**
	 * 
	 * Use {@link LineSegmentIntersectorUtil#findIntersectionsBruteForce(List, com.sinergise.common.util.collections.SearchItemReceiver)}
	 */
	@Deprecated
	public static List<EventPoint> findIntersectionsBruteForce(List<CoordinatePair> segments, int intersectionMask){

		Map<HasCoordinate, EventPoint>	nonDuplicateIntersections	= new HashMap<HasCoordinate, EventPoint>();
		final CoordinatePair[] segList = segments.toArray(new CoordinatePair[segments.size()]);
		final int N = segList.length;

		double px1, px2, py1, py2, qx1, qx2, qy1, qy2;

		

		for (int i = 0; i < N; i++) {
			final CoordinatePair seg1 = segList[i];

			SegmentWrapper newSeg1 = new SegmentWrapper(seg1);
			if (!LineSegmentIntersector.isP1Upper(seg1.c1(), seg1.c2())) newSeg1.reverse();

			for (int k = i+1; k < N; k++) {
				final CoordinatePair seg2 = segList[k];

				SegmentWrapper newSeg2 = new SegmentWrapper(seg2);
				if (!LineSegmentIntersector.isP1Upper(seg2.c1(), seg2.c2())) newSeg2.reverse();

				px1 = newSeg1.x1();
				py1 = newSeg1.y1();
				px2 = newSeg1.x2();
				py2 = newSeg1.y2();

				qx1 = newSeg2.x1();
				qy1 = newSeg2.y1();
				qx2 = newSeg2.x2();
				qy2 = newSeg2.y2();


				final int intersection = GeomUtil.lineLineIntersect(px1, py1, px2, py2, qx1, qy1, qx2, qy2);
				if ((intersection & intersectionMask) != 0) {
					HasCoordinate iP = LineSegmentIntersector.getIntersection(intersection, px1, py1, px2, py2, qx1, qy1, qx2, qy2);
					Point roundP = new Point(roundDecimals(iP.x()), roundDecimals(iP.y())); 
					EventPoint temp = nonDuplicateIntersections.get(roundP);
					if (temp == null) {
						temp = new EventPoint(iP, new ArrayList<SegmentWrapper>());
						nonDuplicateIntersections.put(roundP, temp);
					}

					if (!temp.contains(newSeg1)) temp.addNew(newSeg1);
					if (!temp.contains(newSeg2)) temp.addNew(newSeg2);
					Collections.sort(temp.segInEP, new SortSegmentsInEP());

					if (GeomUtil.isOverlapOrWithin(intersection)) {

						if(newSeg1.c2().y() < newSeg2.c2().y() && newSeg1.c2().y() !=  newSeg2.c2().y()){
							iP = newSeg2.c2();
						}

						else if (newSeg1.c2().y() > newSeg2.c2().y() && newSeg1.c2().y() !=  newSeg2.c2().y()){
							iP = newSeg1.c2();
						}

						else if(newSeg1.c2().y() ==  newSeg2.c2().y() && newSeg1.c2().x() >  newSeg2.c2().x()){
							iP = newSeg2.c2();
						}

						else iP = newSeg1.c2();
						roundP = new Point(roundDecimals(iP.x()), roundDecimals(iP.y()));

						EventPoint anotherEP = nonDuplicateIntersections.get(roundP);
						if(anotherEP == null){
							anotherEP = new EventPoint(iP, new ArrayList<SegmentWrapper>());
							nonDuplicateIntersections.put(roundP, anotherEP);
						}
						if (!anotherEP.contains(newSeg2)) anotherEP.addNew(newSeg2);
						if (!anotherEP.contains(newSeg1)) anotherEP.addNew(newSeg1);
						Collections.sort(anotherEP.segInEP, new SortSegmentsInEP());
					}


				}
			}
		}
		
		for (EventPoint ep : nonDuplicateIntersections.values()) {
			for (int i = 0; i < ep.segInEP.size(); i++) {
				for (int j = i+1; j < ep.segInEP.size(); j++) {
					if (GeomUtil.isOverlapOrWithin(GeomUtil.lineLineIntersect(ep.segInEP.get(i).seg, ep.segInEP.get(j).seg))) {
						ep.hasOverlap = true;
					}
				}
			}
		}
		
		ArrayList<EventPoint> out2 = new ArrayList<EventPoint>(nonDuplicateIntersections.values());
		Collections.sort(out2, new SortIntersections());
		return out2;
	}
	
	public static double roundDecimals(double d) {
		return  BigDecimal.valueOf(d).setScale(14, RoundingMode.HALF_UP).doubleValue();
	}
	
	public static boolean findIntersectionsBruteForce(List<CoordinatePair> segments, final SearchItemReceiver<? super EventPoint> output) {
		
		Collections.sort(segments, new SortSegments());
		final CoordinatePair[] segList = segments.toArray(new CoordinatePair[segments.size()]);
		final int N = segList.length;

		final Map<HasCoordinate, EventPoint>	outPoints = new HashMap<HasCoordinate, EventPoint>();
		
		double px1, px2, py1, py2, qx1, qx2, qy1, qy2;
		
		for (int i = 0; i < N-1; i++) {
			final CoordinatePair seg1 = segList[i];
			
			SegmentWrapper newSeg1 = new SegmentWrapper(seg1);
			if (!LineSegmentIntersector.isP1Upper(seg1.c1(), seg1.c2())) {
				newSeg1.reverse();
			}
			
			for (int k = i+1; k < N; k++) {
				final CoordinatePair seg2 = segList[k];
				
				SegmentWrapper newSeg2 = new SegmentWrapper(seg2);
				if (!LineSegmentIntersector.isP1Upper(seg2.c1(), seg2.c2())) newSeg2.reverse();
				
				px1 = newSeg1.x1();
				py1 = newSeg1.y1();
				px2 = newSeg1.x2();
				py2 = newSeg1.y2();
				
				qx1 = newSeg2.x1();
				qy1 = newSeg2.y1();
				qx2 = newSeg2.x2();
				qy2 = newSeg2.y2();
				
				final int intersection = GeomUtil.lineLineIntersect(px1, py1, px2, py2, qx1, qy1, qx2, qy2);
				if (GeomUtil.isOverlapOrWithin(intersection)) {
					
					if (LineSegmentIntersector.isP1Upper(newSeg1.c1(), newSeg2.c1())) {
						addOverlapToBruteForceOutput(outPoints, newSeg1, newSeg2, newSeg2.c1());
					} else {
						addOverlapToBruteForceOutput(outPoints, newSeg1, newSeg2, newSeg1.c1());
					}
					
					if (LineSegmentIntersector.isP1Upper(newSeg1.c2(), newSeg2.c2())) {
						addOverlapToBruteForceOutput(outPoints, newSeg1, newSeg2, newSeg1.c2());
					} else {
						addOverlapToBruteForceOutput(outPoints, newSeg1, newSeg2, newSeg2.c2());
					}
					
				} else if (intersection != 0) {
					HasCoordinate iP = LineSegmentIntersector.getIntersection(intersection, px1, py1, px2, py2, qx1, qy1, qx2, qy2);
					addPointToBruteForceOutput(outPoints, newSeg1, newSeg2, iP);
				}
			}
		}

		for (EventPoint ep : outPoints.values()) {
			for (int i = 0; i < ep.segInEP.size(); i++) {
				for (int j = i+1; j < ep.segInEP.size(); j++) {
					if (GeomUtil.isOverlapOrWithin(GeomUtil.lineLineIntersect(ep.segInEP.get(i).seg, ep.segInEP.get(j).seg))) {
						ep.hasOverlap = true;
					}
				}
			}
		}

		
		ArrayList<EventPoint> out2 = new ArrayList<EventPoint>(outPoints.values());
		Collections.sort(out2, new SortIntersections());
		for (int nEP = 0; nEP<out2.size(); nEP++){
			output.execute(out2.get(nEP));
		}
		
		return true;
	}

	private static void addOverlapToBruteForceOutput(final Map<HasCoordinate, EventPoint> outPoints, SegmentWrapper seg1, SegmentWrapper seg2, HasCoordinate iP) {
		EventPoint ep = addPointToBruteForceOutput(outPoints, seg1, seg2, iP);
		ep.hasOverlap = true;
	}

	
	private static EventPoint addPointToBruteForceOutput(final Map<HasCoordinate, EventPoint> outPoints, SegmentWrapper seg1, SegmentWrapper seg2, HasCoordinate iP) {
		Point roundP = new Point(roundDecimals(iP.x()), roundDecimals(iP.y())); 
		EventPoint temp = outPoints.get(roundP);
		if (temp == null) {
			temp = new EventPoint(iP);
			outPoints.put(roundP, temp);
		}
		temp.addNew(seg1);
		temp.addNew(seg2);
		return temp;
	}

}
