package com.sinergise.common.util.math.stats;

import java.util.ArrayList;
import java.util.List;

/**
 * Calculates statistics for a single double value over several nested time windows.
 *  
 * @author Miha
 */
public class TimeWindowStatsCollector {
	private final List<StatsHolder>[] prevStats;
	private final StatsCollector[] curStats;
	
	private final StatsCollector overall;

	private final int numWindows;
	private final long[] windowWidths;

	private int[] numHist;

	/**
	 * Constructs a new TimeWindowStatsCollector with the specified window widths. 
	 * History is initialized to such values that enable retrieval (at all times) of all sub-windows 
	 * that produced the first history entry in the previous larger window, e.g. if the window sizes were powers of two:
	 * <pre>
	 * hist1 : A.7    A.6    A.5
	 * hist2 : B.65   B.43   B.21
	 * hist3 : C.4321
	 * </pre>
	 * @param windowWidths
	 */
	public TimeWindowStatsCollector(int numVariables, long[] windowWidths) {
		this(numVariables, windowWidths, null);
	}

	@SuppressWarnings("unchecked")
	public TimeWindowStatsCollector(int numVariables, long[] windowWidths, int[] numHist) {
		for (int i = 1; i < windowWidths.length; i++) {
			double fact = windowWidths[i] / windowWidths[i - 1];
			if (fact != Math.round(fact)) {
				throw new IllegalArgumentException("Subsequent duration should be a multiple of the previous one: "
					+ windowWidths[i] + "/" + windowWidths[i - 1] + " = " + fact);
			}
		}
		this.numWindows = windowWidths.length;
		this.windowWidths = windowWidths;

		final long startT = System.currentTimeMillis();
		this.prevStats = new ArrayList[numWindows];
		this.curStats = new StatsCollector[numWindows];
		for (int i = 0; i < numWindows; i++) {
			long shouldStart = lastBracket(startT, windowWidths[i]);
			curStats[i] = new StatsCollector(numVariables, startT);
			curStats[i].setDuration(shouldStart + windowWidths[i] - startT);
			prevStats[i] = new ArrayList<StatsHolder>();
		}

		this.overall = new StatsCollector(numVariables, startT);
		
		this.numHist = numHist;
		if (this.numHist == null) {
			this.numHist = new int[numWindows];
			for (int i = 0; i < numWindows-1; i++) {
				this.numHist[i] = 2 * (int)(windowWidths[i+1] / windowWidths[i]) - 1;
			}
			this.numHist[numWindows - 1] = 1;
		}
	}

	public void add(int varIndex, double value) {
		final long ts = System.currentTimeMillis();
		checkTimestamp(ts);
		curStats[0].add(varIndex, value, 1);
		curStats[0].updateEndTime(ts);
		overall.add(varIndex, value, 1);
		overall.updateEndTime(ts);
	}

	private void checkTimestamp(long timestamp) {
		checkTimestamp(timestamp, 0);
	}

	private void checkTimestamp(final long timestamp, final int durationIndex) {
		synchronized(curStats) {
			final StatsCollector curStat = curStats[durationIndex];
			final long delta = windowWidths[durationIndex];
			final long endTime = lastBracket(curStat.getStartTime(), delta) + delta;
			if (timestamp >= endTime) {
			// too late to go into current
				if (durationIndex + 1 < numWindows) {
					// add to parent, update parent timestamps
					curStats[durationIndex + 1].addFrom(curStat);
					checkTimestamp(timestamp, durationIndex + 1); //this will reset parent
				}
				
				// add current to history
				pushToHist(durationIndex, curStat, endTime);
				curStat.reset(lastBracket(timestamp, delta), -1);
			}
		}
	}

	private static long lastBracket(final long timestamp, final long delta) {
		return timestamp - (timestamp % delta);
	}

	private void pushToHist(int index, StatsHolder curStat, long endTime) {
		List<StatsHolder> hist = prevStats[index];
		synchronized(hist) {
			StatsHolder newStat = new StatsHolder(curStat);
			newStat.msDuration = endTime - newStat.startTime;
			hist.add(0, newStat);
			if (hist.size() > numHist[index]) {
				hist.remove(hist.size()-1);
			}
		}
	}
	
	public double getMean(int binIndex, int varIdx) {
		StatsHolder latest = getLatest(binIndex);
		if (latest == null) {
			return Double.NaN;
		}
		return latest.getMean(varIdx);
	}
	
	public StatsHolder[] getHist(int durationIdx) {
		synchronized (curStats) {
			// check so that we don't send out old data
			checkTimestamp(System.currentTimeMillis());
			List<StatsHolder> hist = prevStats[durationIdx];
			if (hist.isEmpty()) return new StatsHolder[0];
			return hist.toArray(new StatsHolder[hist.size()]);
		}
	}
	
	public StatsCollector getOverall() {
		return overall;
	}

	public long getCount(int binIdx) {
		StatsHolder latest = getLatest(binIdx);
		if (latest == null) {
			return 0;
		}
		return latest.getMaxCount();
	}

	public StatsHolder[][] getAllHist() {
		StatsHolder[][] ret = new StatsHolder[numWindows][];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = getHist(i);
		}
		return ret;
	}


	public StatsHolder getPrev(int binIndex) {
		synchronized (curStats) {
			// check so that we don't send out old data
			checkTimestamp(System.currentTimeMillis());
			List<StatsHolder> hist = prevStats[binIndex];
			if (hist.isEmpty()) return null;
			return hist.get(0);
		}
	}
	
	public StatsHolder getLive(int durationIdx) {
		return curStats[durationIdx];
	}

	public StatsHolder getLatest(int durationIdx) {
		if (durationIdx == 0) {
			return getPrev(0);
		}
		synchronized (curStats) {
			// check so that we don't send out old data
			checkTimestamp(System.currentTimeMillis());
			int srcIdx = getIndexToComputeLatest(durationIdx);
			if (srcIdx < 0) {
				return getPrev(durationIdx);
			}
			return computeLatest(durationIdx, srcIdx);
		}
	}

	private StatsHolder computeLatest(int durationIdx, int srcIdx) {
		StatsCollector retSH = null;
		final int srcLen = (int)(windowWidths[durationIdx] / windowWidths[srcIdx]);
		for (int i = 0; i < srcLen; i++) {
			StatsHolder srcStat = prevStats[srcIdx].get(i);
			if (retSH == null) {
				retSH = new StatsCollector(srcStat);
			} else {
				retSH.addFrom(srcStat);
			}
		}
		return retSH;
	}

	private int getIndexToComputeLatest(int durationIdx) {
		final long delta = windowWidths[durationIdx];
		for (int i = 0; i <= durationIdx; i++) {
			if (prevStats[i].size() >= delta/windowWidths[i]) {
				return i;
			}
		}
		return -1;
	}
}
