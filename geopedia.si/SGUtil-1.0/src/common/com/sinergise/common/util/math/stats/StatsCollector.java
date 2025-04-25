package com.sinergise.common.util.math.stats;

import java.util.Arrays;

public class StatsCollector extends StatsHolder {
	private static final long serialVersionUID = -5333835657658536129L;
	@Deprecated
	protected StatsCollector() {}

	public StatsCollector(int numVariables, long startTime) {
		super(numVariables, startTime, 0);
		Arrays.fill(min, Double.POSITIVE_INFINITY);
		Arrays.fill(max, Double.NEGATIVE_INFINITY);
	}

	public StatsCollector(StatsHolder other) {
		super(other);
		setFrom(other);
	}

	public synchronized void setFrom(StatsHolder other) {
		final int len = min.length;
		System.arraycopy(min, 0, other.min, 0, len);
		System.arraycopy(max, 0, other.max, 0, len);
		System.arraycopy(weight, 0, other.weight, 0, len);
		System.arraycopy(wSum, 0, other.wSum, 0, len);
		System.arraycopy(wSumSq, 0, other.wSumSq, 0, len);
	}

	public synchronized void reset(long newStartTime, long msNewDuration) {
		this.startTime = newStartTime;
		this.msDuration = msNewDuration;
		
		Arrays.fill(this.min, Double.POSITIVE_INFINITY);
		Arrays.fill(this.max, Double.NEGATIVE_INFINITY);
		Arrays.fill(this.weight, 0);
		Arrays.fill(this.wSum, 0);
		Arrays.fill(this.wSumSq, 0);
	}

	public synchronized void addFrom(StatsHolder other) {
		for (int i = 0; i < min.length; i++) {
			min[i] = Math.min(other.min[i], min[i]);
			max[i] = Math.max(other.max[i], max[i]);
			weight[i] += other.weight[i];
			wSum[i] += other.wSum[i];
			wSumSq[i] += other.wSumSq[i];
		}
		final long oldEndTime = getEndTime();
		startTime = Math.min(startTime, other.startTime);
		updateEndTime(Math.max(oldEndTime, other.getEndTime()));
	}
	
	/**
	 * used for calculating stats of an array with only one type of variables with a constant weight 1
	 * 
	 */
	public synchronized void simpleAdd(double value) {
		add(0, value, 1);
	}
	
	public synchronized void add(final int varIndex, final double value, final double sampleWeight) {
		if (Double.isNaN(value) || sampleWeight <= 0) {
			return;
		}
		final double wVal = value * sampleWeight;
		min[varIndex] = Math.min(value, min[varIndex]);
		max[varIndex] = Math.max(value, max[varIndex]);
		weight[varIndex] += sampleWeight;
		wSum[varIndex] += wVal;
		wSumSq[varIndex] += sampleWeight * value * value;
	}
	
	public synchronized void add(double[] allVars, double sampleWeight) {
		for (int i = 0; i < allVars.length; i++) {
			add(i, allVars[i], sampleWeight);
		}
	}

	public synchronized void add(double[] allVars, double[] sampleWeights) {
		for (int i = 0; i < allVars.length; i++) {
			add(i, allVars[i], sampleWeights[i]);
		}
	}

	
	public void setDuration(long msDuration) {
		this.msDuration = msDuration;
	}
	
	public void updateEndTime(long endTime) {
		this.msDuration = endTime - startTime;
	}
}
