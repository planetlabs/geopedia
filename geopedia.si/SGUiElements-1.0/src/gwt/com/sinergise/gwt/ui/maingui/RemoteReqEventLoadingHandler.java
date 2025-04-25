package com.sinergise.gwt.ui.maingui;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.sinergise.gwt.util.event.RemoteReqEvent;
import com.sinergise.gwt.util.event.RemoteReqEventHandler;

/**
 * {@link RemoteReqEventHandler} that show {@link ILoadingWidget} 
 * while there are any remote requests in progress.
 * 
 * @author tcerovski
 */
public class RemoteReqEventLoadingHandler implements RemoteReqEventHandler {

	/** Loading widget to show when any remote requests are in progress */
	private final ILoadingWidget loadingWidget;
	
	/** Remote requests in progress.  */
	private Set<RemoteReqEvent> inProgress = new HashSet<RemoteReqEvent>();
	
	/**
	 * @param loadingWidget Loading widget to show while any remote requests are in progress.
	 */
	public RemoteReqEventLoadingHandler(ILoadingWidget loadingWidget) {
		this.loadingWidget = loadingWidget;
	}
	
	/* (non-Javadoc)
	 * @see com.sinergise.gwt.util.remote.RemoteReqEventHandler#remoteRequestStarted()
	 */
	public void remoteRequestStarted(RemoteReqEvent event) {
		if(!event.isFinished()) {
			inProgress.add(event);
			loadingWidget.showLoading(0);
		}
	}

	/* (non-Javadoc)
	 * @see com.sinergise.gwt.util.remote.RemoteReqEventHandler#remoteRequestFinished()
	 */
	public void remoteRequestFinished(RemoteReqEvent event) {
		if(event.isFinished()) {
			inProgress.remove(event);
		}
		if(inProgress.isEmpty()) {
			loadingWidget.hideLoading();
		}
	}
	
	public Collection<RemoteReqEvent> getEventsInProgress() {
		return Collections.unmodifiableSet(inProgress);
	}
	
}
