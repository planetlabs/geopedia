package com.sinergise.common.util.math.stats;

import java.io.Serializable;


public class StatsHolder implements Serializable {
	private static final long serialVersionUID = 1L;
	protected long msDuration = 0;
	protected long startTime = -1;
	
	protected double[] min;
	protected double[] max;
	protected double[] weight;
	protected double[] wSum;
	protected double[] wSumSq;

	@Deprecated
	protected StatsHolder() {
	}

	protected StatsHolder(int numVariables, long startTime, long msDuration) {
		this.startTime = startTime;
		this.msDuration = msDuration;
		
		this.min = new double[numVariables];
		this.max = new double[numVariables];
		this.weight = new double[numVariables];
		this.wSum = new double[numVariables];
		this.wSumSq = new double[numVariables];
	}
	
	public StatsHolder(double singleValue, long startTime, long msDuration) {
		this(singleValue, singleValue, singleValue, 1, singleValue, startTime, msDuration);
	}
	
	public StatsHolder(double min, double max, double avg, double weight, double stDev, long startTime, long msDuration) {
		this(1, startTime, msDuration);
		final double vSum = avg * weight;
		final double stdCount = stDev * weight;
		final double vSumSq = (stdCount * stdCount - vSum * vSum) / weight;

		this.min[0] = min;
		this.max[0] = max;
		this.weight[0] = weight;
		this.wSum[0] = vSum;
		this.wSumSq[0] = vSumSq;
	}

	public StatsHolder(StatsHolder other) {
		this(other.min.length, other.startTime, other.msDuration);
		int len = other.min.length;
		
		System.arraycopy(other.min, 0, this.min, 0, len);
		System.arraycopy(other.max, 0, this.max, 0, len);
		System.arraycopy(other.weight, 0, this.weight, 0, len);
		System.arraycopy(other.wSum, 0, this.wSum, 0, len);
		System.arraycopy(other.wSumSq, 0, this.wSumSq, 0, len);
	}

	public long getCount(int varIndex) {
		return Math.round(weight[varIndex]);
	}

	public long getMaxCount() {
		double maxCnt = Double.NEGATIVE_INFINITY;
		for (double cnt : weight) {
			if (cnt > maxCnt) {
				maxCnt = cnt;
			}
		}
		return maxCnt > 0 ? Math.round(maxCnt) : 0;
	}

	public synchronized double getMean(int varIndex) {
		return wSum[varIndex] / weight[varIndex];
	}

	public synchronized double getStDev(int varIndex) {
		double s0 = weight[varIndex];
		double s1 = wSum[varIndex];
		return Math.sqrt((wSumSq[varIndex] * s0 - s1 * s1) / (s0 * (s0 - 1)));
	}

	public boolean isEmpty() {
		for (double l : weight) {
			if (l > 0) {
				return false;
			}
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (startTime >= 0) {
			sb.append(startTime).append(':').append(msDuration).append(' ');
		}
		for (int i = 0; i < min.length; i++) {
			sb.append('[');
			if (weight[i] == 0) {
				sb.append('0');
			} else {
				sb.append(weight[i])
					.append(": ")
					.append(min[i])
					.append(" < ")
					.append(getMean(i))
					.append(" < ")
					.append(max[i]);
			}
			sb.append(']');
		}
		return sb.toString();
	}

	public long getEndTime() {
		return startTime + msDuration;
	}
	
	public long getStartTime() {
		return startTime;
	}

	public double getEventsPerSecond(int varIndex) {
		return getCount(varIndex) * 1000.0 / msDuration;
	}

	public double getMaxEventsPerSecond() {
		return getMaxCount() * 1000.0 / msDuration;
	}

	public long getDuration() {
		return msDuration;
	}

	public double getMin(int varIndex) {
		return min[varIndex];
	}
	
	public double getMax(int varIndex) {
		return max[varIndex];
	}
}