package com.sinergise.java.util.io;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import com.sinergise.common.util.ArrayUtil;
import com.sinergise.java.util.io.BinaryInput.BinaryRandomAccessInput;

public class ByteArrayInputStream extends InputStream implements BinaryRandomAccessInput {
	protected byte buff[];
	protected int  pos;
	protected int  markPos;
	protected int  lastPos;
	
	public ByteArrayInputStream(final byte buff[]) {
		this.buff = buff;
		this.pos = 0;
		this.lastPos = buff.length;
	}
	
	public ByteArrayInputStream(final byte buff[], final int offset, final int length) {
		this.buff = buff;
		this.pos = offset;
		this.lastPos = Math.min(offset + length, buff.length);
		this.markPos = offset;
	}
	
	@Override
	public int read() {
		return (pos < lastPos) ? (buff[pos++] & 0xff) : -1;
	}
	
	@Override
	public int read(final byte b[], final int off, int len) {
		ArrayUtil.checkOffsetLength(off, len, b.length);
		
		if (pos >= lastPos) {
			return -1;
		}
		
		if (pos + len > lastPos) {
			len = lastPos - pos;
		}
		
		if (len <= 0) {
			return 0;
		}
		
		System.arraycopy(buff, pos, b, off, len);
		pos += len;
		return len;
	}
	
	@Override
	public long skip(long n) {
		if (pos > lastPos - n) n = lastPos - pos;
		if (n < 0) return 0;
		pos += n;
		return n;
	}
	
	@Override
	public void skipFully(int n) throws IOException {
		skip(n);
	}
	
	@Override
	public int available() {
		return lastPos - pos;
	}
	
	@Override
	public boolean markSupported() {
		return true;
	}
	
	@Override
	public synchronized void mark(final int readAheadLimit) {
		markPos = pos;
	}
	
	@Override
	public synchronized void reset() {
		pos = markPos;
	}
	
	@Override
	public void close() {
	// nothing to do..
	}
	
	protected int consume(final int size) throws EOFException {
		if (available() < size) {
			throw new EOFException();
		}
		
		final int res = pos;
		pos = res + size;
		return res;
	}
	
	@Override
	public short readShort() throws EOFException {
		return BinaryUtilJava.getShort(buff, consume(2));
	}
	
	@Override
	public char readChar() throws EOFException {
		return BinaryUtilJava.getChar(buff, consume(2));
	}
	
	@Override
	public int readInt() throws EOFException {
		return BinaryUtilJava.getInt(buff, consume(4));
	}
	
	@Override
	public float readFloat() throws EOFException {
		return BinaryUtilJava.getFloat(buff, consume(4));
	}
	
	@Override
	public double readDouble() throws EOFException {
		return BinaryUtilJava.getDouble(buff, consume(8));
	}
	
	@Override
	public short readShortLE() throws EOFException {
		return BinaryUtilJava.getShortLE(buff, consume(2));
	}
	
	@Override
	public char readCharLE() throws EOFException {
		return BinaryUtilJava.getCharLE(buff, consume(2));
	}
	
	@Override
	public int readIntLE() throws EOFException {
		return BinaryUtilJava.getIntLE(buff, consume(4));
	}
	
	@Override
	public float readFloatLE() throws EOFException {
		return BinaryUtilJava.getFloatLE(buff, consume(4));
	}
	
	@Override
	public double readDoubleLE() throws EOFException {
		return BinaryUtilJava.getDoubleLE(buff, consume(8));
	}
	
	@Override
	public String readStringLenUTF8() throws EOFException {
		final int len = readInt();
		if (len < 0) {
			return null;
		}
		
		if (len == 0) {
			return "";
		}
		
		String out;
		try {
			out = new String(buff, pos, len, "UTF-8");
		} catch(final UnsupportedEncodingException e) {
			throw new InternalError();
		}
		pos += len;
		return out;
	}
	
	@Override
	public long readLong() throws EOFException {
		return BinaryUtilJava.getLong(buff, consume(8));
	}
	
	@Override
	public long readLongLE() throws EOFException {
		return BinaryUtilJava.getLongLE(buff, consume(8));
	}
	
	public boolean readBool() throws EOFException {
		return readByte() != 0;
	}
	
	@Override
	public byte readByte() throws EOFException {
		final int r = read();
		if (r < 0) {
			throw new EOFException();
		}
		return (byte)r;
	}
	
	@Override
	public void readFully(final byte[] outBuff) throws IOException {
		if (read(outBuff, 0, outBuff.length) < outBuff.length) {
			throw new EOFException();
		}
	}
	
	@Override
	public void readFully(final byte[] outBuff, final int offset, final int length) throws EOFException {
		if (read(outBuff, offset, length) < outBuff.length) {
			throw new EOFException();
		}
	}
	
	@Override
	public String readASCII(final int len) throws IOException {
		return BinaryUtilJava.getASCII(this, len, len<=8 ? buff : new byte[len]);
	}
	
	@Override
	public boolean readBoolean() throws IOException {
		return readByte() != 0;
	}
	@Override
	@Deprecated
	public String readLine() throws IOException {
		throw new UnsupportedOperationException();
	}
	@Override
	public int readUnsignedByte() throws IOException {
		return BinaryUtilJava.getUByte(buff, consume(1));
	}
	@Override
	public int readUnsignedShort() throws IOException {
		return BinaryUtilJava.getUShort(buff, consume(2));
	}
	
	@Override
	@Deprecated
	public String readUTF() throws IOException {
		return BinaryUtilJava.getJavaModifiedUTF(this);
	}
	@Override
	public int skipBytes(int n) throws IOException {
		return (int)skip(n);
	}

	@Override
	public long readUnsignedIntLE() throws IOException {
		return BinaryUtilJava.getUIntLE(buff, consume(4));
	}

	@Override
	public int readUnsignedShortLE() throws IOException {
		return BinaryUtilJava.getUShortLE(buff, consume(2));
	}

	@Override
	public long readUnsignedInt() throws IOException {
		return BinaryUtilJava.getUInt(buff, consume(4));
	}
	
	@Override
	public void seek(long seekPos) throws IOException {
		this.pos = (int)seekPos; 
	}
	
	@Override
	public long length() throws IOException {
		return lastPos;
	}
}
