package com.sinergise.util.io;

import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;

public class ByteArrayOutputStream extends OutputStream implements BinaryOutput
{
	byte[] buff;

	int count;

	/**
	 * Creates a new BAOS with the default buffer size (32 bytes).
	 */
	public ByteArrayOutputStream()
	{
		this(32);
	}

	/**
	 * Creates a new BAOS with the specified buffer size (must be >= 0).
	 * 
	 * @param buffSize
	 *            the initial buffer size
	 */
	public ByteArrayOutputStream(int buffSize)
	{
		if (buffSize < 0)
			throw new IllegalArgumentException();

		buff = new byte[buffSize];
	}

	/**
	 * Grows the internal buffer so it is at least minSize big.
	 * 
	 * @param minSize
	 *            the new minimum size
	 */
	protected void grow(int minSize)
	{
		if (minSize < 0) {
			throw new IllegalStateException("Array size exhausted");
		}
		int newSize = buff.length << 1;
		if (newSize < minSize)
			newSize = minSize;

		byte[] newbuff = new byte[newSize];
		System.arraycopy(buff, 0, newbuff, 0, count);
		buff = newbuff;
	}

	public void write(byte[] b, int off, int len)
	{
		ArrayUtils.checkOffsetLength(off, len, b.length);

		int nc = count + len;
		if (nc > buff.length) {
			grow(nc);
		}

		System.arraycopy(b, off, buff, count, len);
		count = nc;
	}

	public void write(byte[] b)
	{
		write(b, 0, b.length);
	}

	public void write(int b)
	{
		int nc = count + 1;
		if (nc > buff.length) {
			grow(nc);
		}

		buff[count] = (byte) b;
		count = nc;
	}

	/**
	 * Writes a binary integer (4 bytes) in little-endian order.
	 * 
	 * @param val
	 *            the value to write
	 */
	public void writeIntLE(int val)
	{
		int nc = count + 4;
		if (nc > buff.length)
			grow(nc);

		Binary.putIntLE(buff, count, val);
		count = nc;
	}

	/**
	 * Writes a binary float (4 bytes) in little-endian order.
	 * 
	 * @param val
	 *            the value to write
	 */
	public void writeFloatLE(float fval)
	{
		int nc = count + 4;
		if (nc > buff.length)
			grow(nc);

		Binary.putFloatLE(buff, count, fval);
		count = nc;
	}

	/**
	 * Writes a binary short (2 bytes) in little-endian order.
	 * 
	 * @param val
	 *            the value to write
	 */
	public void writeShortLE(short val)
	{
		int nc = count + 2;
		if (nc > buff.length)
			grow(nc);

		Binary.putShortLE(buff, count, val);
		count = nc;
	}

	/**
	 * Writes a binary char (2 bytes) in little-endian order.
	 * 
	 * @param val
	 *            the value to write
	 */
	public void writeCharLE(char val)
	{
		int nc = count + 2;
		if (nc > buff.length)
			grow(nc);

		Binary.putCharLE(buff, count, val);
		count = nc;
	}

	/**
	 * Writes a binary double (8 bytes) in little-endian order.
	 * 
	 * @param val
	 *            the value to write
	 */
	public void writeDoubleLE(double value)
	{
		int nc = count + 8;
		if (nc > buff.length)
			grow(nc);

		Binary.putDoubleLE(buff, count, value);
		count = nc;
	}

	/**
	 * Writes a binary int (4 bytes) in big-endian order.
	 * 
	 * @param val
	 *            the value to write
	 */
	public void writeInt(int val)
	{
		int nc = count + 4;
		if (nc > buff.length)
			grow(nc);

		Binary.putInt(buff, count, val);
		count = nc;
	}

	/**
	 * Writes a binary float (4 bytes) in big-endian order.
	 * 
	 * @param val
	 *            the value to write
	 */
	public void writeFloat(float fval)
	{
		int nc = count + 4;
		if (nc > buff.length)
			grow(nc);

		Binary.putFloat(buff, count, fval);
		count = nc;
	}

	/**
	 * Writes a binary short (2 bytes) in big-endian order.
	 * 
	 * @param val
	 *            the value to write
	 */
	public void writeShort(short val)
	{
		int nc = count + 2;
		if (nc > buff.length)
			grow(nc);

		Binary.putShort(buff, count, val);
		count = nc;
	}

	/**
	 * Writes a binary char (2 bytes) in big-endian order.
	 * 
	 * @param val
	 *            the value to write
	 */
	public void writeChar(char val)
	{
		int nc = count + 2;
		if (nc > buff.length)
			grow(nc);

		Binary.putChar(buff, count, val);
		count = nc;
	}

	/**
	 * Writes a binary double (8 bytes) in big-endian order.
	 * 
	 * @param val
	 *            the value to write
	 */
	public void writeDouble(double value)
	{
		int nc = count + 8;
		if (nc > buff.length)
			grow(nc);

		Binary.putDouble(buff, count, value);
		count = nc;
	}
	
	/**
	 * Writes a long (8 bytes) in big-endian order.
	 * 
	 * @param val
	 *            the value to write
	 */
	public void writeLong(long value)
	{
		int nc = count + 8;
		if (nc > buff.length)
			grow(nc);

		Binary.putLong(buff, count, value);
		count = nc;
	}
	
	/**
	 * Writes a long (8 bytes) in little-endian order.
	 * 
	 * @param val
	 *            the value to write
	 */
	public void writeLongLE(long value)
	{
		int nc = count + 8;
		if (nc > buff.length)
			grow(nc);

		Binary.putLongLE(buff, count, value);
		count = nc;
	}

	/**
	 * Returns the number of bytes written to this BAOS so far.
	 * 
	 * @return
	 */
	public int size()
	{
		return count;
	}

	/**
	 * Returns the data written to this BAOS in a new byte[]. If you don't want a copy to be created, use
	 * getInternalBuffer() and size() to get to the data.
	 * 
	 * @return the data written to this BAOS
	 */
	public byte[] toByteArray()
	{
		byte[] out = new byte[count];
		System.arraycopy(buff, 0, out, 0, count);
		return out;
	}

	/**
	 * Returns the reference to the internal buffer of this BAOS. Use it if you want to examine the bytes or whatever.
	 * Feel free to corrupt the buffer as you see fit, as it's your data anyway :)
	 * 
	 * @return
	 */
	public byte[] getInternalBuffer()
	{
		return buff;
	}

	/**
	 * Writes the data written to this BAOS to another output stream. Just a convenience method, about the same as
	 * 
	 * <pre>
	 * os.write(baos.getInternalBuffer(), 0, baos.size());
	 * </pre>
	 * 
	 * @param os
	 *            the stream to write to
	 * @throws IOException
	 *             if an I/O exception occurs
	 */
	public void writeTo(OutputStream os) throws IOException
	{
		os.write(buff, 0, count);
	}

	/**
	 * Writes the data written to this BAOS to a RandomAccessFile. Just a convenience method, about the same as
	 * 
	 * <pre>
	 * raf.write(baos.getInternalBuffer(), 0, baos.size());
	 * </pre>
	 * 
	 * @param raf
	 *            the file to write to
	 * @throws IOException
	 *             if an I/O exception occurs
	 */
	public void writeTo(RandomAccessFile raf) throws IOException
	{
		raf.write(buff, 0, count);
	}

	/**
	 * Sets size to 0, so new data can be put in this BAOS. Same as calling reset(false);
	 */
	public void reset()
	{
		count = 0;
	}

	/**
	 * Sets size to 0 and optionally shrinks the internal buffer. If the shrinkBuffer parameter is true, the internal
	 * buffer will be shrinked to 32 bytes, if it was previously larger (the data is discarded in any case).
	 * 
	 * @param shrinkBuffer
	 *            if true, memory for a large buffer will be released
	 */
	public void reset(boolean shrinkBuffer)
	{
		count = 0;
		if (shrinkBuffer) {
			if (buff.length > 32)
				buff = new byte[32];
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
	public String toHexString()
	{
		return toHexString(16, true, " ", "\n");
	}

	/**
	 * Returns this BAOS's contents in hexadecimal representation. The parameters control the exact output.
	 * 
	 * @param bytesPerLine
	 *            how many bytes to put in one line (must be at least 1)
	 * @param upperCase
	 *            controls whether the numbers A-F are uppercase or lowercase
	 * @param byteSep
	 *            the separator between bytes (often " ")
	 * @param lineSep
	 *            the separator between lines (usually "\n")
	 * @return
	 */
	public String toHexString(int bytesPerLine, boolean upperCase, String byteSep, String lineSep)
	{
		if (bytesPerLine < 1)
			throw new IllegalArgumentException();
		if (byteSep == null)
			byteSep = "";
		if (lineSep == null)
			lineSep = "";

		char letBase = (char) (upperCase ? 'A' - 10 : 'a' - 10);

		byte[] data = buff;
		int pos = 0;
		int lines = count / bytesPerLine;
		int remain = count - lines * bytesPerLine;
		int lastLine = -1;
		if (remain > 0) {
			lastLine = lines++;
		}

		int last = bytesPerLine;
		StringBuffer sb = new StringBuffer();
		for (int a = 0; a < lines; a++) {
			if (a == lastLine)
				last = remain;

			for (int b = 0; b < last; b++) {
				if (b > 0)
					sb.append(byteSep);

				int val = data[pos++] & 255;
				int high = val >>> 4;
				int low = val & 15;

				sb.append((char) (high > 9 ? letBase + high : '0' + high));
				sb.append((char) (low > 9 ? letBase + low : '0' + low));
			}
			sb.append(lineSep);
		}

		return sb.toString();
	}

	public String asString(String charset) throws UnsupportedEncodingException
    {
		return new String(buff, 0, count, charset);
    }

	public void writeStringLenUTF8(String remote)
    {
		if (remote == null) {
			writeInt(-1);
		} else {
			byte[] fufu;
            try {
	            fufu = remote.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
            	throw new InternalError();
            }
			writeInt(fufu.length);
			write(fufu);
		}
    }

	public void writeBool(boolean val)
    {
		write(val ? 1 : 0);
    }

	public void writeByte(byte val)
    {
		write(val);
    }
}
