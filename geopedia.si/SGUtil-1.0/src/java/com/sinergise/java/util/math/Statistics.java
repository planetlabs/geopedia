package com.sinergise.java.util.math;

import java.util.Collection;

public class Statistics {
	
	private double max;
	private double min;
	private double avg;
	private double dev;
	
	public Statistics(Collection<? extends Number> data) {
		double minTemp = Double.MAX_VALUE;
		double maxTemp = Double.MIN_VALUE;
		double sum = 0;
		
		for(Number val : data) {
			if(val.doubleValue() < minTemp) {
				minTemp = val.doubleValue();
			}
			if(val.doubleValue() > maxTemp) {
				maxTemp = val.doubleValue();
			}
			sum += val.doubleValue();
		}
		
		max = maxTemp;
		min = minTemp;
		avg = sum / data.size();
		
		calculateDev(data);
	}

	private void calculateDev(Collection<? extends Number> data) {
		double difSquare = 0;
		for(Number val : data) {
			difSquare += Math.pow((avg - val.doubleValue()), 2);
		}
		dev = Math.sqrt(difSquare/data.size());
	}
	
	public double getMax() {
		return max;
	}
	
	public double getMin() {
		return min;
	}
	
	public double getAvg() {
		return avg;
	}
	
	public double getDev() {
		return dev;
	}
}
