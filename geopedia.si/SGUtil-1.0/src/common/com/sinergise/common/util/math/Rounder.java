package com.sinergise.common.util.math;

/**
 * Provides rounding of numbers. There are two ways to use it: <br>
 * The first way is to simply call roundIt(double,int) and be done with it, the second is to use one of the Rounder instances available
 * statically or via getRounder(int). The advantage of the second approach is twofold - performance is slightly better (around 25% less
 * time/op), and there is no need to keep track of the desired precision. <br>
 * <br>
 * For convenience, RounderNone is also provided, which just returns the parameter.
 * 
 * @author Mitja
 */
public abstract class Rounder {
	public Rounder() {}
	
	public abstract double round(double value);
	
	public static final Rounder Rounder10   = new Rounder() {
		                                        @Override
		                                        public double round(final double value) {
			                                        return Math.floor(0.5 + value * 10000000000.0) / 10000000000.0;
		                                        }
	                                        };
	public static final Rounder Rounder9    = new Rounder() {
		                                        @Override
		                                        public double round(final double value) {
			                                        return Math.floor(0.5 + value * 1000000000.0) / 1000000000.0;
		                                        }
	                                        };
	public static final Rounder Rounder8    = new Rounder() {
		                                        @Override
		                                        public double round(final double value) {
			                                        return Math.floor(0.5 + value * 100000000.0) / 100000000.0;
		                                        }
	                                        };
	public static final Rounder Rounder7    = new Rounder() {
		                                        @Override
		                                        public double round(final double value) {
			                                        return Math.floor(0.5 + value * 10000000.0) / 10000000.0;
		                                        }
	                                        };
	public static final Rounder Rounder6    = new Rounder() {
		                                        @Override
		                                        public double round(final double value) {
			                                        return Math.floor(0.5 + value * 1000000.0) / 1000000.0;
		                                        }
	                                        };
	public static final Rounder Rounder5    = new Rounder() {
		                                        @Override
		                                        public double round(final double value) {
			                                        return Math.floor(0.5 + value * 100000.0) / 100000.0;
		                                        }
	                                        };
	public static final Rounder Rounder4    = new Rounder() {
		                                        @Override
		                                        public double round(final double value) {
			                                        return Math.floor(0.5 + value * 10000.0) / 10000.0;
		                                        }
	                                        };
	public static final Rounder Rounder3    = new Rounder() {
		                                        @Override
		                                        public double round(final double value) {
			                                        return Math.floor(0.5 + value * 1000.0) / 1000.0;
		                                        }
	                                        };
	public static final Rounder Rounder2    = new Rounder() {
		                                        @Override
		                                        public double round(final double value) {
			                                        return Math.floor(0.5 + value * 100.0) / 100.0;
		                                        }
	                                        };
	public static final Rounder Rounder1    = new Rounder() {
		                                        @Override
		                                        public double round(final double value) {
			                                        return Math.floor(0.5 + value * 10.0) / 10.0;
		                                        }
	                                        };
	public static final Rounder Rounder0    = new Rounder() {
		                                        @Override
		                                        public double round(final double value) {
			                                        return Math.floor(0.5 + value);
		                                        }
	                                        };
	public static final Rounder RounderM1   = new Rounder() {
		                                        @Override
		                                        public double round(final double value) {
			                                        return Math.floor(0.5 + value / 10) * 10;
		                                        }
	                                        };
	public static final Rounder RounderM2   = new Rounder() {
		                                        @Override
		                                        public double round(final double value) {
			                                        return Math.floor(0.5 + value / 100) * 100;
		                                        }
	                                        };
	public static final Rounder RounderM3   = new Rounder() {
		                                        @Override
		                                        public double round(final double value) {
			                                        return Math.floor(0.5 + value / 1000) * 1000;
		                                        }
	                                        };
	public static final Rounder RounderM4   = new Rounder() {
		                                        @Override
		                                        public double round(final double value) {
			                                        return Math.floor(0.5 + value / 10000) * 10000;
		                                        }
	                                        };
	public static final Rounder RounderM5   = new Rounder() {
		                                        @Override
		                                        public double round(final double value) {
			                                        return Math.floor(0.5 + value / 100000) * 100000;
		                                        }
	                                        };
	public static final Rounder RounderM6   = new Rounder() {
		                                        @Override
		                                        public double round(final double value) {
			                                        return Math.floor(0.5 + value / 1000000) * 1000000;
		                                        }
	                                        };
	public static final Rounder RounderM7   = new Rounder() {
		                                        @Override
		                                        public double round(final double value) {
			                                        return Math.floor(0.5 + value / 10000000) * 10000000;
		                                        }
	                                        };
	public static final Rounder RounderM8   = new Rounder() {
		                                        @Override
		                                        public double round(final double value) {
			                                        return Math.floor(0.5 + value / 100000000) * 100000000;
		                                        }
	                                        };
	public static final Rounder RounderM9   = new Rounder() {
		                                        @Override
		                                        public double round(final double value) {
			                                        return Math.floor(0.5 + value / 1000000000) * 1000000000;
		                                        }
	                                        };
	public static final Rounder RounderM10  = new Rounder() {
		                                        @Override
		                                        public double round(final double value) {
			                                        return Math.floor(0.5 + value / 10000000000.0) * 10000000000.0;
		                                        }
	                                        };
	public static final Rounder RounderNone = new Rounder() {
		                                        @Override
		                                        public double round(final double value) {
			                                        return value;
		                                        }
	                                        };
	
	public static Rounder getRounder(final int decDigits) {
		switch (decDigits) {
			case -10:
				return RounderM10;
			case -9:
				return RounderM9;
			case -8:
				return RounderM8;
			case -7:
				return RounderM7;
			case -6:
				return RounderM6;
			case -5:
				return RounderM5;
			case -4:
				return RounderM4;
			case -3:
				return RounderM3;
			case -2:
				return RounderM2;
			case -1:
				return RounderM1;
			case 0:
				return Rounder0;
			case 1:
				return Rounder1;
			case 2:
				return Rounder2;
			case 3:
				return Rounder3;
			case 4:
				return Rounder4;
			case 5:
				return Rounder5;
			case 6:
				return Rounder6;
			case 7:
				return Rounder7;
			case 8:
				return Rounder8;
			case 9:
				return Rounder9;
			case 10:
				return Rounder10;
			default:
				throw new IllegalArgumentException();
		}
	}
	
	private static final double[] vals = {1, // 0
	        10, // 1
	        100, // 2
	        1000, // 3
	        10000, // 4
	        100000, // 5
	        1000000, // 6
	        10000000, // 7
	        100000000, // 8
	        1000000000, // 9
	        10000000000.0,// 10
	        100000000000.0,// 11
	        1000000000000.0,// 12
	        10000000000000.0,// 13
	        100000000000000.0         // 14
	                                   };
	
	/**
	 * Rounds a value to the specified number of decimal places. Valid numbers of places are -10 to 10. The number is effectively rounded to
	 * nearest 0.1<sup>decPlaces</sup>:
	 * 
	 * <pre>
	 *      value     decPlaces      result
	 *   12345.6789      -3         12000.0
	 *   12345.6789      -1         12350.0
	 *   12345.6789       0         12346.0
	 *   12345.6789       1         12345.7
	 *   12345.6789       3         12345.679
	 * </table>
	 * 
	 * @param value
	 * @param decPlaces
	 * @return
	 */
	
	public static double roundIt(final double value, final int decPlaces) {
		// don't 'optimize' out division by multiplication
		// with inverses - integers above are exactly representable
		// as doubles, but decimal fractions aren't.
		// 
		// While division is usually implemented in hardware by
		// inverting the divisor and then multiplying, the hardware
		// can do so internally with greater precision
		// (e.g. x86 uses 80 bits)
		//
		// Also, don't use longs for obvious reasons (loss of NaNs and
		// infinities). While the code below is also not perfect,
		// it's still much more robust than converting to long (with
		// Math.round for example).
		
		if (decPlaces == 0) {
			return Math.rint(value);
		}
		
		if (decPlaces > 0) {
			final double div = vals[decPlaces];
			return Math.rint(value * div) / div;
		}
		
		final double div = vals[-decPlaces];
		return Math.rint(value / div) * div;
	}
}
