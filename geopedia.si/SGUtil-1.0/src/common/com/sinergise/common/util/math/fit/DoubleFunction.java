package com.sinergise.common.util.math.fit;

public interface DoubleFunction {
	
	public static class Util {
		private Util() {}
		
		public static DoubleFunction CONSTANT_ONE = new DoubleFunction() {
			@Override
			public double apply(double value) {
				return 1;
			}
		};

		public static DoubleFunction LINEAR_IDENTITY = new DoubleFunction() {
			@Override
			public double apply(double value) {
				return value;
			}
		};
	}
	
	double apply(double value);
}
