package com.sinergise.common.util.math.fit;

public class IndexedValue<T> {
    @SuppressWarnings("unchecked")
    public static <A> IndexedValue<A>[] makeIndexed(A[] vals, int numCopies) {
        int len=vals.length;
        IndexedValue<A>[] ret=new IndexedValue[len*numCopies];
        for (int i = 0; i < len; i++) {
            for (int j = 0; j < numCopies; j++) {
                ret[i*numCopies+j]=new IndexedValue<A>(vals[i],j);
            }
        }
        return ret;
    }
    
    public final T val;
    public final int index;

    public IndexedValue(T val, int index) {
        this.val = val;
        this.index = index;
    }
}
