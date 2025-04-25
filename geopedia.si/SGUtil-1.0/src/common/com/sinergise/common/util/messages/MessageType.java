package com.sinergise.common.util.messages;

import static com.sinergise.common.util.messages.TypedMessage.FUNCTION_GET_MSG_TYPE;

import com.sinergise.common.util.collections.CollectionUtil;
import com.sinergise.common.util.string.StringUtil;

public enum MessageType {
	
	PROGRESS(0), INFO(1), SUCCESS(2), QUESTION(3), WARNING(4), ERROR(5);

	public  final String title;
	private final int priority;
	
	private MessageType(int priority) {
		this.priority = priority;
		this.title = StringUtil.toTitleCase(name());
	}
	
	public static MessageType highestPriority(MessageType ...types) {
		MessageType highest = null;
		for (MessageType type : types) {
			if (highest == null || highest.priority < type.priority) {
				highest= type;
			}
		}
		return highest;
	}
	
	public static MessageType highestPriority(TypedMessage ...msgs) {
		return highestPriority(CollectionUtil.mapToArray(msgs, new MessageType[msgs.length], FUNCTION_GET_MSG_TYPE));
	}
}
