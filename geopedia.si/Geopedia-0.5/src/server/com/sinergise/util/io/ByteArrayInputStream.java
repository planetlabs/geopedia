package com.sinergise.util.io;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;




public class ByteArrayInputStream extends InputStream implements BinaryInput
{
	protected byte buff[];
	protected int pos;
	protected int markPos;
	protected int lastPos;

	public ByteArrayInputStream(byte buff[])
	{
		this.buff = buff;
		this.pos = 0;
		this.lastPos = buff.length;
	}

	public ByteArrayInputStream(byte buff[], int offset, int length)
	{
		this.buff = buff;
		this.pos = offset;
		this.lastPos = Math.min(offset + length, buff.length);
		this.markPos = offset;
	}

	public int read()
	{
		return (pos < lastPos) ? (buff[pos++] & 0xff) : -1;
	}

	public int read(byte b[], int off, int len)
	{
		ArrayUtils.checkOffsetLength(off, len, b.length);

		if (pos >= lastPos)
			return -1;

		if (pos + len > lastPos)
			len = lastPos - pos;

		if (len <= 0)
			return 0;

		System.arraycopy(buff, pos, b, off, len);
		pos += len;
		return len;
	}

	public long skip(long n)
	{
		if (pos > lastPos - n) {
			n = lastPos - pos;
		}
		if (n < 0) {
			return 0;
		}
		pos += n;
		return n;
	}

	public int available()
	{
		return lastPos - pos;
	}

	public boolean markSupported()
	{
		return true;
	}

	public void mark(int readAheadLimit)
	{
		markPos = pos;
	}

	public void reset()
	{
		pos = markPos;
	}

	public void close()
	{
		// nothing to do..
	}
	
	protected int consume(int size) throws EOFException
	{
		if (available() < size)
			throw new EOFException();
		
		int res = pos;
		pos = res + size;
		return res;
	}
	
	public short readShort() throws EOFException
	{
		return Binary.getShort(buff, consume(2));
	}
	
	public char readChar() throws EOFException
	{
		return Binary.getChar(buff, consume(2));
	}
	
	public int readInt() throws EOFException
	{
		return Binary.getInt(buff, consume(4));
	}
	
	public float readFloat() throws EOFException
	{
		return Binary.getFloat(buff, consume(4));
	}
	
	public double readDouble() throws EOFException
	{
		return Binary.getDouble(buff, consume(8));
	}
	
	public short readShortLE() throws EOFException
	{
		return Binary.getShortLE(buff, consume(2));
	}
	
	public char readCharLE() throws EOFException
	{
		return Binary.getCharLE(buff, consume(2));
	}
	
	public int readIntLE() throws EOFException
	{
		return Binary.getIntLE(buff, consume(4));
	}
	
	public float readFloatLE() throws EOFException
	{
		return Binary.getFloatLE(buff, consume(4));
	}
	
	public double readDoubleLE() throws EOFException
	{
		return Binary.getDoubleLE(buff, consume(8));
	}

	public String readStringLenUTF8() throws EOFException
    {
		int len = readInt();
		if (len < 0)
			return null;
		
		if (len == 0)
			return "";

		String out;
        try {
	        out = new String(buff, pos, len, "UTF-8");
        } catch (UnsupportedEncodingException e) {
        	throw new InternalError();
        }
		pos += len;
		return out;
    }

	public long readLong() throws EOFException
    {
		return Binary.getLong(buff, consume(8));
    }
	
	public long readLongLE() throws EOFException
    {
		return Binary.getLongLE(buff, consume(8));
    }

	public boolean readBool() throws EOFException
    {
		return readByte() != 0;
    }

	public byte readByte() throws EOFException
    {
		int r = read();
		if (r < 0)
			throw new EOFException();
		return (byte)r;
    }

	public void readFully(byte[] buff) throws IOException
    {
		if (read(buff, 0, buff.length) < buff.length)
			throw new EOFException();
    }

	public void readFully(byte[] buff, int offset, int length) throws EOFException
    {
		if (read(buff, offset, length) < buff.length)
			throw new EOFException();
    }
}
