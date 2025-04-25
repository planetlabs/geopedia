package com.sinergise.common.ui.messages;

import com.sinergise.common.util.messages.MessageType;

public interface MessageListener {

	void onMessage(MessageType type, String msg);
	
	public static class DummyMessageListener implements MessageListener {
		
		public void onMessage(MessageType type, String msg) {
			//ignore
		}
	}
	
}
