package com.sinergise.common.util.math.fit;

import com.sinergise.common.util.ArrayUtil;
import com.sinergise.common.util.math.AngleUtil;

public class AffineFitUtil {

	public static final class AffineFitFunction implements FitFunctionMultiD<double[]> {
		double	sx		= 0;
		double	sy		= 0;
	
		double	s		= 1;
		double	sR		= 1;
		double	z		= 0;
		double	tX		= 0;
		double	tY		= 0;
		double	cos		= 1;
		double	sin		= 0;
	
		double	cmzs	= 0;
		double	zcms	= 0;
		double	zcps	= 0;
		double	cpzs	= 0;
	
		@Override
		public int length() {
			return 6;
		}
	
		@Override
		public void getY(double[] yRet) {
			yRet[0] = s * (cmzs * sx + sR * zcms * sy) + tX;
			yRet[1] = s * (zcps * sx + sR * cpzs * sy) + tY;
		}
	
		@Override
		public void set(double[] x, double[] params) {
			sx = x[0];
			sy = x[1];
	
			s = params[0];
			sR = params[1];
			double fi = params[2];
			z = params[3];
			tX = params[4];
			tY = params[5];
			cos = Math.cos(fi);
			sin = Math.sin(fi);
	
			cmzs = (cos - z * sin);
			zcms = (z * cos - sin);
			zcps = (z * cos + sin);
			cpzs = (cos + z * sin);
		}
	
		@Override
		public void getDerivs(double[][] dyda) {
			dyda[0][0] = sR * sy * zcms + sx * cmzs;
			dyda[0][1] = sx * zcps + sR * sy * cpzs;
			dyda[1][0] = s * sy * zcms;
			dyda[1][1] = s * sy * cpzs;
			dyda[2][0] = -s * (sx * zcps + sR * sy * cpzs);
			dyda[2][1] = s * (sR * sy * zcms + sx * cmzs);
			dyda[3][0] = s * (cos * sR * sy - sin * sx);
			dyda[3][1] = s * (cos * sx + sin * sR * sy);
			dyda[4][0] = 1;
			dyda[4][1] = 0;
			dyda[5][0] = 0;
			dyda[5][1] = 1;
		}
	}

	public static final int	IDX_SCALE	= 0;
	public static final int	IDX_STRETCH	= 1;
	public static final int	IDX_ROTATE	= 2;
	public static final int	IDX_SHEAR	= 3;
	public static final int	IDX_TRX		= 4;
	public static final int	IDX_TRY		= 5;

	public static double getRMSAffine(double[][] source, double[][] dest, double[] paramsAffine) {
		double sum = 0;
		for (int i = 0; i < source.length; i++) {
			double xT = paramsAffine[0] * source[i][0] + paramsAffine[2] * source[i][1] + paramsAffine[4];
			double yT = paramsAffine[1] * source[i][1] + paramsAffine[3] * source[i][0] + paramsAffine[5];
			double dX = dest[i][0] - xT;
			double dY = dest[i][1] - yT;
			sum += dX * dX + dY * dY;
		}
		return Math.sqrt(sum / source.length);
	}

	public static double getRMSHelmert(double[][] source, double[][] dest, double[] paramsAffine) {
		double sum = 0;
		for (int i = 0; i < source.length; i++) {
			double xT = paramsAffine[0] * source[i][0] + paramsAffine[1] * source[i][1] + paramsAffine[2];
			double yT = paramsAffine[0] * source[i][1] - paramsAffine[1] * source[i][0] + paramsAffine[3];
			double dX = dest[i][0] - xT;
			double dY = dest[i][1] - yT;
			sum += dX * dX + dY * dY;
		}
		return Math.sqrt(sum / source.length);
	}

	public static double[] paramsToAffine(double[] params) {
		double s = params[0];
		double sR = params[1];
		double fi = params[2];
		double z = params[3];
		double cos = Math.cos(fi);
		double sin = Math.sin(fi);
	
		return new double[]{s * (cos - sin * z), s * (sin + cos * z), s * sR * (-sin + cos * z), s * sR * (cos + sin * z), params[4], params[5]};
	}

	//TODO: Test this
	public static double[] affineToParams(double[] affine) {
		final double mxx = affine[0];
		final double myx = affine[1];
		final double mxy = affine[2];
		final double myy = affine[3];
	
		final double mxx2 = mxx * mxx;
		final double myx2 = myx * myx;
		final double mxx2myx2 = mxx2 + myx2;
		final double det = mxx * myy - mxy * myx;
	
		final double sR = Math.signum(det) * Math.sqrt((mxy * mxy + myy * myy) / mxx2myx2);
		final double s = Math.sqrt(0.5 * (mxx2myx2 + det / sR));
	
		final double s2CosFi = mxx + myy / sR;
		final double s2SinFi = myx - mxy / sR;
	
		final double fi = Math.atan2(s2SinFi, s2CosFi);
		final double z = (myx + mxy / sR) / s2CosFi;
	
		return new double[]{s, sR, fi, z, affine[4], affine[5]};
	}

	/**
	 * <ol>
	 * <li><b>s</b>: scale (0..inf, default 1)</li>
	 * <li><b>sR</b>: scY/scX scale ratio (-inf..inf, default -1)</li>
	 * <li><b>fi</b>: rotation (-PI < fi <= PI, default 0)</li>
	 * <li><b>z</b>: shear (-1 .. 1, default 0)</li>
	 * <li><b>trX</b>: x translation (-inf .. inf; should not normally be preset)</li>
	 * <li><b>trY</b>: y translation (-inf .. inf; should not normally be preset)</li>
	 * </ol>
	 * 
	 * @param source
	 * @param dest
	 * @param fixedParams array double[6]; NaN indicates param that will be fitted; value indicates the chosen fixed
	 *            value
	 * @return transform params {mxx, mxy, myx, myy, trX, trY}; can be converted to affine by using paramsToAffine()
	 *         function
	 */
	@SuppressWarnings("null")
	public static double[] fit2DAffine(double[][] source, double[][] dest, double[] fixedParams) {
		double[] params = new double[]{1, -1, 0, 0, 0, 0};
		int freeParams = 0;
		reduceParams(2 * source.length, fixedParams, new int[]{AffineFitUtil.IDX_SHEAR, AffineFitUtil.IDX_STRETCH, AffineFitUtil.IDX_SCALE, AffineFitUtil.IDX_ROTATE, AffineFitUtil.IDX_TRY, AffineFitUtil.IDX_TRX}, params);
		for (int i = 0; i < fixedParams.length; i++) {
			double value = fixedParams[i];
			if (!Double.isNaN(value)) {
				params[i] = value;
			} else {
				freeParams++;
			}
		}
		if (freeParams == 6) { //General affine
			return fitGeneralAffine(source, dest);
			
		} else if (freeParams == 4 && fixedParams[1] == -1 && fixedParams[3] == 0) { //Helmert 2D
			double[][] helmSrc = new double[source.length][2];
			for (int i = 0; i < helmSrc.length; i++) {
				helmSrc[i][0] = source[i][0];
				helmSrc[i][1] = -source[i][1];
			}
			double[] hParams = fitHelmert2DreturnAffine(helmSrc, dest);
			hParams[2] *= -1;
			hParams[3] *= -1;
			return hParams;
			
		} else if (freeParams == 2 && Double.isNaN(fixedParams[4]) && Double.isNaN(fixedParams[5]) && source.length==1) { //translation
			
			return new double[]{0.15,0,0,-0.15,dest[0][0]-0.15*source[0][0],dest[0][1]+0.15*source[0][1]};
		}
	
		//TODO: Revert to linear fit if possible (full affine, helmert, scX+scY no rot, scX=scY no rot)(trX and trY are always linear)
		// This works fine, but may be less accurate than linear fit in some cases, and definitely takes longer 
		Fitmrq<IndexedValue<double[]>> fitter = Fitmrq.createFitmrqMultiD(source, dest, params, new AffineFitUtil.AffineFitFunction(), 1e-16);
		if (fixedParams != null) {
			for (int i = 0; i < fixedParams.length; i++) {
				double value = fixedParams[i];
				if (!Double.isNaN(value)) {
					fitter.hold(i, value);
				}
			}
		}
		fitter.fit();
		params[2] = AngleUtil.signedNormalAngle(params[2]);
		return paramsToAffine(params);
	}

	public static void reduceParams(int maxParams, double[] fixedParams, int[] idxs, double[] defaults) {
		int cntFree = 0;
		for (int i = 0; i < fixedParams.length; i++) {
			if (Double.isNaN(fixedParams[i])) cntFree++;
		}
	
		for (int i = 0; i < idxs.length; i++) {
			if (cntFree > maxParams && Double.isNaN(fixedParams[idxs[i]])) {
				fixedParams[idxs[i]] = defaults[idxs[i]];
				cntFree--;
			}
		}
	}

	/**
	 * @param source {{x0,y0},{x1,y1}...}
	 * @param dest {{x0',y0'},{x1',y1'}...}
	 * @return {(1+s)cos(fi),(1+s)sin(fi),trX,trY}
	 */
	public static double[] fitHelmert2DAffine(double[][] source, double[][] dest) {
		double[][] Aik = new double[4][4];
		double[] Bi = new double[4];
	
		for (int i = 0; i < 4; i++) {
			for (int k = 0; k < 4; k++) {
				for (int j = 0; j < source.length; j++) {
					Aik[i][k] += funX(source[j], i) * funX(source[j], k);
					Aik[i][k] += funY(source[j], i) * funY(source[j], k);
				}
			}
		}
	
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < source.length; j++) {
				Bi[i] += dest[j][0] * funX(source[j], i);
				Bi[i] += dest[j][1] * funY(source[j], i);
			}
		}
		linearSolve(Aik, Bi);
		return Bi;
	}

	public static double vecLength(double[] vec) {
		return Math.sqrt(vecLengthSq(vec));
	}

	public static double vecLengthSq(double[] vec) {
		double ret = 0;
		for (int i = 0; i < vec.length; i++) {
			ret += vec[i] * vec[i];
		}
		return ret;
	}

	/**
	 * @param source {{x0,y0},{x1,y1}...}
	 * @param dest {{x0',y0'},{x1',y1'}...}
	 * @return {s,fi,trX,trY}
	 */
	public static double[] fitHelmert2D(double[][] source, double[][] dest) {
		double[] Ai = fitHelmert2DAffine(source, dest);
		return new double[]{Math.sqrt(Ai[0] * Ai[0] + Ai[1] * Ai[1]) - 1, Math.atan2(Ai[1], Ai[0]), Ai[2], Ai[3]};
	}

	/**
	 * @param source {{x0,y0},{x1,y1}...}
	 * @param dest {{x0',y0'},{x1',y1'}...}
	 * @return {m0, m1, m2, m3, m4, m5}
	 */
	public static double[] fitHelmert2DreturnAffine(double[][] source, double[][] dest) {
		double[] Ai = fitHelmert2DAffine(source, dest);
		return new double[]{Ai[0], -Ai[1], Ai[1], Ai[0], Ai[2], Ai[3]};
	}

	/**
	 * @param xy source {x,y}
	 * @param i function index {x, y, 1 (translation in x), 1 (translation in y)}
	 * @return value of the x coordinate of the i-th function
	 */
	private static double funX(double[] xy, int i) {
		switch (i) {
			case 0:
				return xy[0];
			case 1:
				return xy[1];
			case 2:
				return 1;
			default:
				return 0;
		}
	}

	/**
	 * @param xy source {x,y}
	 * @param i function index {x, y, 1 (translation in x), 1 (translation in y)}
	 * @return value of the y coordinate of the i-th function
	 */
	public static double funY(double[] xy, int i) {
		switch (i) {
			case 0:
				return xy[1];
			case 1:
				return -xy[0];
			case 3:
				return 1;
			default:
				return 0;
		}
	}

	/**
	 * @param A
	 * @param b in right-hand side of the equation Ax = b; stores output result 
	 */
	public static void linearSolve(double[][] A, double[] b) {
		int[] idx = new int[A.length];
		ludcmp(A, idx);
		lubksb(A, idx, b); // Result will be in b
	}
	
	public static double determinant3x3(double[][] A) {
		return //
			A[0][0]*(A[1][1]*A[2][2] - A[1][2]*A[2][1]) + //
			A[0][1]*(A[1][2]*A[2][0] - A[1][0]*A[2][2]) + //
			A[0][2]*(A[1][0]*A[2][1] - A[1][1]*A[2][0]);
	}
	
	public static double determinant(double[][] A) {
		int len = A.length;
		double[][] tmp = new double[len][len];
		ArrayUtil.arraycopy(A, 0, 0, tmp, 0, 0, len, len);
		double d = ludcmp(tmp, new int[len]);
		for (int i = 0; i < len; i++) {
			d *= tmp[i][i];
		}
		return d;
	}

	/**
	 * Given a matrix a[1..n][1..n], this routine replaces it by the LU decomposition of a rowwise permutation of
	 * itself. a and n are input. a is output, arranged as in equation (2.3.14) above; indx[1..n] is an output vector
	 * that records the row permutation effected by the partial pivoting; d is output as ±1 depending on whether the
	 * number of row interchanges was even or odd, respectively. This routine is used in combination with lubksb to
	 * solve linear equations or invert a matrix.
	 */
	//TODO: This is Numerical Recipes code - put it in a NR package and sort out licences
	private static double ludcmp(double[][] a, int[] indx) {
		final double TINY = 1e-20;
		int i, imax = 0, j, k;
		int n = a.length;
		double big, dum, temp, d;
		double sum = 0;
		double[] vv = new double[n]; // vv stores the implicit scaling of
										// each row.
		d = 1.0; // No row interchanges yet.
		for (i = 1; i <= n; i++) { // Loop over rows to get the implicit
									// scaling information.
			big = 0.0;
			for (j = 1; j <= n; j++) {
				if ((temp = Math.abs(a[i - 1][j - 1])) > big) big = temp;
			}
			if (big == 0.0) { throw new IllegalArgumentException("Singular matrix in routine ludcmp"); }
			// No nonzero largest element.
			vv[i - 1] = 1.0 / big; // Save the scaling.
		}
		for (j = 1; j <= n; j++) { // This is the loop over columns of Croutâ€™s
									// method.
			for (i = 1; i < j; i++) { // This is equation (2.3.12) except for
										// i = j.
				sum = a[i - 1][j - 1];
				for (k = 1; k < i; k++)
					sum -= a[i - 1][k - 1] * a[k - 1][j - 1];
				a[i - 1][j - 1] = sum;
			}
			big = 0.0; // Initialize for the search for largest pivot element.
			for (i = j; i <= n; i++) { // This is i = j of equation (2.3.12)
										// and i = j+1. . .N
				// of equation (2.3.13). 
				sum = a[i - 1][j - 1];
				for (k = 1; k < j; k++) {
					sum -= a[i - 1][k - 1] * a[k - 1][j - 1];
				}
				a[i - 1][j - 1] = sum;
				if ((dum = vv[i - 1] * Math.abs(sum)) >= big) { // Is the figure of
																// merit for the
																// pivot better than
																// the best so far?
					big = dum;
					imax = i;
				}
			}
			if (j != imax) { // Do we need to interchange rows?
				for (k = 1; k <= n; k++) { // Yes, do so...
					dum = a[imax - 1][k - 1];
					a[imax - 1][k - 1] = a[j - 1][k - 1];
					a[j - 1][k - 1] = dum;
				}
				d = -d; // ...and change the parity of d.
				vv[imax - 1] = vv[j - 1]; // Also interchange the scale factor.
			}
			indx[j - 1] = imax;
			if (a[j - 1][j - 1] == 0.0) a[j - 1][j - 1] = TINY;
			// If the pivot element is zero the matrix is singular (at least to
			// the precision of the
			// algorithm). For some applications on singular matrices, it is
			// desirable to substitute
			// TINY for zero.
			if (j != n) { // Now, finally, divide by the pivot element.
				dum = 1.0 / (a[j - 1][j - 1]);
				for (i = j + 1; i <= n; i++)
					a[i - 1][j - 1] *= dum;
			}
		} // Go back for the next column in the reduction.
		return d;
	}

	/**
	 * Solves the set of n linear equations A·X = B. Here a[1..n][1..n] is input, not as the matrix A but rather as its
	 * LU decomposition, determined by the routine ludcmp. indx[1..n] is input as the permutation vector returned by
	 * ludcmp. b[1..n] is input as the right-hand side vector B, and returns with the solution vector X. a, n, and index
	 * are not modified by this routine and can be left in place for successive calls with different right-hand sides b.
	 * This routine takes into account the possibility that b will begin with many zero elements, so it is effcient for
	 * use in matrix inversion.
	 */
	//TODO: This is Numerical Recipes code - put it in a NR package and sort out licences
	public static void lubksb(double[][] a, int[] indx, double b[]) {
		int i, ii = 0, ip, j;
		int n = b.length;
		double sum;
		for (i = 1; i <= n; i++) {
			/*
			 * When ii is set to a positive value, it will become the index of
			 * the first nonvanishing element of b. We now do the forward
			 * substitution, equation (2.3.6). The only new wrinkle is to
			 * unscramble the permutation as we go.
			 */
			ip = indx[i - 1];
			sum = b[ip - 1];
			b[ip - 1] = b[i - 1];
			if (ii != 0) {
				for (j = ii; j <= i - 1; j++) {
					sum -= a[i - 1][j - 1] * b[j - 1];
				}
			} else if (sum != 0) {
				ii = i; /*
						 * A nonzero element was encountered, so from now on we
						 * will have to do the sums in the loop above.
						 */
			}
			b[i - 1] = sum;
		}
		for (i = n; i >= 1; i--) { // Now we do the backsubstitution, equation
									// (2.3.7).
			sum = b[i - 1];
			for (j = i + 1; j <= n; j++)
				sum -= a[i - 1][j - 1] * b[j - 1];
			b[i - 1] = sum / a[i - 1][i - 1]; // Store a component of the solution vector
			// X.
		}// All done!
	}

	/**
	 * @param xy source {x,y}
	 * @param i function index {x, 0, y, 0, 1 (translation in x), 0}
	 * @return value of the x coordinate of the i-th function
	 */
	private static double funXGA(double[] xy, int i) {
		switch (i) {
			case 0:
				return xy[0];
			case 2:
				return xy[1];
			case 4:
				return 1;
			default:
				return 0;
		}
	}

	/**
	 * @param xy source {x,y}
	 * @param i function index {0, x, 0, y, 0, 1 (translation in y)}
	 * @return value of the y coordinate of the i-th function
	 */
	private static double funYGA(double[] xy, int i) {
		switch (i) {
			case 1:
				return xy[0];
			case 3:
				return xy[1];
			case 5:
				return 1;
			default:
				return 0;
		}
	}

	public static double[] fitGeneralAffine(double[][] source, double[][] dest) {
	
		double[][] Aik = new double[6][6];
		double[] Bi = new double[6];
	
		for (int i = 0; i < 6; i++) {
			for (int k = 0; k < 6; k++) {
				for (int j = 0; j < source.length; j++) {
					Aik[i][k] += funXGA(source[j], i) * funXGA(source[j], k);
					Aik[i][k] += funYGA(source[j], i) * funYGA(source[j], k);
				}
			}
		}
	
		for (int i = 0; i < 6; i++) {
			for (int j = 0; j < source.length; j++) {
				Bi[i] += dest[j][0] * funXGA(source[j], i);
				Bi[i] += dest[j][1] * funYGA(source[j], i);
			}
		}
		linearSolve(Aik, Bi);
		return Bi;
	}
}
