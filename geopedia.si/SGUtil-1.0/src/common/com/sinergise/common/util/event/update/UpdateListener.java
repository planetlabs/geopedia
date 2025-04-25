package com.sinergise.common.util.event.update;

public interface UpdateListener {
	void itemUpdateStarted(Object sender);
	
	void itemUpdateConfirmed(Object sender);
	
	void itemUpdateCancelled(Object sender);
}
