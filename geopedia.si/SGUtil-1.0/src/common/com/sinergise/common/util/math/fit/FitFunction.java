/*
 *
 */
package com.sinergise.common.util.math.fit;

public interface FitFunction<TX> {
    void set(TX x, double[] a);
    double getY();
    void getDerivs(double[] dyda);
}
