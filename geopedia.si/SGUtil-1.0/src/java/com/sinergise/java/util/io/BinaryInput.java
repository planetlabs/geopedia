package com.sinergise.java.util.io;

import java.io.Closeable;
import java.io.DataInput;
import java.io.IOException;

public interface BinaryInput extends Closeable, DataInput {
	
	public static interface BinaryRandomAccessInput extends BinaryInput, HasRandomAccess {} 
	
	short readShortLE() throws IOException;
	
	char readCharLE() throws IOException;
	
	int readIntLE() throws IOException;
	
	long readLongLE() throws IOException;
	
	float readFloatLE() throws IOException;
	
	double readDoubleLE() throws IOException;
	
	String readStringLenUTF8() throws IOException;
	
	@Override
	@Deprecated
	String readLine() throws IOException;
	
	@Override
	@Deprecated
	String readUTF() throws IOException;

	abstract long readUnsignedIntLE() throws IOException;

	abstract int readUnsignedShortLE() throws IOException;

	abstract long readUnsignedInt() throws IOException;
	
	abstract void skipFully(int n) throws IOException;

	String readASCII(int len) throws IOException;
}
