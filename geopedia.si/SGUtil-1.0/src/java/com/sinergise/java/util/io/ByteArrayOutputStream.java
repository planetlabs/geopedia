package com.sinergise.java.util.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import com.sinergise.java.util.io.BinaryOutput.BinaryRandomAccessOutput;

public class ByteArrayOutputStream extends OutputStream implements BinaryRandomAccessOutput {
	byte[] buff;
	int length = 0;
	int pos = 0;
	
	/**
	 * Creates a new BAOS with the default buffer size (32 bytes).
	 */
	public ByteArrayOutputStream() {
		this(32);
	}
	
	/**
	 * Creates a new BAOS with the specified buffer size (must be >= 0).
	 * 
	 * @param buffSize the initial buffer size
	 */
	public ByteArrayOutputStream(final int buffSize) {
		if (buffSize < 0) {
			throw new IllegalArgumentException();
		}
		
		buff = new byte[buffSize];
	}
	
	/**
	 * Creates a new BAOS with the specified buffer size (must be >= 0).
	 * 
	 * @param buffSize the initial buffer size
	 */
	public ByteArrayOutputStream(byte[] initialBuff) {
		buff = initialBuff;
	}
	
	/**
	 * Grows the internal buffer so it is at least minSize big.
	 * 
	 * @param minSize the new minimum size
	 */
	protected void grow(final int minSize) {
		if (minSize < 0) {
			throw new IllegalStateException("Array size exhausted");
		}
		int newSize = buff.length > Integer.MAX_VALUE /2 ? Integer.MAX_VALUE : 2 * buff.length;
		if (newSize < minSize) {
			newSize = minSize;
		}
		
		final byte[] newbuff = new byte[newSize];
		System.arraycopy(buff, 0, newbuff, 0, length);
		buff = newbuff;
	}
	
	@Override
	public void write(final byte[] b, final int off, final int len) {
		int writePos = prepareWrite(len);
		System.arraycopy(b, off, buff, writePos, len);
	}
	
	@Override
	public void write(final byte[] b) {
		write(b, 0, b.length);
	}
	
	@Override
	public void write(final int b) {
		int writePos = prepareWrite(1);
		buff[writePos] = (byte)b;
	}
	
	/**
	 * Writes a binary integer (4 bytes) in little-endian order.
	 * 
	 * @param val the value to write
	 */
	@Override
	public void writeIntLE(final int val) {
		int writePos = prepareWrite(4);
		BinaryUtilJava.putIntLE(buff, writePos, val);
	}

	/**
	 * @param howMuch
	 * @return position in buffer to write to
	 */
	private int prepareWrite(int howMuch) {
		final int oldPos = pos;
		final int newPos = oldPos + howMuch;
		if (newPos > buff.length) {
			grow(newPos);
		}
		pos = newPos;
		if (pos > length) {
			length = pos;
		}
		return oldPos;
	}
	
	/**
	 * Writes a binary float (4 bytes) in little-endian order.
	 * 
	 * @param val the value to write
	 */
	@Override
	public void writeFloatLE(final float fval) {
		int writePos = prepareWrite(4);
		BinaryUtilJava.putFloatLE(buff, writePos, fval);
	}
	
	/**
	 * Writes a binary short (2 bytes) in little-endian order.
	 * 
	 * @param val the value to write
	 */
	@Override
	public void writeShortLE(final short val) {
		int writePos = prepareWrite(2);
		BinaryUtilJava.putShortLE(buff, writePos, val);
	}
	
	/**
	 * Writes a binary char (2 bytes) in little-endian order.
	 * 
	 * @param val the value to write
	 */
	@Override
	public void writeCharLE(final char val) {
		int writePos = prepareWrite(2);
		BinaryUtilJava.putCharLE(buff, writePos, val);
	}
	
	/**
	 * Writes a binary double (8 bytes) in little-endian order.
	 * 
	 * @param val the value to write
	 */
	@Override
	public void writeDoubleLE(final double value) {
		int writePos = prepareWrite(8);
		BinaryUtilJava.putDoubleLE(buff, writePos, value);
	}
	
	/**
	 * Writes a binary int (4 bytes) in big-endian order.
	 * 
	 * @param val the value to write
	 */
	@Override
	public void writeInt(final int val) {
		int writePos = prepareWrite(4);
		BinaryUtilJava.putInt(buff, writePos, val);
	}
	
	/**
	 * Writes a binary float (4 bytes) in big-endian order.
	 * 
	 * @param val the value to write
	 */
	@Override
	public void writeFloat(final float fval) {
		int writePos = prepareWrite(4);
		BinaryUtilJava.putFloat(buff, writePos, fval);
	}
	
	/**
	 * Writes a binary short (2 bytes) in big-endian order.
	 * 
	 * @param val the value to write
	 */
	@Override
	public void writeShort(final int val) {
		int writePos = prepareWrite(2);
		BinaryUtilJava.putShort(buff, writePos, val);
	}
	
	/**
	 * Writes a binary char (2 bytes) in big-endian order.
	 * 
	 * @param val the value to write
	 */
	@Override
	public void writeChar(final int val) {
		int writePos = prepareWrite(2);
		BinaryUtilJava.putChar(buff, writePos, (char)val);
	}
	
	/**
	 * Writes a binary double (8 bytes) in big-endian order.
	 * 
	 * @param val the value to write
	 */
	@Override
	public void writeDouble(final double value) {
		int writePos = prepareWrite(8);
		BinaryUtilJava.putDouble(buff, writePos, value);
	}
	
	/**
	 * Writes a long (8 bytes) in big-endian order.
	 * 
	 * @param val the value to write
	 */
	@Override
	public void writeLong(final long value) {
		int writePos = prepareWrite(8);
		BinaryUtilJava.putLong(buff, writePos, value);
	}
	
	/**
	 * Writes a long (8 bytes) in little-endian order.
	 * 
	 * @param val the value to write
	 */
	@Override
	public void writeLongLE(final long value) {
		int writePos = prepareWrite(8);
		BinaryUtilJava.putLongLE(buff, writePos, value);
	}
	
	/**
	 * Returns the number of bytes written to this BAOS so far.
	 * 
	 * @return
	 */
	public int size() {
		return length;
	}
	
	/**
	 * Returns the data written to this BAOS in a new byte[]. If you don't want a copy to be created, use getInternalBuffer() and size() to
	 * get to the data.
	 * 
	 * @return the data written to this BAOS
	 */
	public byte[] toByteArray() {
		final byte[] out = new byte[length];
		System.arraycopy(buff, 0, out, 0, length);
		return out;
	}
	
	/**
	 * Returns the reference to the internal buffer of this BAOS. Use it if you want to examine the bytes or whatever. Feel free to corrupt
	 * the buffer as you see fit, as it's your data anyway :)
	 * 
	 * @return
	 */
	public byte[] getInternalBuffer() {
		return buff;
	}
	
	/**
	 * Writes the data written to this BAOS to another output stream. Just a convenience method, about the same as
	 * 
	 * <pre>
	 * os.write(baos.getInternalBuffer(), 0, baos.size());
	 * </pre>
	 * 
	 * @param os the stream to write to
	 * @throws IOException if an I/O exception occurs
	 */
	public void writeTo(final OutputStream os) throws IOException {
		os.write(buff, 0, length);
	}
	
	/**
	 * Reads data from an input stream to this BAOS 
	 * 
	 * @param is the input stream to read from
	 * @param b 
	 * @throws IOException if an I/O exception occurs
	 */
	public void readFrom(final InputStream is, boolean closeAfterRead) throws IOException {
		try {
			byte[] interimBuffer = new byte[1024 * 32];
			int    read          = 0;
			do {
				read = is.read(interimBuffer);
				if (read != -1) {
					write(interimBuffer, 0, read);
				}
			} while (read == interimBuffer.length);
		} finally {
			if (closeAfterRead) is.close();
		}
	}
	
	/**
	 * Writes the data written to this BAOS to a RandomAccessFile. Just a convenience method, about the same as
	 * 
	 * <pre>
	 * raf.write(baos.getInternalBuffer(), 0, baos.size());
	 * </pre>
	 * 
	 * @param raf the file to write to
	 * @throws IOException if an I/O exception occurs
	 */
	public void writeTo(final RandomAccessFile raf) throws IOException {
		raf.write(buff, 0, length);
	}
	
	/**
	 * Sets size to 0, so new data can be put in this BAOS. Same as calling reset(false);
	 */
	public void reset() {
		length = 0;
		pos = 0;
	}
	
	/**
	 * Sets size to 0 and optionally shrinks the internal buffer. If the shrinkBuffer parameter is true, the internal buffer will be
	 * shrinked to 32 bytes, if it was previously larger (the data is discarded in any case).
	 * 
	 * @param shrinkBuffer if true, memory for a large buffer will be released
	 */
	public void reset(final boolean shrinkBuffer) {
		length = 0;
		pos = 0;
		if (shrinkBuffer) {
			if (buff.length > 32) {
				buff = new byte[32];
			}
		}
	}
	
	/**
	 * Returns this BAOS's contents in hexadecimal representation. Same as
	 * 
	 * <pre>
	 * toHexString(16, true, &quot; &quot;, &quot;\n&quot;);
	 * </pre>
	 * 
	 * @return
	 */
	public String toHexString() {
		return toHexString(16, true, " ", "\n");
	}
	
	/**
	 * Returns this BAOS's contents in hexadecimal representation. The parameters control the exact output.
	 * 
	 * @param bytesPerLine how many bytes to put in one line (must be at least 1)
	 * @param upperCase controls whether the numbers A-F are uppercase or lowercase
	 * @param byteSep the separator between bytes (often " ")
	 * @param lineSep the separator between lines (usually "\n")
	 * @return
	 */
	public String toHexString(final int bytesPerLine, final boolean upperCase, String byteSep, String lineSep) {
		if (bytesPerLine < 1) {
			throw new IllegalArgumentException();
		}
		if (byteSep == null) {
			byteSep = "";
		}
		if (lineSep == null) {
			lineSep = "";
		}
		
		final char letBase = (char)(upperCase ? 'A' - 10 : 'a' - 10);
		
		final byte[] data = buff;
		int curPos = 0;
		int lines = length / bytesPerLine;
		final int remain = length - lines * bytesPerLine;
		int lastLine = -1;
		if (remain > 0) {
			lastLine = lines++;
		}
		
		int last = bytesPerLine;
		final StringBuffer sb = new StringBuffer();
		for (int a = 0; a < lines; a++) {
			if (a == lastLine) {
				last = remain;
			}
			
			for (int b = 0; b < last; b++) {
				if (b > 0) {
					sb.append(byteSep);
				}
				
				final int val = data[curPos++] & 255;
				final int high = val >>> 4;
				final int low = val & 15;
				
				sb.append((char)(high > 9 ? letBase + high : '0' + high));
				sb.append((char)(low > 9 ? letBase + low : '0' + low));
			}
			sb.append(lineSep);
		}
		
		return sb.toString();
	}
	
	public String asString(final String charset) throws UnsupportedEncodingException {
		return new String(buff, 0, length, charset);
	}
	
	@Override
	public void writeStringLenUTF8(final String remote) {
		if (remote == null) {
			writeInt(-1);
		} else {
			byte[] fufu;
			try {
				fufu = remote.getBytes("UTF-8");
			} catch(final UnsupportedEncodingException e) {
				throw new InternalError();
			}
			writeInt(fufu.length);
			write(fufu, 0, fufu.length);
		}
	}
	
	@Override
	public void writeBoolean(final boolean val) {
		write(val ? 1 : 0);
	}
	
	@Override
	public void writeByte(final int val) {
		write(val);
	}
	
	@Override
	@Deprecated
	public void writeBytes(String s) throws IOException {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void writeChars(String s) throws IOException {
		BinaryUtilJava.putChars(s, this);
	}
	
	@Override
	@Deprecated
	public void writeUTF(String str) throws IOException {
		BinaryUtilJava.putJavaModifiedUTF(str, this);
	}
	
	@Override
	public void writeASCII(String str) throws IOException {
		write(str.getBytes("US-ASCII"));
	}
	
	@Override
	public void writeDoubles(double[] vals, int off, int len, boolean bigEndian) throws IOException {
		if (bigEndian) {
			int nBytes = 8*len;
			int writePos = prepareWrite(nBytes);
			ByteBuffer.wrap(buff, writePos, nBytes).asDoubleBuffer().put(vals, off, len);
		} else {
			int srcIdx = off;
			for (int i = 0; i < len; i++) {
				writeDoubleLE(vals[srcIdx++]);
			}
		}
	}
	
	@Override
	public void seek(long newPos) throws IOException {
		pos = (int)newPos;
	}
	
	@Override
	public long length() throws IOException {
		return length;
	}
	
	public ByteArrayInputStream createInputStream() {
		return new ByteArrayInputStream(buff, 0, length);
	}
}
