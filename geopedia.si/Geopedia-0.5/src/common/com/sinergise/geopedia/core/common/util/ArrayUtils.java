package com.sinergise.geopedia.core.common.util;

import java.util.ArrayList;
import java.util.Arrays;

public class ArrayUtils {
    /**
     * Reads a string of double values, separated by <code>|</code>.
     * Example: "1234.1234|134.42" returns {1234.1234,134.42}
     * 
     * @param sVal
     * @return the parsed double[], or <code>null</code> if string is empty
     */
    public static double[] readDoubleSeq(String sVal) {
        if (sVal.length()<1) return null;
        String[] st=sVal.split("\\|");
        double[] ret=new double[st.length];
        try {
            for (int i = 0; i < ret.length; i++) {
                ret[i]=Double.parseDouble(st[i]);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
        return ret;
    }

    public static int[] readIntSeq(String sVal) {
        if (sVal.length()<1) return null;
        String[] st=sVal.split("\\|");
        int[] ret=new int[st.length];
        try {
            for (int i = 0; i < ret.length; i++) {
                ret[i]=Integer.parseInt(st[i]);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
        return ret;
    }
    
    public static String writeDoubleSeq(double[] value) {
        StringBuffer sb=new StringBuffer(3*value.length);
        for (int i = 0; i < value.length; i++) {
            if (i>0) sb.append("|");
            sb.append(value[i]);
        }
        return sb.toString();
    }

    public static String writeIntSeq(int[] value) {
        StringBuffer sb=new StringBuffer(3*value.length);
        for (int i = 0; i < value.length; i++) {
            if (i>0) sb.append("|");
            sb.append(value[i]);
        }
        return sb.toString();
    }
    
    
    
    public static void arraycopy(Object[] src, int srcOff, Object[] target, int tOff, int length)
	{
		for (int i = 0; i < length; i++) {
			target[tOff + i] = src[srcOff + i];
		}
	}

	public static void arraycopy(double[] src, int srcOff, double[] target, int tOff, int length)
	{
		for (int i = 0; i < length; i++) {
			target[tOff + i] = src[srcOff + i];
		}
	}
	
	public static void arraycopy(int[] src, int srcOff, int[] target, int tOff, int length)
    {
		for (int i = 0; i < length; i++) {
			target[tOff + i] = src[srcOff + i];
		}
    }

	public static Object[] toArray(ArrayList<?> src, Object[] tgt) {
        for (int i = 0; i < tgt.length; i++) {
            tgt[i]=src.get(i);
        }
        return tgt;
    }

    public static boolean contains(int[] array, int val) {
        for (int i = 0; i < array.length; i++) {
            if (array[i]==val) return true;
        }
        return false;
    }

    public static int indexOf(double[] array, double value, double epsilon) {
        for (int i = 0; i < array.length; i++) {
            if (Math.abs(array[i]-value)<epsilon) {
                return i;
            }
        }
        return -1;
    }

    public static void sort(double[] vals) {
        Double[] objVals=new Double[vals.length];
        for (int i = 0; i < vals.length; i++) {
            objVals[i]=new Double(vals[i]);
        }
        Arrays.sort(objVals);
        for (int i = 0; i < vals.length; i++) {
            vals[i]=((Double)objVals[i]).doubleValue();
        }
    }

    public static boolean contains(Object[] array, Object value) {
        for (int i = 0; i < array.length; i++) {
            if (array[i]==null && value==null) return true;
            if (value==null) continue;
            if (value.equals(array[i])) return true;
        }
        return false;
    }
    
    public static Object[] reverse(Object[] arr) {
        int len=arr.length;
        for (int i = 0; i < arr.length/2; i++) {
            Object left=arr[i];
            int rightIdx=len-1-i;
            arr[i]=arr[rightIdx];
            arr[rightIdx]=left;
        }
        return arr;
    }

}
