package com.sinergise.java.util.io;

import java.io.IOException;
import java.io.OutputStream;

public class StreamBinaryOutput extends AbstractBinaryOutput {
	protected final OutputStream os;
	
	public StreamBinaryOutput(OutputStream os) {
		this.os = os;
	}
	
	@Override
	public void writeByte(int v) throws IOException {
		os.write(v);
	}
	
	@Override
	public void close() throws IOException {
		os.close();
	}
	
	@Override
	public void flush() throws IOException {
		os.flush();
	}
	
	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		os.write(b, off, len);
	}
}
