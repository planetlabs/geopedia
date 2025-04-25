/*
 *
 */
package com.sinergise.common.util.math.fit;

import com.sinergise.common.util.ArrayUtil;
import com.sinergise.common.util.math.MatrixUtil;

public class GaussJ {
    public static void gaussj(double[][] a, double[][] b)
    {
        int i,icol=0,irow=0,j,k,l,ll,n=MatrixUtil.numRows(a),m=MatrixUtil.numCols(b);
        double big,dum,pivinv;
        int[] indxc=new int[n];
        int[] indxr=new int[n];
        int[] ipiv=new int[n];
        for (j=0;j<n;j++) ipiv[j]=0;
        for (i=0;i<n;i++) {
            big=0.0;
            for (j=0;j<n;j++)
                if (ipiv[j] != 1)
                    for (k=0;k<n;k++) {
                        if (ipiv[k] == 0) {
                            if (Math.abs(a[j][k]) >= big) {
                                big=Math.abs(a[j][k]);
                                irow=j;
                                icol=k;
                            }
                        }
                    }
            ++(ipiv[icol]);
            if (irow != icol) {
                for (l=0;l<n;l++) ArrayUtil.swap(a,irow,l,a,icol,l);
                for (l=0;l<m;l++) ArrayUtil.swap(b,irow,l,b,icol,l);
            }
            indxr[i]=irow;
            indxc[i]=icol;
            if (a[icol][icol] == 0.0) throw new ArithmeticException("gaussj: Singular Matrix");
            pivinv=1.0/a[icol][icol];
            a[icol][icol]=1.0;
            for (l=0;l<n;l++) a[icol][l] *= pivinv;
            for (l=0;l<m;l++) b[icol][l] *= pivinv;
            for (ll=0;ll<n;ll++)
                if (ll != icol) {
                    dum=a[ll][icol];
                    a[ll][icol]=0.0;
                    for (l=0;l<n;l++) a[ll][l] -= a[icol][l]*dum;
                    for (l=0;l<m;l++) b[ll][l] -= b[icol][l]*dum;
                }
        }
        for (l=n-1;l>=0;l--) {
            if (indxr[l] != indxc[l]) {
                for (k=0;k<n;k++) ArrayUtil.swap(a,k,indxr[l],a,k,indxc[l]);
            }
        }
    }

    void gaussj(double[][] a)
    {
        double[][] b = MatrixUtil.construct(MatrixUtil.numRows(a), 0);
        gaussj(a,b);
    }
}
