package com.sinergise.java.util.io;

import java.io.IOException;

public interface HasRandomAccess {
	void seek(long pos) throws IOException;
	long length() throws IOException;
}
