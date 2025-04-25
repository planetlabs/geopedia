/*
 *
 */
package com.sinergise.common.util.math.fit;


public class FitFunctionMulti2Single<TX> implements FitFunction<IndexedValue<TX>> {
    FitFunctionMultiD<TX> multiFun;
    TX lastSet;
    boolean gotVals;
    boolean gotDerivs;
    int curIdx;
    double[] curVals;
    double[][] curDerivs;
    
    public FitFunctionMulti2Single(FitFunctionMultiD<TX> multiFun) {
        this.multiFun=multiFun;
        curVals=new double[multiFun.length()];
    }
    
    @Override
	public void getDerivs(double[] dyda) {
        if (!gotDerivs) {
            if (curDerivs==null) {
                curDerivs=new double[dyda.length][multiFun.length()];
            }
            multiFun.getDerivs(curDerivs);
        }
        for (int i = 0; i < dyda.length; i++) {
            dyda[i]=curDerivs[i][curIdx];
        }
    }

    @Override
	public double getY() {
        if (!gotVals) {
            multiFun.getY(curVals);
        }
        return curVals[curIdx];
    }

    @Override
	public void set(IndexedValue<TX> x, double[] a) {
       curIdx=x.index;
       if (!x.val.equals(lastSet)) {
           gotVals=false;
           gotDerivs=false;
           lastSet=x.val;
           multiFun.set(x.val, a);
       }
    }
}
