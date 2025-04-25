package com.sinergise.common.util.math;

import java.util.Arrays;

import com.sinergise.common.util.math.fit.AffineFitUtil;

public class MatrixUtil {
    public static int numCols(double[][] matrix) { return matrix[0].length; }

    public static int numRows(double[][] matrix) { return matrix.length; }

    public static double[] flatten(double[][] yy) {
        int r=numRows(yy);
        int c=numCols(yy);
        double[] ret=new double[r*c];
        for (int i = 0; i < r; i++) {
        	System.arraycopy(yy[i], 0, ret, c*i, c);
        }
        return ret;
    }
    
    public static double[][] construct(int nRows, int nCols) {
        return new double[nRows][nCols];
    }
    
    public static double[] constructAndFillVector(int len, double fill) {
        double[] ret=new double[len];
        Arrays.fill(ret, fill);
        return ret;
    }
    
    public static double determinant(double[][] matrix) {
    	return AffineFitUtil.determinant(matrix);
    }

	public static void dot(double[][] mtrx, double[] vec, double[] tgt) {
		for (int row = tgt.length-1; row >=0; row--) {
			double sum=0;
			for (int col = vec.length-1; col >= 0; col--) {
				sum += mtrx[row][col]*vec[col];
			}
			tgt[row] = sum;
		}
	}
	
	public static void dot(double[][] a, double[][] b, double[][] ret) {
		for (int row = 0; row < a.length; row++) {
			for (int col = 0; col < b[0].length; col++) {
				double sum = 0;
				for (int i = 0; i < b.length; i++) {
					sum += a[row][i] * b[i][col];
				}
				ret[row][col] = sum;
			}
		}
	}

	public static void rotateTaitBryan(double omega, double phi, double kappa, double[][] ret) {
		rotate4(kappa, 0, 0, 1, ret);
		rotate4(phi, 0, 1, 0, ret);
		rotate4(omega, 1, 0, 0, ret);
	}

	/**
	 * 
	 * @param ang
	 * @param x normalized axis vector
	 * @param y
	 * @param z
	 * @param ret
	 */
	public static void rotate4(double ang, double x, double y, double z, double[][] ret) {
		double c = Math.cos(ang);
		double s = Math.sin(ang);
		double c1 = 1-c;
		
		double[][] tmp = new double[4][4];
		MatrixUtil.dot(new double[][] {
			{x*x*c1 + c,   y*x*c1 - z*s, z*x*c1 + y*s, 0},
			{x*y*c1 + z*s, y*y*c1 + c,   z*y*c1 - x*s, 0},
			{x*z*c1 - y*s, y*z*c1 + x*s, z*z*c1 + c,   0},
			{0,            0,            0,            1}
		}, ret, tmp);
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				ret[i][j] = tmp[i][j];
			}
		}
	}

	public static double[][] createIdentity(int n) {
		double[][] ret = construct(n, n);
		for (int i = n-1; i >= 0; i--) {
			ret[i][i] = 1;
		}
		return ret;
	}

	public static void transposeInPlace(double[][] m, int n) {
		for (int row = 1; row < n; row++) {
			for (int col = 0; col < row; col++) {
				double tmp = m[row][col];
				m[row][col] = m[col][row];
				m[col][row] = tmp;
			}
		}
	}

	public static void multiply(double[][] m, double d) {
		for (double[] row : m) {
			multiply(row, d);
		}
	}

	public static void multiply(double[] vector, double d) {
		for (int j = vector.length-1; j >=0; j--) {
			vector[j] *= d;
		}
	}

	public static double multiply(double[] a, double[] b) {
		double sum = 0;
		for (int i = a.length-1; i >= 0; i--) {
			sum += a[i] * b[i];
		}
		return sum;
	}
}
