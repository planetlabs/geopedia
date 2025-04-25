package com.sinergise.common.raster.dataraster;

import java.util.LinkedList;
import java.util.List;

import com.sinergise.common.geometry.crs.transform.AffineTransform2D;

public class CompositeDataBank extends SGDataBank {
	List<SGDataBank> banks = new LinkedList<SGDataBank>();
	
	/** For serialization purposes only */
	@Deprecated
	protected CompositeDataBank() {
	}
	
	public CompositeDataBank(AffineTransform2D at) {
		super(at);
	}
	
	public void add(SGDataBank bank) {
		if (!bank.getWorldTr().equals(worldTr)) {
			throw new IllegalArgumentException("Trying to add invalid bank - should have the same world transform");
		}
		banks.add(bank);
		dataEnv = dataEnv.expandToInclude(bank.getEnvelope());
	}
	
	@Override
	public void close() {
	}

	@Override
	public double getValue(long x, long y) {
		checkEnv(x, y);
		for (SGDataBank b : banks) {
			if (b.getEnvelope().contains(x, y)) {
				double val = b.getValue(x, y);
				if (!Double.isNaN(val)) {
					return val;
				}
			}
		}
		return Double.NaN;
	}
	
	@Override
	protected void expandDataStore(int rowsBottom, int rowsTop, int colsLeft, int colsRight) {
		throw new UnsupportedOperationException("CompositeDataBank is immutable");
	}

	@Override
	public void setValue(long x, long y, double value) {
		throw new UnsupportedOperationException("CompositeDataBank is immutable");
	}

	@Override
	public SGDataBank cutBorders(int top, int left, int bot, int right) {
		throw new UnsupportedOperationException("CompositeDataBank is immutable");
	}

	@Override
	public void checkDataStore() {
		throw new UnsupportedOperationException("SubsedDataBank is unmodifiable");
	}

}
