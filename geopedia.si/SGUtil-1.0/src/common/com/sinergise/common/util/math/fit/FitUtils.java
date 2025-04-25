/*
 *
 */
package com.sinergise.common.util.math.fit;



public class FitUtils {
	public static interface FunCalculator<F, T> {
		double apply(F fun, T srcArr, int idx);
	}
	
	
    public static <TX> double[] linearFit(TX[] srcX, double[] srcY, double[] srcYsig, ToDoubleFunction<TX>[] funs) {
    	FunCalculator<ToDoubleFunction<TX>, TX[]> fCalc = new FunCalculator<ToDoubleFunction<TX>, TX[]>() {
			@Override
			public double apply(ToDoubleFunction<TX> fun, TX[] srcArr, int idx) {
				return fun.applyAsDouble(srcArr[idx]);
			}
		};
		return linearFit(fCalc, srcX, srcY, srcYsig, funs);
	}
    
    public static double[] linearFit(double[] srcX, double[] srcY, double[] srcYsig, DoubleFunction[] funs) {
    	FunCalculator<DoubleFunction, double[]> fCalc = new FunCalculator<DoubleFunction, double[]>() {
			@Override
			public double apply(DoubleFunction fun, double[] srcArr, int idx) {
				return fun.apply(srcArr[idx]);
			}
		};
		return linearFit(fCalc, srcX, srcY, srcYsig, funs);
    }
    public static <F, T> double[] linearFit(FunCalculator<F, T> fCalc, T srcX, double[] srcY, double[] srcYsig, F[] funs) {
    	int N = srcY.length;
    	int M = funs.length;
    	double[][] Ajk = new double[M][M];
		double[] Bk = new double[M];
		double[] funVals = new double[M];
		for (int i = 0; i < N; i++) {
			double invSigY = 1.0 / srcYsig[i];
			double yi = srcY[i] * invSigY;
			applyFunctions(fCalc, funs, M, srcX, i, invSigY, funVals);

			for (int j = 0; j < M; j++) {
				double fj = funVals[j];
				double[] Aj = Ajk[j];

				for (int k = 0; k < M; k++) {
					Aj[k] += fj * funVals[k];
				}
				Bk[j] += yi * fj;
			}
		}
		AffineFitUtil.linearSolve(Ajk, Bk);
		return Bk;
	}
    
	public static <F, T> void applyFunctions(FunCalculator<F, T> fCalc, F[] funs, int fLen, T srcX, int srcIdx, double factor, double[] out) {
		for (int i = fLen-1; i >= 0; i--) {
			out[i] = fCalc.apply(funs[i], srcX, srcIdx) * factor;
		}
	}
	
	public static <TX> double compute(FitFunction<TX> funcs, double[] a, TX x, double[] dyda) {
        funcs.set(x, a);
        funcs.getDerivs(dyda);
        return funcs.getY();
    }
}
