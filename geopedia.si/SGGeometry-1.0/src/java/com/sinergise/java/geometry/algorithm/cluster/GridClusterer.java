package com.sinergise.java.geometry.algorithm.cluster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.geom.EnvelopeBuilder;
import com.sinergise.common.util.geom.HasCoordinate;

public class GridClusterer {

	/**
	 * Comparator that sorts plane parts cells according to the number of
	 * points they contain. The biggest cell comes first.
	 */
	public static class PlanePartComparator implements Comparator<PlanePart>
	{
		@Override
		public int compare(PlanePart pp1, PlanePart pp2) {
			if(pp1.numOfPoints < pp2.numOfPoints) return 1;
			else if (pp1.numOfPoints > pp2.numOfPoints) return -1;
			return 0;
		}
	}

	/**
	 * Comparator that sorts cluster points outside of symbol border -
	 * most distant come first.
	 */
	public static class PointsOutsideOfSymbolComparator implements Comparator<HasCoordinate>{

		private double xCenter;
		private double yCenter;
		
		public PointsOutsideOfSymbolComparator(double xCenter, double yCenter){
			this.xCenter = xCenter;
			this.yCenter = yCenter;
		}
		
		@Override
		public int compare(HasCoordinate o1, HasCoordinate o2) {

			double d1Sq = (o1.x() - xCenter)*(o1.x() - xCenter) + (o1.y() - yCenter)*(o1.y() - yCenter);
			double d2Sq = (o2.x() - xCenter)*(o2.x() - xCenter) + (o2.y() - yCenter)*(o2.y() - yCenter);

			if(d1Sq > d2Sq) return 1;
			if(d1Sq < d2Sq) return -1;
			return 0;
		}
	}
	/**
	 * Class that controls a single cluster. Knows how many points are added
	 * to cluster and how many are outside of the symbol border.
	 * Calculates all intersections between this cluster and potential neighbors.
	 */

	public class GridCluster {

		List<HasCoordinate> pointsInsideCluster;

		CellIndex gridID;
		private int numOfPoints;
		private double xCenter;
		private double yCenter;
		private double rad = radius;

		/**
		 * constructor - always constructs an empty cluster, located at (0,0).
		 * Center is moved when points are added.
		 * @param itg: index of the plane part (cell) on which cluster is located.
		 */
		public GridCluster(CellIndex itg){

			this.xCenter = 0;
			this.yCenter = 0;
			this.gridID = itg;
			this.pointsInsideCluster= new ArrayList<HasCoordinate>();
			numOfPoints = 0;
		}

		public List<HasCoordinate> getClusterPoints(){
			return pointsInsideCluster;
		}

		public int getNumOfPoints(){
			return numOfPoints;
		}

		public CellIndex getGridId(){
			return gridID;
		}
		
		public double getXCenter(){
			return xCenter;
		}

		public double getYCenter(){
			return yCenter;
		}
		
		public void setRadius(int rad){
			this.rad = rad > radius ? rad : radius;
		}

		public double getRad(){
			return rad;
		}

		/**
		 * General function that walks through potential neighbors (based on neighbor cells).
		 * @return true if cluster intersects any other clusters and false if not (so there is space to make a new cluster).
		 */
		public boolean intersectsNeighbours(CellIndex ppIndex) {
			
			if(maxRadius == 0) maxRadius = radius*1.15;
			if(minRadius == 0) minRadius = radius*0.85;

			this.rad = numOfPoints > maxNum ? maxRadius : numOfPoints < maxNum && numOfPoints > minNum ? radius : minRadius;

			PlanePart pp = planeDivided.get(ppIndex == null ? gridID : ppIndex);
			for (CellIndex cIdx : pp.neighbourParts) {
				if(intersectsASingleNeighbour(cIdx)) return true;
			}
			return false;
		}

		/**
		 * Function that controls if a cluster intersects another cluster in a particular neighbor cell (if it exists)
		 * @param neighbourId : id of the plane part (cell) where we look for a cluster
		 * @return true if they intersect, false otherwise (also false, if no cluster exists in this cell).
		 */
		public boolean intersectsASingleNeighbour(CellIndex neighbourId){
			return intersects(clusters.get(neighbourId));
		}

		/**
		 * Function that checks for another cluster if it intersects with this one.
		 * @param otherCluster: the cluster for which we want to check.
		 * @return true if they intersect, false otherwise.
		 */
		public boolean intersects(GridCluster otherCluster){
			if (otherCluster == null) return false;
			
			double distSq = (otherCluster.getXCenter() - this.xCenter)*(otherCluster.getXCenter() - this.xCenter) + 
			(otherCluster.getYCenter() - this.yCenter)*(otherCluster.getYCenter() - this.yCenter);
			if(distSq < (otherCluster.rad + this.rad)*(otherCluster.rad + this.rad)) return true;
			return false;
		}
		
		/**
		 * Function that detects points located outside of symbol border and sorts them by distance - 
		 * most distant come first.
		 * @return List of HasCordinate outside of symbol border.
		 */
		public List<HasCoordinate> detectpointsOfsideOfRadius() {
			List<HasCoordinate> pointsOutsideOfRadius = new ArrayList<HasCoordinate>();
			double distSq = 0;
			for (HasCoordinate hc : pointsInsideCluster) {
				distSq = (hc.x() - xCenter)* (hc.x() - xCenter) + (hc.y() - yCenter)* (hc.y() - yCenter); 
				if(distSq > rad*rad) pointsOutsideOfRadius.add(hc);
			}
			Collections.sort(pointsOutsideOfRadius, new PointsOutsideOfSymbolComparator(xCenter, yCenter));

			return pointsOutsideOfRadius;
		}

		/**
		 * Function that adds yet unsorted point to a cluster. First, the point is added, then
		 * new cluster position is checked - if the new position would intersect with another cluster,
		 * we add a point without moving the center.
		 * @param hc
		 */
		public void checkAddingAPoint(HasCoordinate hc, CellIndex ppIndex){

			double tempXCenter = this.xCenter;
			double tempYCenter = this.yCenter;
			
			addPoint(hc, true);

			if (intersectsNeighbours(null) || intersectsNeighbours(ppIndex)){ 
				this.pointsInsideCluster.remove(hc);
				this.xCenter =  tempXCenter;
				this.yCenter = tempYCenter;
				this.numOfPoints = numOfPoints - 1;
				addPoint(hc, false);
			}
		}
		
		/**
		 * Function that adds new point to the cluster.
		 *  and moves its center.
		 * @param newPoint: point to be added.
		 * @param moveCenter: if true, adding this point should also correct the location of the center. If false, center is not moved.
		 */
		public void addPoint(HasCoordinate newPoint, boolean moveCenter) {
			this.pointsInsideCluster.add(newPoint);
			if (moveCenter) {
				this.xCenter = (xCenter*numOfPoints + newPoint.x())/(numOfPoints+1);
				this.yCenter = (yCenter*numOfPoints + newPoint.y())/(numOfPoints+1);
			}
			numOfPoints++;
		}

		/**
		 * Function that removes the point from the cluster but does not move the center.
		 * @param point
		 */
		public void removePointWithoutMovingCenter(HasCoordinate point){
			this.pointsInsideCluster.remove(point);
			this.numOfPoints--;
		}
	}
	
	private static final class CellIndex {
		public final int x;
		public final int y;
		
		public CellIndex(int x, int y) {
			this.x = x;
			this.y = y;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + x;
			result = prime * result + y;
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			CellIndex other = (CellIndex)obj;
			if (x != other.x) return false;
			if (y != other.y) return false;
			return true;
		}
		
	}
	
	/**
	 * Class that holds points that belong to a cell of the plane.
	 * Also knows its neighbour cells and if any cluster is located in it.
	 */
	private class PlanePart {

		public CellIndex index;
		public CellIndex[] neighbourParts;
		public boolean isHostForCluster;
		public List<HasCoordinate> pointsInsidePlanePart;
		public int numOfPoints;
	

		public PlanePart(CellIndex index){
			this.index = index;
			this.pointsInsidePlanePart = new ArrayList<HasCoordinate>();
			this.numOfPoints = 0;
			this.neighbourParts = new CellIndex[8];
			setNeighbourPartsIdx();
			this.isHostForCluster = false;
		}

		/**
		 * neighboring cell indexes
		 */
		private void setNeighbourPartsIdx(){
			neighbourParts[0] = new CellIndex(index.x-1, index.y+1); //upleft
			neighbourParts[1] = new CellIndex(index.x, index.y+1); //up
			neighbourParts[2] = new CellIndex(index.x+1, index.y+1);//upright
			neighbourParts[3] = new CellIndex(index.x+1, index.y); //right
			neighbourParts[4] =	new CellIndex(index.x+1, index.y-1); //downright
			neighbourParts[5] = new CellIndex(index.x, index.y-1); //down
			neighbourParts[6] = new CellIndex(index.x-1, index.y-1); //downleft
			neighbourParts[7] = new CellIndex(index.x-1, index.y); //left
		}

		public void addToPlanePart(HasCoordinate hc){
			this.pointsInsidePlanePart.add(hc);
			this.numOfPoints++;
		}

		public boolean isPointInside(HasCoordinate hc){
			return this.pointsInsidePlanePart.contains(hc) ? true : false;

		}
	}

	public Collection<HasCoordinate> coordinates; 
	public HashMap<HasCoordinate, HashMap<String, Integer>> attrs;

	public LinkedHashMap<CellIndex, PlanePart> planeDivided;
	public LinkedHashMap<CellIndex, GridCluster> clusters;

	public double radius;
	public double cellSize;
	public int tempKey;
	public double width;
	public double height;
	public int xSize;
	public int ySize;
	public double maxRadius = 0;
	public double minRadius = 0;
	public int maxNum = 50;
	public int minNum = 30;//default
	
	public Envelope borderEnvelope;

	/**
	 * 
	 * @param coordinates: list of all coordinates
	 * @param radius: radius of the middle symbol - plane will be divided to cells with size 2*radius
	 */
	public GridClusterer(double gridSize){
		this(gridSize, 0, 0, 30, 50);
	}
	
	/**
	 * 
	 * @param coordinates: list of all coordinates
	 * @param radius: radius of the middle symbol - plane will be divided to cells with size 2*radius
	 * @param maxRadius: radius of the maximum sized symbol
	 * @param minRadius: radius of the minimum sized symbol
	 * @param maxNum: if more than maxNum points in cluster, maxRadius will be used
	 * @param minNum: if less than minNum points in cluster, minRadius will be used
	 */
	public GridClusterer(double gridSize, double minRadius, double maxRadius, int minNum, int maxNum){
		setParams(gridSize, minRadius, maxRadius, minNum, maxNum);
	}

	private void initialize(Collection<HasCoordinate> coords) {
		this.coordinates = coords;
		this.attrs = null;
		this.cellSize = 2*radius;
		this.tempKey = 0;
		this.planeDivided = new LinkedHashMap<CellIndex, GridClusterer.PlanePart>();
		this.clusters = new LinkedHashMap<CellIndex, GridClusterer.GridCluster>();
		this.borderEnvelope = new Envelope();
		defineEnvelope();
	}

	private void setParams(double gridSize, double minRadius, double maxRadius, int minNum, int maxNum) {
		this.radius = gridSize;
		this.minRadius = minRadius;
		this.maxRadius = maxRadius;
		this.maxNum = maxNum;
		this.minNum = minNum;

		if (minRadius >= gridSize || gridSize >= maxRadius){
			throw new IllegalArgumentException("Invalid radius boundaries; should be minRadius < gridSize < maxRadius");
		}
		if (minNum >= maxNum){
			throw new IllegalArgumentException("Invalid boundaries for numbers of points; should be minNum <= maxNum");
		}
	}

	private void defineEnvelope() {

		EnvelopeBuilder bld = new EnvelopeBuilder();
		bld.expandToInclude(this.coordinates);
		
		final double minX = bld.getMinX();
		final double maxX = bld.getMaxX();
		final double minY = bld.getMinY();
		final double maxY = bld.getMaxY();

		//expand in each direction
		Envelope tempEnvelope = new Envelope(minX - minX % cellSize, maxX - maxX % cellSize + cellSize, minY- minY % cellSize, maxY - maxY % cellSize + cellSize);
		
		this.width = tempEnvelope.getWidth();
		this.height = tempEnvelope.getMaxY() - tempEnvelope.getMinY();
		
		this.xSize = (int)Math.round(width/cellSize);
		this.ySize = (int)Math.round(height/cellSize);

		this.borderEnvelope = tempEnvelope;
	}

	/**
	 * Center function that controls the process of clustering.
	 * @param wdh: frame width
	 * @param hgt: frame height
	 */
	public Cluster[] buildClusters(Collection<HasCoordinate> coords) {
		initialize(coords);
		return buildClusters();
	}
	
	private Cluster[] buildClusters() {

		//FIRST: divide points to cells
		for (HasCoordinate h : this.coordinates) {
			assignPointToPlaneCell(h);
		}
		//sort cells - the one with most points comes first
		List<PlanePart> plD = new ArrayList<GridClusterer.PlanePart>(planeDivided.values());
		Collections.sort(plD, new PlanePartComparator());

		//SECOND: make clusters where it is possible
		for (int i = 0; i < plD.size(); i++) {
			PlanePart pp = plD.get(i);
			//if no clusters exist in this cell, possible make one
			if(!pp.isHostForCluster) makeClusters(pp, true);
		}
		
		//THIRD: add unsorted points - from plane parts where clusters were not made because of possible intersections
		for (PlanePart pp : plD) {
			if(!pp.isHostForCluster) addUnsortedPointsToClusters(pp);
		}
		//now all points are already in clusters.
		
		//FOURTH: go through clusters again, detect points outside symbol borders and check if they could be closer to other clusters
		movePoints();
		
		//FIFTH: prepare results
		return prepareResults();
	}

	/**
	 * Function that prepares results that to be drawn. From each cluster an instance of ClusterResults is made,
	 * with information of location, number of points, types of points, etc.
	 * @return
	 */
	private Cluster[] prepareResults() {
		int count = 0;
		Cluster [] clusterResults = new Cluster[clusters.size()];
		for (GridCluster clus : clusters.values()) {
			clusterResults[count++] = new Cluster(clus.getXCenter(), clus.getYCenter(), clus.getRad(), clus.pointsInsideCluster);
		}
		return clusterResults;
	}

	/**
	 * Function that checks all clusters after all points have been sorted and possibly moves some points to closer clsuers.
	 * @return
	 */
	private void movePoints() {

		for (GridCluster cl : clusters.values()) {

			//find the points outside cluster symbol border
			List<HasCoordinate> pointsOutside= cl.detectpointsOfsideOfRadius();
			for (HasCoordinate hc : pointsOutside) {
				CellIndex clusId = cl.gridID;
				PlanePart pp = planeDivided.get(clusId);
				boolean isInside = pp.isPointInside(hc); 
				if (!isInside) { //if point is not in the same cell as cluster center, find the right cell
					for (CellIndex tempI : pp.neighbourParts) {
						PlanePart newPP = planeDivided.get(tempI);
						if(newPP == null) continue;
						if (newPP.isPointInside(hc)) {
							pp = newPP;
							break;
						}
					}
				}
				//find closest cluster index
				CellIndex minDistIdx = walkThroughNeighbours(hc, pp, cl.gridID, false);
				//if point is closer to some other cluster, move
				if (!clusId.equals(minDistIdx)) {
					cl.removePointWithoutMovingCenter(hc); //remove from existing cluster
					clusters.get(minDistIdx).addPoint(hc, false); //add to new cluster, but do not move the center - we don't want to destroy fragile clusters' balance
				} 
			}
		}
	}

	/**
	 * Function that adds points from plane parts that do not host a cluster (because of intersections)
	 * to existing clusters.
	 * @param p: plane part to be checked.
	 */
	private void addUnsortedPointsToClusters(PlanePart p) {

		for (HasCoordinate hc : p.pointsInsidePlanePart) {
			//add a point and check location of the cluster center
			clusters.get(walkThroughNeighbours(hc, p, null, true)).checkAddingAPoint(hc, p.index);
		}
		p.isHostForCluster = true;
	}

	/**
	 * Function that finds the closest cluster for a particular point. Used for adding unsorted points to clusters and for moving points between clusters.
	 * @param hc: point to be examined.
	 * @param p: plane part to which the point belongs.
	 * @param currentClusId: if point already belongs to a cluster, its ID. If point is unsorted, this parameter is not used, can be anything.
	 * @param unsorted: true if point does not belong to any cluster yet and false if it belongs to a cluster and we are just moving it.
	 * @return: the ID of the closest cluster.
	 */
	private CellIndex walkThroughNeighbours(HasCoordinate hc, PlanePart p, CellIndex currentClusId, boolean unsorted) {

		CellIndex minDistNeighbour;
		double minDistSq;
		double tempDistSq;

		if (unsorted) {
			minDistNeighbour = null;
			minDistSq = Double.MAX_VALUE;
		} else{
			minDistNeighbour = currentClusId;
			GridCluster currentCluster = clusters.get(currentClusId);
			minDistSq = (currentCluster.getXCenter() - hc.x())*(currentCluster.getXCenter() - hc.x()) + (currentCluster.getYCenter() - hc.y())*(currentCluster.getYCenter() - hc.y()); 
		}

		for (CellIndex curNeigbour : p.neighbourParts) {
			GridCluster neighbour = clusters.get(curNeigbour); //if there is a cluster in this plane part, calculate the distance from its center to point.
			if (neighbour == null) continue;
			tempDistSq = (hc.x()- neighbour.getXCenter())*(hc.x()- neighbour.getXCenter()) + (hc.y()- neighbour.getYCenter())*(hc.y()- neighbour.getYCenter());
			if (tempDistSq < minDistSq) {
				minDistSq = tempDistSq;
				minDistNeighbour = curNeigbour;
			}
		}
		return minDistNeighbour;
	}

	/**
	 * Function that constructs clusters from plane parts.
	 * @param p: plane part
	 * @param markPlaneParts: option for later - it is possible to make clusters again. Not used so far.
	 */
	private void makeClusters(PlanePart p, boolean markPlaneParts) {

		//first, we make a cluster candidate
		GridCluster candidate = new GridCluster(p.index);
		for (HasCoordinate hc : p.pointsInsidePlanePart) {
			candidate.addPoint(hc, true);
		}
		//check if candidate intersects any existing cluster
		boolean intersects = candidate.intersectsNeighbours(null);
		//if it doesn't, add candidate to the map of clusters
		if (!intersects) {
			clusters.put(p.index, candidate);
			if(markPlaneParts)p.isHostForCluster = true;
		}
		//if it does, do nothing, points in this plane part will be added later.
	}

	/**
	 * Function that adds all points to correct plane parts (cells).
	 * Integer key is the index of the particular cell, calculated form its position.
	 * @param hc: point to be added
	 */
	private void assignPointToPlaneCell(HasCoordinate hc){
		
		int n = (int)((hc.x() - borderEnvelope.getMinX())/cellSize);
		int m = (int)((-hc.y() + borderEnvelope.getMaxY())/cellSize);
		CellIndex key = new CellIndex(n, m);
		
		if (planeDivided.containsKey(key)) { //if some points already exist in this cell, add point
			planeDivided.get(key).addToPlanePart(hc);
		} else { //no points in this plane part yet, make new plane part and add point
			PlanePart p = new PlanePart(key);
			p.addToPlanePart(hc);
			planeDivided.put(key, p);
		}
	}
}
