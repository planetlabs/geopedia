package com.sinergise.java.util.io;

import java.io.IOException;
import java.io.InputStream;

public class StreamBinaryInput extends AbstractBinaryInput {
	protected final InputStream stream;
	
	public StreamBinaryInput(InputStream is) {
		this.stream = is;
	}
	
	@Override
	public void close() throws IOException {
		stream.close();
	}
	
	@Override
	public void readFully(byte[] b, int off, int len) throws IOException {
		IOUtilJava.readFully(stream, b, off, len);
	}

	@Override
	public int skipBytes(int n) throws IOException {
		return (int)stream.skip(n);
	}

	public int read() throws IOException {
		return stream.read();
	}

	public int read(byte[] b) throws IOException {
		return stream.read(b);
	}

	public int read(byte[] b, int off, int len) throws IOException {
		return stream.read(b, off, len);
	}

	public long skip(long n) throws IOException {
		return stream.skip(n);
	}

	public int available() throws IOException {
		return stream.available();
	}

	public void mark(int readlimit) {
		stream.mark(readlimit);
	}

	public void reset() throws IOException {
		stream.reset();
	}

	public boolean markSupported() {
		return stream.markSupported();
	}

}
