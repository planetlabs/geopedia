package com.sinergise.common.raster.dataraster;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.sinergise.common.geometry.crs.transform.AffineTransform2D;

public abstract class AbstractShortDataBank extends SGDataBank {
	public static final short NO_DATA_SHORT = (short)0xFFFF;

	@Override
	protected void overlayRow(SGDataBank bank, long srcRow, long srcMinCol, long thisRow, long thisMinCol, int length) throws IOException {
		if (bank instanceof AbstractShortDataBank && ((AbstractShortDataBank)bank).zSettingsEqual(zMin, zScale)) {
			AbstractShortDataBank sb = (AbstractShortDataBank)bank;
			short[] dataRow = new short[length];
			sb.getDataRow(srcRow, srcMinCol, length, dataRow, 0);
			for (int i = 0; i < length; i++) {
				final short val = dataRow[i];
				if (val == NO_DATA_SHORT) dataRow[i] = getShortValue(thisMinCol + i, thisRow);
			}
			setDataRow(thisRow, thisMinCol, length, dataRow, 0);
		} else {
			super.overlayRow(bank, srcRow, srcMinCol, thisRow, thisMinCol, length);
		}
	}
	
	public boolean zSettingsEqual(double otherZMin, double otherZScale) {
		return this.zMin == otherZMin && this.zScale == otherZScale;
	}
	

	public double zScale;
	public transient double derivFactorX;
	public transient double derivFactorY;
	public double zMin;
	
	public boolean isContinous = true;
	public boolean noDataAsZero = false;
	
	/** For serialization purposes only */
	@Deprecated
	protected AbstractShortDataBank() {
	}

	@Override
	public void updateTransient() {
		super.updateTransient();
		derivFactorX = 0.5 * zScale / worldTr.getScaleX();
		derivFactorY = 0.5 * zScale / worldTr.getScaleY();
	}

	protected final short calcShortVal(double zVal) {
		if (Double.isNaN(zVal)) {
			return NO_DATA_SHORT;
		}
		assert zVal >= zMin : "Value too low ("+zVal+"; min="+zMin+")";
		assert Math.round((zVal - zMin)/zScale) <= 0xFFFF: "Value too large ("+zVal+"; max="+(zMin + (0xFFFE * zScale))+")";
		return (short)Math.round((zVal - zMin)/zScale);
	}

	protected final double calcHeightVal(final short dta) {
		if (dta == NO_DATA_SHORT) {
			return Double.NaN;
		}
		return zMin + (dta & 0xFFFF) * zScale;
	}

	protected void addInternal(long x, long y, short value) {
		expandToInclude(x, y, x, y);
		setShortValue(x, y, value);
	}
	
	@Override
	public void setValue(long x, long y, double value) {
		setShortValue(x, y, calcShortVal(value));
	}
	

	@Override
	public double getValue(long x, long y) {
		return calcHeightVal(getShortValue(x, y));
	}

	public double getAverageDouble(long colStart, long rowStart, int numCols, int numRows) {
		int cnt = 0;
		double avg = 0;
		for (long x = colStart; x < colStart + numCols; x++) {
			for (long y = rowStart; y < rowStart + numRows; y++) {
				if (dataEnv.contains(x, y)) {
					final short dta = getShortValue(x, y);
					if(noDataAsZero) {
						cnt++;
						avg += (dta & 0xFFFF);
					} else {
						if (dta != NO_DATA_SHORT) {
							cnt++;
							avg += (dta & 0xFFFF);
						}
					}
					
				}
			}
		}
		if (cnt == 0) return Double.NaN;
		return zMin + zScale*avg/cnt;
	}
	
	public double getMostCommonDouble(long colStart, long rowStart, int numCols, int numRows) {
		Map<Short, AtomicInteger> freqMap = new HashMap<Short, AtomicInteger>();
		for (long x = colStart; x < colStart + numCols; x++) {
			for (long y = rowStart; y < rowStart + numRows; y++) {
				if (dataEnv.contains(x, y)) {
					Short dtaObj = Short.valueOf(getShortValue(x, y));
					if (freqMap.containsKey(dtaObj)) {
						freqMap.put(dtaObj, new AtomicInteger(1));
					} else {
						freqMap.get(dtaObj).incrementAndGet();
					}
				}
			}
		}
		int maxCnt = 0;
		Short returnValue = null;
		for (Map.Entry<Short, AtomicInteger> f : freqMap.entrySet()) {
			int curr = f.getValue().intValue();
			if (curr > maxCnt) {
				maxCnt = curr;
				returnValue = f.getKey();
			}
		}
		if (returnValue == null) {
			return Double.NaN;
		}
		return calcHeightVal(returnValue.shortValue());
	}
	
	/**
	 * Recursive 2x2 averaging method. 
	 * 
	 * @param x Left value index
	 * @param y Top value index
	 * @param depth How many iterations. Depth of 0 returns the one direct height value at (x, y), 
	 * depth of 1 the average of 4 values, depth of 2 the average of 16 values, etc. 
	 * @param reduceZStep If the internal computation is using different zScale for different zoom levels.
	 * @return Average height value
	 */
	public double getAverageHeight(long x, long y, int depth, boolean reduceZStep) {
		double zScaleBetter = calculateReducedZScale(zScale, (depth > 0) ? depth - 1 : 0, reduceZStep);
		double zMinBetter = calculateReducedZMin(zMin, zScaleBetter);
		
		if (depth <= 0) {
			if (getEnvelope().contains(x, y)) {
				int heightShort = getShortValue(x, y);
				if (noDataAsZero || (heightShort != NO_DATA_SHORT)) {
					return zMinBetter + zScaleBetter * (heightShort & 0xFFFF);
				}
			}
			return Double.NaN;
		}
		
		int count = 0;
		double heightSum = 0;
		
		for (long xOfs = 0; xOfs < 2; xOfs++) {
			for (long yOfs = 0; yOfs < 2; yOfs++) {
				int jump = (1 << (depth - 1));
				
				if (depth > 1) {
					double height = getAverageHeight(x + ((xOfs > 0) ? jump : 0), y + ((yOfs > 0) ? jump : 0), depth - 1, reduceZStep);
					if (!Double.isNaN(height)) {
						count++;
						heightSum += (height - zMinBetter) / zScaleBetter;
					}
				}
				else if (getEnvelope().contains(xOfs + x, yOfs + y)) {
					int heightShort = getShortValue(xOfs + x, yOfs + y);
					if (noDataAsZero || (heightShort != NO_DATA_SHORT)) {
						count++;
						heightSum += heightShort & 0xFFFF;
					} 
				}
			}
		}
		
		if (count == 0) return Double.NaN;

		double heightAverage = zMinBetter + zScaleBetter * heightSum / count;
		double zScaleLesser = calculateReducedZScale(zScale, depth, reduceZStep);
		double zMinLesser = calculateReducedZMin(zMin, zScaleLesser);
		return Math.round((heightAverage - zMinLesser) / zScaleLesser) * zScaleLesser + zMinLesser;
	}
	
	/**
	 * Converts zScale to some other zoom level's scale.
	 * 
	 * @param sourceZScale zScale of the source zoom level.
	 * @param zoomLevel Difference from the source to the target (less detailed or same as the source) zoom level.
	 * @param reduceZStep If the internal computation is using different zScale for different zoom levels.
	 * @return rescaled zScale
	 */
	public static double calculateReducedZScale(double sourceZScale, int zoomLevelDifference, boolean reduceZStep) {
		return (reduceZStep && (zoomLevelDifference >= 0)) ? sourceZScale * (1 << zoomLevelDifference) : sourceZScale;
	}
	
	/**
	 * Converts zMin to some other zoom level's representation of zMin.
	 * 
	 * @param sourceZMin zMin of the source zoom level.
	 * @param sourceZScale zScale of the source zoom level.
	 * @param zoomLevel Difference from the source to the target (less detailed or same as the source) zoom level.
	 * @param reduceZStep If the internal computation is using different zScale for different zoom levels.
	 * @return rescaled zMin
	 */
	public static double calculateReducedZMin(double sourceZMin, double sourceZScale, int zoomLevelDifference, boolean reduceZStep) {
		double targetZScale = calculateReducedZScale(sourceZScale, zoomLevelDifference, reduceZStep);
		return calculateReducedZMin(sourceZMin, targetZScale);
	}
	
	/**
	 * Converts zMin to some other zoom level's representation of zMin.
	 * 
	 * @param sourceZMin zMin of the source zoom level.
	 * @param targetZScale zScale of the target zoom level.
	 * @return rescaled zMin
	 */
	public static double calculateReducedZMin(double sourceZMin, double targetZScale) {
		if ((sourceZMin == 0) || (targetZScale == 0)) {
			return 0;
		}
		return (sourceZMin >= 0) ? targetZScale * Math.floor(sourceZMin / targetZScale) : -targetZScale * Math.ceil(-sourceZMin / targetZScale);
	}

	@Override
	public final AbstractShortDataBank expandToInclude(long x0, long y0, long x1, long y1) {
		return (AbstractShortDataBank)super.expandToInclude(x0, y0, x1, y1);
	}

	public AbstractShortDataBank(double xStep, double yStep, double zMin, double zScale) {
		this(AffineTransform2D.createScale(xStep, yStep), zMin, zScale);
	}
	public AbstractShortDataBank(AffineTransform2D affTr, double zMin, double zScale) {
		super(affTr);
		this.zMin = zMin;
		this.zScale = zScale;
		updateTransient();
	}

	public short[][] getDataRectangle(long minX, long minY, int rectW, int rectH, short[][] out, int outCol, int outRow) throws IOException {
		for (int i = 0; i < rectH; i++) {
			getDataRow(minY+i, minX, rectW, out[outRow+i], outCol);
		}
		return out;
	}
	
	/**
	 * This will overwrite the specified range of the out[]
	 * 
	 * @param row
	 * @param minCol
	 * @param length
	 * @param out
	 * @param outCol
	 * @throws IOException
	 */
	public abstract short[] getDataRow(long row, long minCol, int length, short[] out, int outStart) throws IOException;

	public abstract void setDataRow(long row, long minCol, int length, short[] inData, int inStart) throws IOException;
	
	protected abstract void setShortValue(long x, long y, short value);
	
	public abstract short getShortValue(long x, long y);

}