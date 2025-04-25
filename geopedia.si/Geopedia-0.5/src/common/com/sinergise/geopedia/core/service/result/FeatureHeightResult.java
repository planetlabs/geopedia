package com.sinergise.geopedia.core.service.result;

import java.io.Serializable;
import java.util.ArrayList;

import com.sinergise.common.geometry.geom.Point;
import com.sinergise.common.util.math.MathUtil;

public class FeatureHeightResult implements Serializable{
	private static final long serialVersionUID = -1187047347503633402L;
	
	public ArrayList<Double> heights = new ArrayList<Double>();
	public double minHeight = Double.MAX_VALUE;
	public double maxHeight = Double.MIN_VALUE;
	public double projectedLength;
	public double startHeight;
	public double endHeight;
	public double elevationGain=0;
	public double elevationLoss=0;
	public double climbDistance=0;
	public double descentDistance=0;
	public ArrayList<Point> points = new ArrayList<Point>();
	
	
	public void addNewSegment(double length, double prevZ, double currZ) {
		double diffZ = currZ-prevZ;
		if (diffZ>0) {
			elevationGain+=diffZ;
			climbDistance+=MathUtil.hypot(length, diffZ);
		} else {
			elevationLoss+=(diffZ*-1);
			descentDistance+=MathUtil.hypot(length, diffZ*-1);
		}
	}
}
