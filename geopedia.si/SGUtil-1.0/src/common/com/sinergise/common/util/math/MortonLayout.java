package com.sinergise.common.util.math;

import com.sinergise.common.util.geom.PointI;

/**
 * Morton layout is an ordering scheme for 2D arrays that fills the upper-left corner of 
 * the array first, like this: 
 * 
 * <pre>
 *   0   1 |  4   5
 *   2   3 |  6   7
 * --------+--------
 *   8   9 | 12  13
 *  10  11 | 14 ...
 * </pre>
 * 
 * <p>This can be represented in binary by alternating bits from the x and the y ordinates: 
 * </p>
 * <code>
 * 	x = ...x<sup>2</sup>x<sup>1</sup>x<sup>0</sup><sub>b</sub> <br/> 
 * 	y = ...y<sup>2</sup>y<sup>1</sup>y<sup>0</sup><sub>b</sub> <br/>
 * 
 * idx<sub>m</sub> = ...y<sup>2</sup>x<sup>2</sup>y<sup>1</sup>x<sup>1</sup>y<sup>0</sup>x<sup>0</sup><sub>b</sub>
 * </code>,
 * <p>e.g.:</p>
 * <code>
 *  x = 2 = <b>10</b><sub>b</sub><br/>
 *  y = 3 = 11<sub>b</sub><br/>
 *  idx = 1<b>1</b>1<b>0</b><sub>b</sub> = 14
 * </code>
 * 
 * @author Miha
 */
public class MortonLayout {
	private static final long[] NORM_TO_INTR_LOOKUP = new long[]{0,1,4,5,16,17,20,21,64,65,68,69,80,81,84,85};
	private static final int[] INTR_TO_NORM_LOOKUP = new int[]{0,1,6,7,2,3,-1,-1,12,13,8,9,14,15,10,11,4,5};
		
	/**
	 * @param idx index in the one-dimensional morton ordering
	 * @return x ordinate of the element in morton layout (0 -> 0, 1 -> 1, 2 -> 0, 3 -> 1, 4 -> 2 etc.)
	 */
	public static final int mortonX(final long idx) {
		return interleavedToNormal(mortonXinterleaved(idx));
	}
	
	protected static final long mortonXinterleaved(final long idx) {
		return idx & 0x5555555555555555L;
	}

	/**
	 * @param idx index in the one-dimensional morton ordering
	 * @return y ordinate of the element in morton layout (0 -> 0, 1 -> 0, 2 -> 1, 3 -> 1, 4 -> 0 etc.)
	 */
	public static final int mortonY(final long idx) {
		return interleavedToNormal(mortonYinterleaved(idx));
	}
	
	protected static int interleavedToNormal(final long a) {
		int ret = 0;
		for (int i = 0; i < 16; i+=4) {
			ret |= INTR_TO_NORM_LOOKUP[(int)(((a >> 2*i) & 0xFFL) % 18L)] << i; 
		}
		return ret;
	}
	
	protected static long normalToInterleaved(final int a) {
		long ret = 0;
		for (int i = 0; i < 16; i+=4) {
			ret |= NORM_TO_INTR_LOOKUP[(a >> i) & 0xF] << (2*i);
		}
		return ret;
	}
	
	public static void main(String[] args) {
		for (int i = 0; i < 64; i++) {
			for (int j = 0; j < 64; j++) {
				long idx = mortonIdx(i, j);
				System.out.println(i + " " + j + " " + idx +" " + Long.toBinaryString(idx) + " "+ mortonX(idx) + " " + mortonY(idx));
				if (mortonX(idx) != i || mortonY(idx) != j) {
					System.out.println(">>>>>>>>>>>>>>>>>>>>> ERROR <<<<<<<<<<<<<<<<<<<<<");
				}
			}
		}
	}

	protected static long mortonYinterleaved(final long idx) {
		return (idx >>> 1) & 0x5555555555555555L;
	}

	
	protected static long incrementBitInterleaved(final long interleavedA) {
		return ((interleavedA | 0xAAAAAAAAAAAAAAAAL) + 1L) & 0x5555555555555555L;
	}

	protected static long decrementBitInterleaved(final long interleavedA) {
		return (interleavedA - 1L) & 0x5555555555555555L;
	}

	public static long mortonIdx(int idxX, int idxY) {
		return normalToInterleaved(idxX) | (normalToInterleaved(idxY) << 1);
	}
	
	public static PointI xyFromMortonIdx(long mortonIdx) {
		return new PointI(mortonX(mortonIdx), mortonY(mortonIdx));
	}
}
