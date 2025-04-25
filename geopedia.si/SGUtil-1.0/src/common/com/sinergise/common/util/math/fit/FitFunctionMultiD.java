/*
 *
 */
package com.sinergise.common.util.math.fit;

public interface FitFunctionMultiD<TX> {
    int length();
    void set(TX x, double[] a);
    void getY(double[] yRet);
    void getDerivs(double[][] dyda);
}
