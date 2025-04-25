package com.sinergise.java.util.io;

import java.io.Closeable;
import java.io.DataOutput;
import java.io.Flushable;
import java.io.IOException;

public interface BinaryOutput extends Closeable, Flushable, DataOutput {
	public static interface BinaryRandomAccessOutput extends BinaryOutput, HasRandomAccess {}
	
	void writeShortLE(short val) throws IOException;
	
	void writeCharLE(char val) throws IOException;
	
	void writeIntLE(int val) throws IOException;
	
	void writeLongLE(long val) throws IOException;
	
	void writeFloatLE(float val) throws IOException;
	
	void writeDoubleLE(double val) throws IOException;

	/**
	 * Arrays can be optimized by using NIO buffers 
	 */
	void writeDoubles(double[] vals, int off, int len, boolean bigEndian) throws IOException;
	
	void writeStringLenUTF8(String val) throws IOException;

	@Override
	@Deprecated
	void writeBytes(String s) throws IOException;
	
	@Override
	@Deprecated
	void writeUTF(String str) throws IOException;

	void writeASCII(String str) throws IOException;
}
