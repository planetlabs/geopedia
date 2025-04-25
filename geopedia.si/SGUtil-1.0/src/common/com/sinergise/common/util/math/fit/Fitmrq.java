/*
 *
 */
package com.sinergise.common.util.math.fit;

import com.sinergise.common.util.ArrayUtil;
import com.sinergise.common.util.math.MatrixUtil;

//TODO: This is Numerical Recipes code - put it in a NR package and sort out licences
public class Fitmrq<X> {
    public static <O> Fitmrq<IndexedValue<O>> createFitmrqMultiD(O[] xx, double[][] yy, double[][] ssig, double[] aa, FitFunctionMultiD<O> funks, double tolerance) {
        return new Fitmrq<IndexedValue<O>>(
                IndexedValue.makeIndexed(xx,MatrixUtil.numCols(yy)), MatrixUtil.flatten(yy), MatrixUtil.flatten(ssig), aa, new FitFunctionMulti2Single<O>(funks),tolerance);
    }
    public static <O> Fitmrq<IndexedValue<O>> createFitmrqMultiD(O[] xx, double[][] yy, double[] aa, FitFunctionMultiD<O> funks, double tolerance) {
        return new Fitmrq<IndexedValue<O>>(
                IndexedValue.makeIndexed(xx,MatrixUtil.numCols(yy)), MatrixUtil.flatten(yy), aa, new FitFunctionMulti2Single<O>(funks),tolerance);
    }
    


    public static final double DEFAULT_TOLERANCE=1e-3;
    
    public static final int NDONE=4;
    public static final int ITMAX=2000;
        
    private int ma; 
    private int ndat;
    private int mfit;
    private X[] x;
    private double[] y;
    private double[] sig;
	private double tol;
    boolean[] ia;
    double[] a;
    double[][] covar;
    double[][] alpha;
    double chisq;
    FitFunction<X> funcs;

    public Fitmrq(X[] xx, double[] yy, double[] ssig, double[] aa, FitFunction<X> funks) {
        this(xx, yy, ssig, aa, funks, DEFAULT_TOLERANCE);
    }

    public Fitmrq(X[] xx, double[] yy, double[] aa, FitFunction<X> funks, double tolerance) {
        this(xx, yy, MatrixUtil.constructAndFillVector(yy.length, 1), aa, funks, tolerance);
    }
    
    public Fitmrq(X[] xx, double[] yy, double[] ssig, double[] aa, FitFunction<X> funks, double tolerance) {
        this.ndat=xx.length;
        this.ma=aa.length;
        this.x=xx;
        this.y=yy;
        this.sig=ssig;
        this.tol=tolerance;
        this.funcs=funks;
        this.ia=new boolean[ma];
        ArrayUtil.fill(ia, true);

        this.alpha=new double[ma][ma];
        this.a=aa;
        this.covar=new double[ma][ma];
    }
    public final void hold(final int i, final double val) {
    	ia[i] = false;
    	a[i] = val;
    }
    public void free(final int i) {
    	ia[i] = true;
    }

    public void fit() {
            int j,k,l,iter,done=0;
            double alamda=0.001;
            double ochisq=0;
            double[] atry=new double[ma];
            double[] beta=new double[ma];
            double[] da=new double[ma];
            mfit=0;
            for (j=0;j<ma;j++) if (ia[j]) mfit++;
            double[][] oneda=new double[mfit][1];
            double[][] temp=new double[mfit][mfit];
            mrqcof(a,alpha,beta);
            for (j=0;j<ma;j++) atry[j]=a[j];
            ochisq=chisq;
            for (iter=0;iter<ITMAX;iter++) {
                if (done==NDONE) alamda=0.;
                for (j=0;j<mfit;j++) {
                    for (k=0;k<mfit;k++) covar[j][k]=alpha[j][k];
                    covar[j][j]=alpha[j][j]*(1.0+alamda);
                    for (k=0;k<mfit;k++) temp[j][k]=covar[j][k];
                    oneda[j][0]=beta[j];
                }
                GaussJ.gaussj(temp,oneda);
                for (j=0;j<mfit;j++) {
                    for (k=0;k<mfit;k++) covar[j][k]=temp[j][k];
                    da[j]=oneda[j][0];
                }
                if (done==NDONE) {
                    covsrt(covar);
                    covsrt(alpha);
                    return;
                }
                for (j=0,l=0;l<ma;l++)
                    if (ia[l]) atry[l]=a[l]+da[j++];
                mrqcof(atry,covar,da);
                if (Math.abs(chisq-ochisq) < Math.max(tol,tol*chisq)) done++;
                if (chisq < ochisq) {
                    alamda *= 0.1;
                    ochisq=chisq;
                    for (j=0;j<mfit;j++) {
                        for (k=0;k<mfit;k++) alpha[j][k]=covar[j][k];
                            beta[j]=da[j];
                    }
                    for (l=0;l<ma;l++) a[l]=atry[l];
                } else {
                    alamda *= 10.0;
                    chisq=ochisq;
                }
            }
            throw new RuntimeException("Fitmrq too many iterations");
        }


        public void mrqcof(double[] inA, double[][] inAlpha, double[] beta) {
            int i,j,k,l,m;
            double ymod,wt,sig2i,dy;
            double[] dyda=new double[ma];
            for (j=0;j<mfit;j++) {
                for (k=0;k<=j;k++) inAlpha[j][k]=0.0;
                beta[j]=0.;
            }
            chisq=0.;
            for (i=0;i<ndat;i++) {
                ymod=FitUtils.compute(funcs, inA, x[i],dyda);
                sig2i=1.0/(sig[i]*sig[i]);
                dy=y[i]-ymod;
                for (j=0,l=0;l<ma;l++) {
                    if (ia[l]) {
                        wt=dyda[l]*sig2i;
                        for (k=0,m=0;m<l+1;m++)
                            if (ia[m]) inAlpha[j][k++] += wt*dyda[m];
                        beta[j++] += dy*wt;
                    }
                }
                chisq += dy*dy*sig2i;
            }
            for (j=1;j<mfit;j++)
                for (k=0;k<j;k++) inAlpha[k][j]=inAlpha[j][k];
        }
        
        private void covsrt(double[][] covarMtrx) {
            int i,j,k;
            for (i=mfit;i<ma;i++)
                for (j=0;j<i+1;j++) covarMtrx[i][j]=covarMtrx[j][i]=0.0;
            k=mfit-1;
            for (j=ma-1;j>=0;j--) {
                if (ia[j]) {
                    for (i=0;i<ma;i++) ArrayUtil.swap(covarMtrx,i,k,covarMtrx,i,j);
                    for (i=0;i<ma;i++) ArrayUtil.swap(covarMtrx,k,i,covarMtrx,j,i);
                    k--;
                }
            }
        }

}
