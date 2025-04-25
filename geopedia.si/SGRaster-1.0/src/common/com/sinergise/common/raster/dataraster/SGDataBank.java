package com.sinergise.common.raster.dataraster;


import static java.lang.Double.NaN;
import static java.lang.Double.isNaN;

import java.io.*;

import com.sinergise.common.geometry.crs.transform.AffineTransform2D;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.geom.EnvelopeL;

abstract public class SGDataBank implements Closeable, Serializable {
	protected EnvelopeL dataEnv = new EnvelopeL();
	protected AffineTransform2D worldTr;

	/**
	 * @return transform to get from indices (i, j) to world locations (x, y)
	 */
	public AffineTransform2D getWorldTr() {
		return worldTr;
	}
	
	/** For serialization purposes only */
	@Deprecated
	protected SGDataBank() {
	}
	
	public void setWorldTr(AffineTransform2D worldTr) {
		this.worldTr = worldTr;
	}

	public SGDataBank(AffineTransform2D worldTr) {
		this.worldTr = worldTr;
	}
	
	public SGDataBank(double xStep, double yStep) {
		this(AffineTransform2D.createScale(xStep, yStep));
	}

	public void updateTransient() {
	}
	
	public void forceOffset(long newMinx, long newMiny) {
		dataEnv = dataEnv.setOrigin(newMinx, newMiny);
	}

	public boolean isEmpty() {
		return dataEnv.isEmpty();
	}
	
	public boolean isEmpty(long x, long y) {
		return Double.isNaN(getValue(x, y));
	}

	public void translate(long deltaX, long deltaY) {
		dataEnv = dataEnv.translate(deltaX, deltaY);
	}

	protected abstract void expandDataStore(final int rowsBottom, final int rowsTop, final int colsLeft, final int colsRight);
	
	public SGDataBank expandToInclude(long x0, long y0, long x1, long y1) {
		if (dataEnv.isEmpty()) { //empty
			dataEnv = new EnvelopeL(x0, y0, x1, y1);
			expandDataStore(0, (int)dataEnv.getHeight(), 0, (int)dataEnv.getWidth());
			updateTransient();
			return this;
		}
		final int rowsBottom = Math.max(0, (int)(dataEnv.getMinY() - y0));
		final int rowsTop = Math.max(0, (int)(y1 - dataEnv.getMaxY()));
		final int colsLeft = Math.max(0, (int)(dataEnv.getMinX() - x0));
		final int colsRight = Math.max(0, (int)(x1 - dataEnv.getMaxX()));
		if (rowsBottom + rowsTop + colsLeft + colsRight == 0) {
			return this;
		}
		System.out.println("Growing "+rowsBottom+" "+rowsTop+" "+colsLeft+" "+colsRight);
		dataEnv = dataEnv.expand(colsLeft, rowsBottom, colsRight, rowsTop);
		expandDataStore(rowsBottom, rowsTop, colsLeft, colsRight);
		updateTransient();
		return this;
	}
	
	public SGDataBank expandToInclude(EnvelopeL env) {
		return expandToInclude(env.getMinX(), env.getMinY(), env.getMaxX(), env.getMaxY());
	}
	
	@Override
	public String toString() {
		return "(" + dataEnv.toString() + ", " + worldTr.toString() + ")";
	}

	/**
	 * This only works properly for rectangular transforms. Rotation or shear are not taken into account.
	 * If the data bank represents a rotated or sheared grid, derivatives should be calculated differently.
	 * 
	 * @param column
	 * @param row
	 * @param out
	 * @param outOff
	 * @return
	 */
	public boolean getDerivs(final long column, final long row, final double[] out, final int outOff) {
		final double dta = getValue(column, row);
		final double sx = worldTr.getScaleX();
		final double sy = worldTr.getScaleY();
		boolean allOK = true;

		{ // Horizontal (x) derivative
			final double dtax1 = (column == dataEnv.getMinX()) ? NaN : getValue(column - 1, row);
			final double dtax2 = (column == dataEnv.getMaxX()) ? NaN : getValue(column + 1, row);

			boolean ok = false;
			if (isNaN(dtax1)) out[outOff] = (dtax2 - dta) / sx;
			else if (isNaN(dtax2)) out[outOff] = (dta - dtax1) / sx;
			else {
				out[outOff] = (0.5 * (dtax2 - dtax1)) / sx;
				ok = true;
			}
			allOK = allOK && ok;
		}

		{ // Vertical (y) derivative
			final double dtay1 = (row == dataEnv.getMinY()) ? NaN : getValue(column, row - 1);
			final double dtay2 = (row == dataEnv.getMaxY()) ? NaN : getValue(column, row + 1);

			boolean ok = false;
			if (isNaN(dtay1)) out[outOff + 1] = (dtay2 - dta) / sy;
			else if (isNaN(dtay2)) out[outOff + 1] = (dta - dtay1) / sy;
			else {
				out[outOff + 1] = (0.5 * (dtay2 - dtay1)) / sy;
				ok = true;
			}
			allOK = allOK && ok;
		}
		return allOK;		
	}

	public final boolean getValueWithDerivs(final long column, final long row, final double[] out, final int outOff) {
		final double val = getValue(column, row); 
		out[outOff] = val;
		
		boolean ok = getDerivs(column, row, out, outOff+1);
		return ok && !Double.isNaN(val);
	}

	public abstract double getValue(long x, long y);

	public abstract void setValue(long x, long y, double value);
	

	public SGDataBank compact() {
		final int w = (int)dataEnv.getWidth();
		final int h = (int)dataEnv.getHeight();
		final long x0 = dataEnv.getMinX();
		final long y0 = dataEnv.getMinY();
		
		int bot = (int)dataEnv.getHeight();
		int top = 0;
		int left = (int)dataEnv.getWidth();
		int right = 0;
		for (int i = 0; i < w * h; i++) {
			final int x = i % w;
			final int y = i / w;
			if (!isEmpty(x0 + x, y0 + y)) {
				bot = y;
				top = h - y - 1;
				left = x;
				right = w - x - 1;
				break;
			}
		}
		for (int i = 0; i < top * w; i++) {
			final int x = i % w;
			final int y = h - 1 - i / w;
			if (!isEmpty(x0 + x, y0 + y)) {
				top = h - y - 1;
				left = Math.min(left, x);
				right = Math.min(right, w - x - 1);
				break;
			}
		}
		for (int i = 0; i < left * h; i++) {
			final int x = i / h;
			final int y = i % h;
			if (!isEmpty(x0 + x, y0 + y)) {
				left = x;
				right = Math.min(right, w - x - 1);
				break;
			}
		}
		for (int i = 0; i < right * h; i++) {
			final int x = w - 1 - i / h;
			final int y = i % h;
			if (!isEmpty(x0 + x, y0 + y)) {
				right = w - x - 1;
				break;
			}
		}
		return cutBorders(top, left, bot, right);
	}
	
	@SuppressWarnings("unused")
	protected void overlayRow(SGDataBank bank, long srcRow, long srcMinCol, long thisRow, long thisMinCol, int length) throws IOException {
		for (int i = 0; i < length; i++) {
			final double val = bank.getValue(srcMinCol + i, srcRow);
			if (!Double.isNaN(val)) setValue(thisMinCol + i, thisRow, val);
		}
	}
	
	
	public void overlayAll(SGDataBank src) throws IOException {
		overlay(src, src.dataEnv);
	}

	public void overlay(SGDataBank src, EnvelopeL extent) throws IOException {
		if (!src.worldTr.equals(this.worldTr)) {
			throw new IllegalArgumentException("Cannot combine height files with different step sizes or horizontal offsets");
		}
		EnvelopeL intr = extent.intersectWith(dataEnv).intersectWith(src.getEnvelope());
		if (intr.isEmpty()) {
			return;
		}
		for (long y = intr.getMinY(); y <= intr.getMaxY(); y++) {
			overlayRow(src, y, intr.getMinX(), y, intr.getMinX(), (int)intr.getWidth());
		}
	}

	
	public abstract SGDataBank cutBorders(int top, int left, int bot, int right);

	protected final int colInData(long x) {
		return (int)(x - dataEnv.getMinX());
	}

	protected final int rowInData(long y) {
		return (int)(y - dataEnv.getMinY());
	}
	
	protected final long extCol(int colInData) {
		return dataEnv.getMinX() + colInData;
	}

	protected final long extRow(int rowInData) {
		return dataEnv.getMinY() + rowInData;
	}

	public int getWidth() {
		return (int)dataEnv.getWidth();
	}

	public int getHeight() {
		return (int)dataEnv.getHeight();
	}

	public void expandAndSet(long x, long y, double value) {
		expandToInclude(x, y, x, y);
		setValue(x, y, value);
	}
	
	public void setSize(int w, int h, boolean updateDataStore) {
		if (dataEnv.isEmpty()) {
			dataEnv = EnvelopeL.fromWH(0, 0, w, h);
		} else {
			dataEnv = EnvelopeL.fromWH(dataEnv.getMinX(), dataEnv.getMinY(), w, h);
		}
		if (updateDataStore) {
			checkDataStore();
		}
	}

	public abstract void checkDataStore();

	public EnvelopeL getEnvelope() {
		return dataEnv;
	}

	protected void checkEnv(long x, long y) {
		if (!dataEnv.contains(x, y)) {
			throw new IndexOutOfBoundsException("x,y not in envelope ("+x+","+y+"), env ="+dataEnv);
		}
	}

	public Envelope getWorldEnvelopeForWholeCells() {
		return getWorldEnvelopeForWholeCells(dataEnv);
	}

	public Envelope getWorldEnvelopeForWholeCells(EnvelopeL env) {
		return Envelope.create(worldTr.point(env.getMinX()-0.5, env.getMinY()-0.5), worldTr.point(env.getMaxX()+0.5, env.getMaxY()+0.5));
	}

	public Envelope getWorldEnvelopeForCellCentres() {
		return getWorldEnvelopeForWholeCells().expandedFor(-0.5 * worldTr.getScaleX(), -0.5 * worldTr.getScaleY());
	}

	public static SGDataBank crop(SGDataBank tileData, Envelope dataBounds) {
		if (tileData.getWorldEnvelopeForWholeCells().equals(dataBounds)) {
			return tileData;
		}
		return new SubsetDataBank(tileData, getCropEnvelope(tileData, dataBounds));
	}

	public static EnvelopeL getCropEnvelope(SGDataBank bank, Envelope worldBounds) {
		AffineTransform2D tr = bank.getWorldTr();
		double sx = tr.getScaleX();
		double sy = tr.getScaleY();
		Envelope relBnds = worldBounds.translate(-tr.getTranslateX(), -tr.getTranslateY());
		long minX = (long)Math.floor(relBnds.getMinX()/sx + 0.5);
		long minY = (long)Math.floor(relBnds.getMinY()/sy + 0.5);
		long maxX = (long)Math.ceil(relBnds.getMaxX()/sx - 0.5);
		long maxY = (long)Math.ceil(relBnds.getMaxY()/sy - 0.5);
		return bank.getEnvelope().intersectWithXY(minX, minY, maxX, maxY);
	}
}