package com.sinergise.geopedia.geometry.height;

public class CubicSplineInterpolator
{
	public CubicSplineInterpolator()
	{
		// ...
	}

	int n;
	double[] valPos, valVal;
	
	double[] yd0, yd1, yd2, yd3;

	/**
	 * Computes cubic spline interpolation coefficients for interpolation with
	 * continuous second derivatives <br>
	 * <br>
	 * <b>Notes:</b> <br>
	 * <br>
	 * The computed cubic spline coefficients are as follows:<br>
	 * yd[i][0] = y(x[i]) (the value of y at x = x[i])<br>
	 * yd[i][1] = y'(x[i]) (the 1st derivative of y at x = x[i])<br>
	 * yd[i][2] = y''(x[i]) (the 2nd derivative of y at x = x[i])<br>
	 * yd[i][3] = y'''(x[i]) (the 3rd derivative of y at x = x[i])<br>
	 * <br>
	 * To evaluate y(x) for x between x[i] and x[i+1] and h = x-x[i], use the
	 * computed coefficients as follows:<br>
	 * <br>
	 * y(x) = yd[i][0]+h*(yd[i][1]+h*(yd[i][2]/2.0+h*yd[i][3]/6.0))
	 * 
	 * @param xy array of points (double[2] { x, y }); abscissae must be
	 *           monotonically increasing
	 * @return array[xy.length][4] of cubic interpolation coefficients
	 * 
	 * @author Dave Hale, Colorado School of Mines (original C code)
	 * @author Mitja Slenc (port to Java)
	 */
	void csplin()
	{
		int n = valPos.length;

		if (yd0 == null || yd0.length != n) {
			yd0 = new double[n];
			yd1 = new double[n];
			yd2 = new double[n];
			yd3 = new double[n];
		}

		/* if n=1, then use constant interpolation */
		if (n == 1) {
			yd0[0] = valVal[0];
			yd1[0] = 0.0;
			yd2[0] = 0.0;
			yd3[0] = 0.0;
			return;

			/* else, if n=2, then use linear interpolation */
		} else if (n == 2) {
			yd0[0] = valVal[0];
			yd0[1] = valVal[1];
			yd1[0] = yd1[1] = (valVal[1] - valVal[0]) / (valPos[1] - valPos[0]);
			yd2[0] = yd2[1] = 0.0;
			yd3[0] = yd3[1] = 0.0;
			return;
		}

		/* set left end derivative via shape-preserving 3-point formula */
		double h1 = valPos[1] - valPos[0];
		double h2 = valPos[2] - valPos[1];
		double hsum = h1 + h2;
		double del1 = (valVal[1] - valVal[0]) / h1;
		double del2 = (valVal[2] - valVal[1]) / h2;
		double w1 = (h1 + hsum) / hsum;
		double w2 = -h1 / hsum;
		double sleft = w1 * del1 + w2 * del2;
		if (sleft * del1 <= 0.0)
			sleft = 0.0;
		else if ((del1 < 0) != (del2 < 0)) {
			double dmax = 3.0 * del1;
			if (((sleft <= 0.0D) ? 0.0D - sleft : sleft) > ((dmax <= 0.0D) ? 0.0D - dmax : dmax))
				sleft = dmax;
		}

		/* set right end derivative via shape-preserving 3-point formula */
		h1 = valPos[n - 2] - valPos[n - 3];
		h2 = valPos[n - 1] - valPos[n - 2];
		hsum = h1 + h2;
		del1 = (valVal[n - 2] - valVal[n - 3]) / h1;
		del2 = (valVal[n - 1] - valVal[n - 2]) / h2;
		w1 = -h2 / hsum;
		w2 = (h2 + hsum) / hsum;
		double sright = w1 * del1 + w2 * del2;
		
		if (sright * del2 <= 0.0) {
			sright = 0.0;
		} else 
		if ((del1 < 0) != (del2 < 0)) {
			double dmax = 3.0 * del2;
			if (((sright <= 0.0D) ? 0.0D - sright : sright) > ((dmax <= 0.0D) ? 0.0D - dmax : dmax))
				sright = dmax;
		}

		/* compute tridiagonal system coefficients and right-hand-side */
		yd0[0] = 1.0;
		yd2[0] = 2.0 * sleft;
		for (int i = 1; i < n - 1; i++) {
			h1 = valPos[i] - valPos[i - 1];
			h2 = valPos[i + 1] - valPos[i];
			del1 = (valVal[i] - valVal[i - 1]) / h1;
			del2 = (valVal[i + 1] - valVal[i]) / h2;
			double alpha = h2 / (h1 + h2);
			yd0[i] = alpha;
			yd2[i] = 3.0 * (alpha * del1 + (1.0 - alpha) * del2);
		}
		yd0[n - 1] = 0.0;
		yd2[n - 1] = 2.0 * sright;

		/* solve tridiagonal system for slopes */
		double t = 2.0;
		yd1[0] = yd2[0] / t;
		for (int i = 1; i < n; i++) {
			yd3[i] = (1.0 - yd0[i - 1]) / t;
			t = 2.0 - yd0[i] * yd3[i];
			yd1[i] = (yd2[i] - yd0[i] * yd1[i - 1]) / t;
		}
		for (int i = n - 2; i >= 0; i--)
			yd1[i] -= yd3[i + 1] * yd1[i + 1];

		/* copy ordinates into output array */
		for (int i = 0; i < n; i++)
			yd0[i] = valVal[i];

		/* compute 2nd and 3rd derivatives of cubic polynomials */
		for (int i = 0; i < n - 1; i++) {
			h2 = valPos[i + 1] - valPos[i];
			del2 = (valVal[i + 1] - valVal[i]) / h2;
			double divdf3 = yd1[i] + yd1[i + 1] - 2.0 * del2;
			yd2[i] = 2.0 * (del2 - yd1[i] - divdf3) / h2;
			yd3[i] = (divdf3 / h2) * (6.0 / h2);
		}
		yd2[n - 1] = yd2[n - 2] + (valPos[n - 1] - valPos[n - 2]) * yd3[n - 2];
		yd3[n - 1] = yd3[n - 2];
	}

	public void setData(double[] pos, double[] val)
	{
		this.valPos = pos;
		this.valVal = val;
		csplin();
	}

	public double eval(double x)
	{
		int np = valPos.length;
		double prev = valPos[0];
		for (int pos = 1; pos < np; pos++) {
			double zis = valPos[pos];
			if (x >= prev && x <= zis) {
				double h = x - prev;
				
				int p1 = pos - 1;
				return yd0[p1] + h * (yd1[p1] + h * (yd2[p1] / 2.0 + h * yd3[p1] / 6.0));
			}
			prev = zis;
		}

		return Double.NaN;
	}
	
	public double derivative1(double x)
	{
		int np = valPos.length;
		double prev = valPos[0];
		for (int pos = 1; pos < np; pos++) {
			double zis = valPos[pos];
			if (x >= prev && x <= zis) {
				double h = x - prev;

				int p1 = pos - 1;
				return  yd1[p1] + 
						h * yd2[p1] + 
						h * h * yd3[p1] / 2.0;
			} else {
				prev = zis;
			}
		}

		return Double.NaN;
	}
}
