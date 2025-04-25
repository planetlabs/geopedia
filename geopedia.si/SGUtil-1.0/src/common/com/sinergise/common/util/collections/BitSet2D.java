package com.sinergise.common.util.collections;

import com.sinergise.common.util.geom.EnvelopeI;
import com.sinergise.common.util.geom.PointI;
import com.sinergise.common.util.lang.Predicate;

public final class BitSet2D {
	private final BitSet bits;
	private final int wData;
	private final int row0;
	private final int col0;
	private final int wFull;
	private final int hFull;
	
	private boolean defaultVal = false;

	public BitSet2D(final int w, final int h) {
		this(w, h, 0, 0, w, h);
	}

	public BitSet2D(final int w, final int h, final int row0, final int col0, final int wData, final int hData) {
		this(new BitSet(wData * hData), w, h, row0, col0, wData);
	}

	public BitSet2D(final BitSet base, final int w, final int h, final int row0, final int col0, final int wData) {
		this.wFull = w;
		this.hFull = h;
		this.bits = base;
		this.row0 = row0;
		this.col0 = col0;
		this.wData = wData;
	}

	public final BitSet asBits() {
		return bits;
	}

	public final long count() {
		return bits.count();
	}

	public final boolean isSet(final int row, final int col) {
		assert col >= 0 && col < wFull: "Col index " + col + " out of bounds (0," + (wFull - 1) + ")";
		assert row >= 0 && row < hFull: "Row index " + row + " out of bounds (0," + (hFull - 1) + ")";
		if (col < col0 || row < row0) return defaultVal;
		if (col >= col0 + wData) return defaultVal; //Col larger than data
		
		long bIdx = bitIdx(row, col);
		if (bIdx >= bits.sizeBits) return defaultVal; //Row larger than data
		return bits.isSet(bIdx);
	}

	public final boolean isSet(final PointI position) {
		return isSet(position.y, position.x);
	}

	public final void set(final int row, final int col) {
		assert col >= 0 && col < wFull: "Col index " + col + " out of bounds (0," + (wFull - 1) + ")";
		assert row >= 0 && row < hFull: "Row index " + row + " out of bounds (0," + (hFull - 1) + ")";
		if (col < col0 || row < row0) throw new IllegalStateException("Should grow: col0="+col0+" col="+col+" row0="+row0+" row="+row);
		if (col >= col0 + wData) throw new IllegalStateException("Should grow: wData="+wData+" col-col0="+(col-col0));
		
		long bIdx = bitIdx(row, col);
		if (bIdx >= bits.sizeBits) throw new IllegalStateException("Should grow: hData="+(bits.sizeBits/wData)+" row-row0="+(row-row0));

		bits.set(bitIdx(row, col));
	}

	public final void set(final PointI position) {
		set(position.y, position.x);
	}

	public final void clear(final int row, final int col) {
		bits.clear(bitIdx(row, col));
	}

	private long bitIdx(final int row, final int col) {
		return ((long)row - row0) * wData + col - col0;
	}

	public final void clear(final PointI position) {
		clear(position.y, position.x);
	}

	public final void clearAll() {
		bits.clearAll();
		defaultVal = false;
	}

	public final void setAll() {
		bits.setAll();
		defaultVal = true;
	}

	public final void flip(final int row, final int col) {
		bits.flip(bitIdx(row, col));
	}

	public final int getHeight() {
		return hFull;
	}

	public final int getWidth() {
		return wFull;
	}

	public final boolean containsAll(final EnvelopeI env) {
		return env.isTrueForAll(new Predicate<PointI>() {
			@Override
			public boolean eval(PointI p) {
				return isSet(p);
			}
		});
	}

	public final boolean intersects(final EnvelopeI tempEnv) {
		for (PointI p : tempEnv) {
			if (isSet(p.y, p.x)) { 
				return true;
			}
		}
		return false;
	}

	/**
	 * @return PointI(col0, row0);
	 */
	public EnvelopeI getDataEnvelope() {
		return new EnvelopeI(col0, row0, col0 + wData -1, row0 + calcDataHeight() - 1);
	}
	
	public BitSet2D pack() {
		final int hData = calcDataHeight();
		
		final long i0 = bits.firstIndexOf(true);
		final long i1 = bits.lastIndexOf(true);
		
		final int t = i0 < 0 ? 0 : (int)(i0 / wData);
		final int b = i1 < 0 ? 0 : hData - (int)(i1 / wData + 1);
		
		final int l = countEmptyColsLeft();
		final int r = l==wData ? 0 : countEmptyColsRight();
		
		if (t == 0 && b == 0 && l == 0 && r == 0) {
			return this;
		}
		
		final BitSet2D ret = new BitSet2D(wFull, hFull, row0 + t, col0 + l, wData - l - r, hData - t - b);
		final int maxRow = row0 + hData - b - 1;
		final int maxCol = col0 + wData - r - 1;
		for (int row = row0 + t; row <= maxRow; row++) {
			for (int col = col0 + l; col <= maxCol; col++) {
				if (isSet(row, col)) {
					ret.set(row, col); 
				}
			}
		}
		return ret;
	}

	private int countEmptyColsLeft() {
		final int hData = calcDataHeight();
		for (int i = 0; i < wData; i++) {
			for (int j = 0; j < hData; j++) {
				if (isSet(row0 + j, col0 + i)) return i; 
			}
		}
		return wData;
	}

	private int countEmptyColsRight() {
		final int hData = calcDataHeight();
		for (int i = 0; i < wData; i++) {
			for (int j = 0; j < hData; j++) {
				if (isSet(row0 + j, col0 + wData - i - 1)) return i; 
			}
		}
		return wData;
	}

	protected int calcDataHeight() {
		return wData == 0 ? 0 : (int)(bits.sizeBits / wData);
	}
	
	public String toDebugString() {
		StringBuilder sb = new StringBuilder();
		sb.append("\n\n===("+row0+" "+col0+")====================================================\n");
		int hData = calcDataHeight();
		for (int i = 0; i < hData; i++) {
			for (int j = 0; j < wData; j++) {
				if (isSet(row0 + i, col0 + j)) sb.append("#");
				else sb.append("-");
			}
			sb.append("\n");
		}
		return sb.toString();
	}

	public void setFrom(BitSet2D o) {
		EnvelopeI env = o.getDataEnvelope().intersection(getDataEnvelope());
		for (PointI p : env) {
			if (o.isSet(p)) {
				set(p);
			}
		}
	}
}
