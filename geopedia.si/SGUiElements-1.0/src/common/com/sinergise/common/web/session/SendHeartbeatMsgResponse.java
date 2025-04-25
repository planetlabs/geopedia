package com.sinergise.common.web.session;

import java.io.Serializable;

public class SendHeartbeatMsgResponse implements Serializable {

	private static final long serialVersionUID = -9115888971138394932L;
	
	private long timestamp;
	
	@Deprecated /** Serialization only */
	protected SendHeartbeatMsgResponse() {}
	
	public SendHeartbeatMsgResponse(long timestamp) {
		this.timestamp = timestamp;
	}
	
	public long getTimestamp() {
		return timestamp;
	}

}
