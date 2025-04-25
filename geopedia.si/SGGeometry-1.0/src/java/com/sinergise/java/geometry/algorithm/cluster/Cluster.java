package com.sinergise.java.geometry.algorithm.cluster;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.sinergise.common.util.geom.HasCoordinate;


public class Cluster implements HasCoordinate {
	private double x;
	private double y;
	private double r;
	private List<HasCoordinate> pointsInsideCluster;
	
	public Cluster(double x, double y, double r, List<HasCoordinate> pointsInsideCluster){
		this.x = x;
		this.y = y;
		this.r = r;
		this.pointsInsideCluster = pointsInsideCluster;
	}
	
	public Collection<HasCoordinate> getPointsInsideCluster() {
		return Collections.unmodifiableCollection(pointsInsideCluster);
	}
	
	@Override
	public double x() {
		return x;
	}
	
	@Override
	public double y() {
		return y;
	}
	
	public double getRadius() {
		return r;
	}

	public int size() {
		return pointsInsideCluster.size();
	}
}
