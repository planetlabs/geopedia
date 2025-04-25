package com.sinergise.java.raster.io;

import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferFloat;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferShort;
import java.awt.image.DataBufferUShort;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import com.sinergise.java.util.io.IOUtilJava;

public class DataBufferUtilJava {
	public static void writeDataBuffer(DataBuffer data, DataOutput out) throws IOException {
		int numBanks = data.getNumBanks();
		int size = data.getSize();
		int[] offs = data.getOffsets();

		out.writeInt(data.getDataType());
		out.writeInt(numBanks);
		out.writeInt(size);
		for (int i = 0; i < numBanks; i++) {
			int off = offs[i];
			if (data instanceof DataBufferByte) {
				IOUtilJava.write(out, ((DataBufferByte)data).getData(i), off, size);
				
			} else if (data instanceof DataBufferUShort) {
				IOUtilJava.write(out, ((DataBufferUShort)data).getData(i), off, size);
				
			} else if (data instanceof DataBufferShort) {
				IOUtilJava.write(out, ((DataBufferShort)data).getData(i), off, size);

			} else if (data instanceof DataBufferInt) {
				IOUtilJava.write(out, ((DataBufferInt)data).getData(i), off, size);

			} else if (data instanceof DataBufferFloat) {
				IOUtilJava.write(out, ((DataBufferFloat)data).getData(i), off, size);

//			} else if (data instanceof com.sun.media.jai.codecimpl.util.DataBufferDouble) {
//				IOUtilJava.write(out, ((com.sun.media.jai.codecimpl.util.DataBufferDouble)data).getData(i), off, size);
			}
		}
	}
	
	public static DataBuffer readDataBuffer(DataInput in) throws IOException {
		int dataType = in.readInt();
		int numBanks = in.readInt();
		int size = in.readInt();
		switch (dataType) {
			case DataBuffer.TYPE_BYTE: 
				return new DataBufferByte(IOUtilJava.readBytes2D(in, numBanks, size), size);
			case DataBuffer.TYPE_SHORT: 
				return new DataBufferShort(IOUtilJava.readShorts2D(in, numBanks, size), size);
			case DataBuffer.TYPE_USHORT: 
				return new DataBufferUShort(IOUtilJava.readShorts2D(in, numBanks, size), size);
			case DataBuffer.TYPE_INT: 
				return new DataBufferInt(IOUtilJava.readInts2D(in, numBanks, size), size);
			case DataBuffer.TYPE_FLOAT: 
				return new DataBufferFloat(IOUtilJava.readFloats2D(in, numBanks, size), size);
//			case DataBuffer.TYPE_DOUBLE: 
//				return new com.sun.media.jai.codecimpl.util.DataBufferDouble(IOUtilJava.readDoubles2D(in, numBanks, size), size);
		}
		throw new IllegalArgumentException("Unsupported DataBuffer type: "+dataType);
	}
}
