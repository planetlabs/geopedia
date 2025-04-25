package com.sinergise.java.raster.io;

import java.io.IOException;

import javax.imageio.stream.ImageOutputStreamImpl;

import com.sinergise.java.util.io.ByteArrayOutputStream;

public class ByteArrayImageOutputStream extends ImageOutputStreamImpl {
	private final ByteArrayOutputStream baos;
	
	public ByteArrayImageOutputStream() {
		this(128*1024);
	}
	
	public ByteArrayImageOutputStream(int initialSize) {
		baos = new ByteArrayOutputStream(initialSize);
	}

	public byte[] getInternalBuffer() {
		return baos.getInternalBuffer();
	}
	
	public int getSize() {
		return baos.size();
	}
	
	@Override
	public void seek(long pos) throws IOException {
		baos.seek(pos);
        this.streamPos = pos;
        this.bitOffset = 0;
	}
	
	@Override
	public void write(int b) {
		baos.write(b);
		super.streamPos++;
	}

	@Override
	public void write(byte[] b, int off, int len) {
		baos.write(b, off, len);
		super.streamPos+=len;
	}

	@Override
	public int read() {
		return baos.getInternalBuffer()[(int)streamPos++];
	}

	@Override
	public int read(byte[] b, int off, int len) {
		int trueLen = Math.min(baos.size() - (int)streamPos, len);
		System.arraycopy(baos.getInternalBuffer(), (int)streamPos, b, off, trueLen);
		streamPos += trueLen;
		return trueLen;
	}

	public ByteArrayOutputStream getStream() {
		return baos;
	}
	
}
