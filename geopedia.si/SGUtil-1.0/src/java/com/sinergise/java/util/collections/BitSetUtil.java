package com.sinergise.java.util.collections;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import com.sinergise.common.util.collections.BitSet;
import com.sinergise.common.util.collections.BitSet2D;
import com.sinergise.common.util.geom.EnvelopeI;

public class BitSetUtil {
	public static final void writeBitSet2D(BitSet2D bSet, DataOutput dOut) throws IOException {
		dOut.writeInt(bSet.getWidth());
		dOut.writeInt(bSet.getHeight());
		EnvelopeI p0 = bSet.getDataEnvelope();
		dOut.writeInt(p0.minY());
		dOut.writeInt(p0.minX());
		dOut.writeInt(p0.getWidth());
		writeBitSet(bSet.asBits(), dOut);
	}
	
	public static final BitSet2D readBitSet2D(DataInput dIn) throws IOException {
		final int w = dIn.readInt();
		final int h = dIn.readInt();
		final int row0 = dIn.readInt();
		final int col0 = dIn.readInt();
		final int wData = dIn.readInt();
		return new BitSet2D(readBitSet(dIn), w, h, row0, col0, wData);
	}

	public static final void writeBitSet(BitSet bSet, DataOutput dOut) throws IOException {
		dOut.writeLong(bSet.size());

		final int[] bsData = bSet.data;
		final int bsArrSize = bsData.length;
		for (int i = 0; i < bsArrSize; i++) {
			dOut.writeInt(bsData[i]);
		}
	}

	public static final BitSet readBitSet(DataInput dIn) throws IOException {
		final long sizeBits = dIn.readLong();

		final BitSet ret = new BitSet(sizeBits);
		final int bsArrSize = ret.data.length;
		for (int i = 0; i < bsArrSize; i++) {
			ret.data[i] = dIn.readInt();
		}
		return ret;
	}
}