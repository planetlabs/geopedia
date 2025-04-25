package com.sinergise.geopedia.server;

public interface SessionTask {
	public boolean isFinished();
	public void cleanup();
}
