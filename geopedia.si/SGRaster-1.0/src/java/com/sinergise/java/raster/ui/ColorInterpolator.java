package com.sinergise.java.raster.ui;

import com.sinergise.common.util.math.interpolation.PiecewiseInterpolationHelper1D;

public interface ColorInterpolator {
	public static final class ColorVal {
		private double a;
		private double r;
		private double g;
		private double b;
		
		public ColorVal(int clr) {
			a = (clr >>> 24 & 0xff)/255.0;
	        r = (clr >>> 16 & 0xff)/255.0;
	        g = (clr >>> 8  & 0xff)/255.0;
	        b = (clr        & 0xff)/255.0;
		}
		public ColorVal() {
		}
		public ColorVal(double a, double r, double g, double b) {
			this.a = a;
			this.r = r;
			this.g = g;
			this.b = b;
		}
		public ColorVal setLinearCombination(ColorVal c1, ColorVal c2, double ratio) {
			double r1 = 1.0 - ratio;
			a = r1*c1.a + ratio*c2.a;
			r = r1*c1.r + ratio*c2.r;
			g = r1*c1.g + ratio*c2.g;
			b = r1*c1.b + ratio*c2.b;
			return this;
		}
		
		public int toIntColor() {
			return //
				(((int)(a * 255.99)) << 24) | //
				(((int)(r * 255.99)) << 16) | //
				(((int)(g * 255.99)) << 8) | //
				(((int)(b * 255.99)));
		}
		public ColorVal setAndMultiplyColorComponents(ColorVal in, double fct) {
			a = in.a;
			r = fct*in.r;
			g = fct*in.g;
			b = fct*in.b;
			return this;
		}
		public ColorVal set(ColorVal other, double f) {
			a = f*other.a;
			r = f*other.r;
			g = f*other.g;
			b = f*other.b;
			return this;
		}
		public ColorVal add(ColorVal other, double f) {
			a += f*other.a;
			r += f*other.r;
			g += f*other.g;
			b += f*other.b;
			return this;
		}
		public int toIntColor(double cf) {
			cf*=255.99;
			return //
				(((int)(a * 255.99)) << 24) | //
				(((int)(r * cf)) << 16) | //
				(((int)(g * cf)) << 8) | //
				(((int)(b * cf)));
		}
	}
	public static final class Const implements ColorInterpolator {
		ColorVal value;
		
		public Const(ColorVal value) {
			this.value = value;
		}
		
		@Override
		public ColorVal getInterpolatedValue(double x, ColorVal ret) {
			return value; 
		}
	}
	
	public static final class Linear extends PiecewiseInterpolationHelper1D implements ColorInterpolator {
		public final ColorVal[] yVals;

		public Linear(double[] xVals, ColorVal[] yVals) {
			super(xVals);
			this.yVals = yVals;
		}

		@Override
		public ColorVal getInterpolatedValue(double x, ColorVal ret) {
			final double ratio = indexAndRatio(x);
			if (ratio == 0) {
				return yVals[binIndex];
			}
			return ret.setLinearCombination(yVals[binIndex], yVals[binIndex + 1], ratio);
		}
	}
	
	//TODO: Implement optimized binary search-based interpolator without calculating interval ratio
	// store previous bin; if searched value in last bin, return last value
	// else binary search and return value in last bin
	public static final class PiecewiseConst extends PiecewiseInterpolationHelper1D implements ColorInterpolator {
		public final ColorVal[] yVals;

		public PiecewiseConst(double[] xVals, ColorVal[] yVals) {
			super(xVals);
			this.yVals = yVals;
		}

		@Override
		public ColorVal getInterpolatedValue(double x, ColorVal ret) {
			indexAndRatio(x);
			return yVals[binIndex];
		}
	}

	ColorVal getInterpolatedValue(double x, ColorVal ret);
}