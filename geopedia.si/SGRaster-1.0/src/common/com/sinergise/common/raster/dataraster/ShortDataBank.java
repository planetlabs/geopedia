package com.sinergise.common.raster.dataraster;

import java.io.IOException;
import java.util.Arrays;

import com.sinergise.common.geometry.crs.transform.AffineTransform2D;
import com.sinergise.common.util.ArrayUtil;
import com.sinergise.common.util.geom.*;

public final class ShortDataBank extends AbstractShortDataBank {
	protected short[][] data;

	/**
	 * @deprecated Serialization only
	 */
	@Deprecated
	protected ShortDataBank() {
		super(1, 1, 0, 1);
	}
	
	public ShortDataBank(AffineTransform2D affTr, double zMin, double zStep) {
		super(affTr, zMin, zStep);
	}

	public ShortDataBank(double xStep, double yStep, double zMin, double zStep) {
		this(AffineTransform2D.createScale(xStep, yStep), zMin, zStep);
	}

	public ShortDataBank(double xStep, double yStep, double zMin, double zStep, short[][] data) {
		this(AffineTransform2D.createScale(xStep, yStep), zMin, zStep, data);
	}

	public ShortDataBank(AffineTransform2D affTr, double zMin, double zStep, short[][] data) {
		this(affTr, zMin, zStep);
		this.data = data;
		this.dataEnv = EnvelopeL.fromWH(0, 0, data[0].length, data.length);
		updateTransient();
	}

	@Override
	public void checkDataStore() {
		final int w = (int)dataEnv.getWidth();
		final int h = (int)dataEnv.getHeight();
		if (data != null && data.length == h && data[0].length == w) return;
		data = new short[h][w];
		return;
	}

	public final short[][] getDataBuffer() {
		return data;
	}

	@Override
	protected void overlayRow(SGDataBank bank, long srcRow, long srcMinCol, long thisRow, long thisMinCol, int length) throws IOException {
		if (bank instanceof ShortDataBank && ((ShortDataBank)bank).zSettingsEqual(zMin, zScale)) {
			ShortDataBank src = (ShortDataBank)bank;
			final short[] srcDta = src.data[src.rowInData(srcRow)];
			final short[] tgtDta = data[rowInData(thisRow)];
			for (int i = 0; i < length; i++) {
				final short val = srcDta[src.colInData(srcMinCol + i)];
				if (val != NO_DATA_SHORT) tgtDta[colInData(thisMinCol + i)] = val;
			}
		} else {
			super.overlayRow(bank, srcRow, srcMinCol, thisRow, thisMinCol, length);
		}
	}

	public ShortDataBank copySubset(long copyMinX, long copyMaxX, long copyMinY, long copyMaxY) {
		EnvelopeL intsctn = dataEnv.intersectWithXY(copyMinX, copyMinY, copyMaxX, copyMaxY);
		if (intsctn.isEmpty()) return null;
		int nh = (int)(copyMaxY - copyMinY + 1);
		int nw = (int)(copyMaxX - copyMinX + 1);

		ShortDataBank out = new ShortDataBank(worldTr, zMin, zScale, new short[nh][nw]);
		out.dataEnv = intsctn;

		int rowIdx = rowInData(copyMinY);
		int colIdx = colInData(copyMinX);
		for (int y = 0; y < nh; y++) {
			System.arraycopy(data[rowIdx + y], colIdx, out.data[y], 0, nw);
		}

		return out;
	}
	
	@Override
	protected void expandDataStore(final int rowsBottom, final int rowsTop, final int colsLeft, final int colsRight) {
		if (data == null) {
			data = new short[rowsBottom + rowsTop][colsLeft + colsRight];
			ArrayUtil.fill2D(data, NO_DATA_SHORT);
			return;
		}
		data = ArrayUtil.expand(data, rowsBottom, rowsTop, colsLeft, colsRight, NO_DATA_SHORT);
	}

	@Override
	public short getShortValue(long x, long y) {
		return data[rowInData(y)][colInData(x)];
	}

	@Override
	public void setShortValue(long x, long y, short value) {
		data[rowInData(y)][colInData(x)] = value;
	}

	@Override
	public void overlay(SGDataBank src, EnvelopeL extent) throws IOException {
		if (src instanceof AbstractShortDataBank) {
			final EnvelopeL intsn = extent.intersectWith(dataEnv).intersectWith(src.dataEnv);
			if (intsn.isEmpty()) {
				return;
			}
			((AbstractShortDataBank)src).getDataRectangle(intsn.getMinX(), intsn.getMinY(), (int)intsn.getWidth(), (int)intsn.getHeight(), data, colInData(intsn.getMinX()),
					rowInData(intsn.getMinY()));
		} else {
			super.overlay(src, extent);
		}
	}

	@Override
	public ShortDataBank cutBorders(int top, int left, int bot, int right) {
		if (top <= 0 && left <= 0 && bot <= 0 && right <= 0) return this;
		final int w = (int)dataEnv.getWidth();
		final int h = (int)dataEnv.getHeight();

		int newW = w - left - right;
		int newH = h - top - bot;

		if (newW <= 0 || newH <= 0) {
			data = null;
			dataEnv = new EnvelopeL();
			updateTransient();
			return this;
		}
		if (newW == w) {
			short[][] dta = new short[newH][];
			System.arraycopy(data, bot, dta, 0, newH);
			this.data = dta;
		} else {
			short[][] dta = new short[newH][newW];
			for (int i = 0; i < newH; i++) {
				System.arraycopy(data[i + bot], left, dta[i], 0, newW);
			}
			this.data = dta;
		}
		dataEnv = dataEnv.expand(-left, -bot, -right, -top);
		updateTransient();
		return this;
	}

	@Override
	public short[] getDataRow(long row, long minCol, int length, short[] out, int outIndex) {
		int srcPos = colInData(minCol);
		int outPos = outIndex;
		if (srcPos == 0 && outPos == 0 && length == getWidth()) {
			return data[rowInData(row)];
		}
		System.arraycopy(data[rowInData(row)], srcPos, out, outPos, length);
		return out;
	}

	@Override
	public void setDataRow(long row, long minCol, int length, short[] inData, int inStart) {
		System.arraycopy(inData, inStart, data[rowInData(row)], colInData(minCol), length);
	}

	public static ShortDataBank createCopy(AbstractShortDataBank sdb, long minCol, long minRow, int w, int h) throws IOException {
		short[][] dta = sdb.getDataRectangle(minCol, minRow, w, h, new short[h][w], 0, 0);
		ShortDataBank ret = new ShortDataBank(sdb.worldTr, sdb.zMin, sdb.zScale, dta);
		ret.dataEnv = EnvelopeL.fromWH(minCol, minRow, w, h);
		return ret;
	}
	
	private static double[] getMinMaxZ(SGDataBank dataBank, long xOffset, long yOffset, long width, long height) {
		EnvelopeL env = dataBank.getEnvelope();
		double[] minMaxZ = new double[] {Double.MAX_VALUE, -Double.MAX_VALUE};
		
		if (dataBank instanceof ShortDataBank) {
			minMaxZ[0] = ((ShortDataBank) dataBank).zMin;
			minMaxZ[1] = minMaxZ[0] + ((ShortDataBank) dataBank).zScale * 0xFFFF;
		}
		
		else if (dataBank instanceof SubsetDataBank) {
			return getMinMaxZ(((SubsetDataBank) dataBank).source, env.getMinX(), env.getMinY(), env.getWidth(), env.getHeight());
		}
		
		else if (dataBank instanceof CompositeDataBank) {
			for (SGDataBank childBank: ((CompositeDataBank) dataBank).banks) {
				double[] childMinMaxZ = getMinMaxZ(childBank, childBank.getEnvelope().getMinX(), childBank.getEnvelope().getMinY(), 
					childBank.getWidth(), childBank.getHeight());
				
				minMaxZ[0] = Math.min(childMinMaxZ[0], minMaxZ[0]);
				minMaxZ[1] = Math.max(childMinMaxZ[1], minMaxZ[1]);
			}
		}
		
		else {
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					double value = dataBank.getValue(x + xOffset, y + yOffset);
					minMaxZ[0] = Math.min(value, minMaxZ[0]);
					minMaxZ[1] = Math.max(value, minMaxZ[1]);
				}
			}
		}
		return minMaxZ;
	}
	
	private static void copyShortValues(SGDataBank dataBank, short[][] destValues, long destXOffset, long destYOffset,
		long srcXOffset, long srcYOffset, long srcWidth, long srcHeight, double zMin, double zScale) {
		
		EnvelopeL env = dataBank.getEnvelope();
		srcXOffset -= env.getMinX();
		srcYOffset -= env.getMinY();
		
		if (dataBank instanceof ShortDataBank) {
			ShortDataBank shortBank = (ShortDataBank) dataBank;
			destXOffset += srcXOffset;
			destYOffset += srcYOffset;
			short[][] srcValues = ((ShortDataBank) dataBank).getDataBuffer();
			if (srcValues != null) {
				for (int y = 0; y < srcHeight; y++) {
					for (int x = 0; x < srcWidth; x++) {
						double value = srcValues[(int) (y + srcYOffset)][(int) (x + srcXOffset)] * shortBank.zScale + shortBank.zMin;
						destValues[(int) (y + destYOffset)][(int) (x + destXOffset)] = (short) ((value - zMin) / zScale);
					}
				}
			}
		}
		
		else if (dataBank instanceof SubsetDataBank) {
			copyShortValues(((SubsetDataBank) dataBank).source, destValues, destXOffset, destYOffset, env.getMinX(), env.getMinY(), env.getWidth(), env.getHeight(), zMin, zScale);
		}
		
		else if (dataBank instanceof CompositeDataBank) {
			for (SGDataBank childBank: ((CompositeDataBank) dataBank).banks) {
				EnvelopeL childEnv = childBank.getEnvelope();
				copyShortValues(childBank, destValues, destXOffset, destYOffset, childEnv.getMinX(), childEnv.getMinY(), childEnv.getWidth(), childEnv.getHeight(), zMin, zScale);
			}
		}
		
		else {
			for (int y = 0; y < srcHeight; y++) {
				for (int x = 0; x < srcWidth; x++) {
					double value = dataBank.getValue(x + srcXOffset, y + srcYOffset);
					destValues[(int) (y + srcYOffset)][(int) (x + srcXOffset)] = (short) ((value - zMin) / zScale);
				}
			}
		}
	}
	
	public static ShortDataBank convertFrom(SGDataBank dataBank) {
		if (dataBank instanceof ShortDataBank) {
			return (ShortDataBank) dataBank;
		}
		
		int width = dataBank.getWidth();
		int height = dataBank.getHeight();
		double[] minMaxZ = getMinMaxZ(dataBank, 0, 0, width, height);
		double zMin = minMaxZ[0];
		double zMax = minMaxZ[1];
		double zScale = (zMax - zMin) / 0xFFFF;
		
		short[][] values = new short[height][];
		
		for (int y = 0; y < height; y++) {
			values[y] = new short[width];
			Arrays.fill(values[y], (short) 0xFFFF);
		}
		
		EnvelopeL env = dataBank.getEnvelope();
		copyShortValues(dataBank, values, -env.getMinX(), -env.getMinY(), 0, 0, width, height, zMin, zScale);
		
		return new ShortDataBank(dataBank.getWorldTr(), zMin, zScale, values);
	}

	@Override
	public void close() {
		data = null;
	}

	public void setDataBuffer(short[][] dataStore) {
		final int w = (int)dataEnv.getWidth();
		final int h = (int)dataEnv.getHeight();
		if (dataStore == null || dataStore.length != h || dataStore[0].length != w) {
			throw new IllegalArgumentException("Trying to set data buffer with illegal size");
		}
		this.data = dataStore;
	}
}