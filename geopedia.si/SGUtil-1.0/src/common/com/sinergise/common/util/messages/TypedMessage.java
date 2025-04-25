package com.sinergise.common.util.messages;

import java.io.Serializable;

import com.sinergise.common.util.lang.Function;
import com.sinergise.common.util.string.HasCanonicalStringRepresentation;

public class TypedMessage implements Serializable, HasCanonicalStringRepresentation {
	private static final long serialVersionUID = 1L;

	protected MessageType type;
	protected String messageHtml;
	
	@Deprecated
	protected TypedMessage() {
	}
	
	public TypedMessage(MessageType type, String messageHtml) {
		super();
		this.type = type;
		this.messageHtml = messageHtml;
	}
	
	public MessageType getType() {
		return type;
	}
	
	public String getMessageHtml() {
		return messageHtml;
	}
	
	@Override
	public String toString() {
		return toCanonicalString();
	}
	
	@Override
	public String toCanonicalString() {
		return type.name() + " " + messageHtml;
	}
	
	public static TypedMessage fromCanonicalString(String messageString) {
		int spcIdx = messageString.indexOf(' ');
		return new TypedMessage(MessageType.valueOf(messageString.substring(0, spcIdx)), messageString.substring(spcIdx+1));
	}
	
	public static final Function<TypedMessage, MessageType> FUNCTION_GET_MSG_TYPE = new Function<TypedMessage, MessageType>() {
		
		@Override
		public MessageType execute(TypedMessage param) {
			return param.type;
		}
	};
	
}
