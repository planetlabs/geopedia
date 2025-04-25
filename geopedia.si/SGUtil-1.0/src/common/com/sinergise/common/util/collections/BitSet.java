package com.sinergise.common.util.collections;

import com.sinergise.common.util.math.MathUtil;

public final class BitSet implements Comparable<BitSet>, Cloneable {
	public final int[] data;
	public final long sizeBits;

	public BitSet(final long sizeBits) {
		if (sizeBits < 0) { throw new IllegalArgumentException("Negative sizes not supported"); }

		final long sizeInts = ((sizeBits + 31L) >>> 5);
		if (sizeInts > Integer.MAX_VALUE) { throw new IllegalArgumentException("Size too large; " + sizeBits
			+ " larger than maximum (" + (32L * Integer.MAX_VALUE) + ")"); }

		this.sizeBits = sizeBits;
		data = new int[(int)sizeInts];
	}

	public final void clearAll() {
		final int[] myData = this.data; // speed hack (around 3% diff)
		for (int a = myData.length - 1; a >= 0; a--) {
			myData[a] = 0;
		}
	}

	public final void setAll() {
		final int[] myData = this.data; // speed hack
		for (int a = myData.length - 1; a >= 0; a--) {
			myData[a] = 0xffffffff;
		}
	}

	public final void set(final long idx) {
		assert (idx >>> 5) >= 0 && (idx >>> 5) < data.length : "Bit index " + idx + " too large (len=" + sizeBits + ")";
		data[(int)(idx >>> 5)] |= 1 << (idx & 31);
	}

	public final void flip(final long idx) {
		data[(int)(idx >>> 5)] ^= 1 << (idx & 31);
	}

	public final void clear(final long idx) {
		data[(int)(idx >>> 5)] &= ~(1 << (idx & 31));
	}

	public final boolean isSet(final long idx) {
		return 0 != (data[(int)(idx >>> 5)] & (1 << (idx & 31)));
	}

	public final long size() {
		return sizeBits;
	}

	@Override
	public final int compareTo(final BitSet o) {
		final int[] myData = this.data;
		final int[] data2 = o.data;
		final int l = myData.length;
		for (int a = 0; a < l; a++) {
			if (myData[a] != data2[a]) { return data2[a] - myData[a]; }
		}
		return 0;
	}

	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof BitSet)) { return false; }
		return equals((BitSet)obj);
	}

	public final boolean equals(final BitSet o) {
		return compareTo(o) == 0;
	}

	// @Override Should not have this annotation because GWT does not have
	// clone() implemented
	@SuppressWarnings("all")
	public Object clone() {
		final BitSet result = new BitSet(sizeBits);
		System.arraycopy(data, 0, result.data, 0, data.length);
		return result;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		final int[] myData = this.data; // speed hack
		final int l = myData.length;
		int result = 1;
		result = prime * result + (int)sizeBits;
		for (int a = 0; a < l; a++) {
			result = prime * result + myData[a];
		}
		return result;
	}

	public long count() {
		long sum = 0;
		final int[] myData = this.data; // speed hack
		for (final int element : myData) {
			sum += MathUtil.bitCount(element);
		}
		int numLast = (int)(sizeBits % 32);
		if (numLast == 0) return sum;

		sum -= MathUtil.bitCount(myData[myData.length - 1]);
		for (int i = 0; i < numLast; i++) {
			if (isSet(sizeBits - i - 1)) sum++;
		}
		return sum;
	}

	public long firstIndexOf(boolean set) {
		final int[] myData = this.data;
		final int byteLen = myData.length;
		final int comp = set ? 0 : 0xFFFFFFFF;
		for (int i = 0; i < byteLen; i++) {
			if (myData[i] == comp) continue;
			for (long j = (long)i << 5; j < (long)(i + 1) << 5; j++) {
				if (isSet(j) == set) { 
					return j < sizeBits ? j : -1;
				}
			}
		}
		return -1L;
	}

	public long lastIndexOf(boolean set) {

		// check last byte first if not complete
		for (long j = 1; j < sizeBits % 32 + 1; j++) {
			if (set == isSet(sizeBits - j)) {
				return sizeBits - j;
			}
		}

		final int[] myData = this.data;
		final int comp = set ? 0 : 0xFFFFFFFF;
		final int start = sizeBits % 32 == 0 ? myData.length - 1 : myData.length - 2;

		for (int i = start; i >= 0; i--) {
			if (myData[i] == comp) {
				continue;
			}
			for (long j = (long)((i + 1) << 5) - 1; j >= (i << 5); j--) {
				if (set == isSet(j)) { 
					return j;
				}
			}
		}
		return -1L;
	}

}
