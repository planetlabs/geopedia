package com.sinergise.common.util.messages;

import java.util.LinkedList;

@SuppressWarnings("serial")
public class ValidationMessage extends TypedMessage {
	
	String defaultMessage;
	
	String validationCode;
	String[] args;

	@Deprecated
	protected ValidationMessage() { }
	
	
	public ValidationMessage(MessageType type, String defaultMessage, String validationCode, String... args) {
		super(type, defaultMessage);
		
		this.defaultMessage = defaultMessage;
		this.validationCode = validationCode;
		this.args = args;
	}
	
	public String getDefaultMessage(){
		return defaultMessage;
	}


	public String getValidationCode() {
		return validationCode;
	}


	public String[] getArgs() {
		return args;
	}

	private static final String SEPARATOR_CANONICAL = "<%>";
	private static final int SEPARATOR_LENGTH = SEPARATOR_CANONICAL.length();

	@Override
	public String toCanonicalString() {
		StringBuilder sb = new StringBuilder(type.name() + SEPARATOR_CANONICAL + defaultMessage + SEPARATOR_CANONICAL + validationCode);
		if(args != null){
			for(String a : args){
				sb.append(SEPARATOR_CANONICAL).append(a);
			}
		}
		return sb.toString();
	}
	
	public static final ValidationMessage fromCanonicalString(String messageString) {
		int messageTypeEndIdx = messageString.indexOf(SEPARATOR_CANONICAL);
		MessageType type = MessageType.valueOf(messageString.substring(0, messageTypeEndIdx));
		
		int defaultMessageEndIdx = messageString.indexOf(SEPARATOR_CANONICAL, messageTypeEndIdx+SEPARATOR_LENGTH);
		String defaultMessage = messageString.substring(messageTypeEndIdx+SEPARATOR_LENGTH, defaultMessageEndIdx);
		
		int valCodeEndIdx = messageString.indexOf(SEPARATOR_CANONICAL, defaultMessageEndIdx+SEPARATOR_LENGTH);
		valCodeEndIdx = valCodeEndIdx == -1 ? messageString.length() : valCodeEndIdx;
		String valCode = messageString.substring(defaultMessageEndIdx+SEPARATOR_LENGTH, valCodeEndIdx);
		
		LinkedList<Object> args = new LinkedList<Object>();
		for(int currentIdx = valCodeEndIdx+SEPARATOR_LENGTH ; currentIdx < messageString.length() ;){
			int endArg = messageString.indexOf(SEPARATOR_CANONICAL, currentIdx+SEPARATOR_LENGTH);
			endArg = (endArg == -1 ? messageString.length() : endArg);

			args.add(messageString.substring(currentIdx, endArg));
			
			currentIdx = endArg+SEPARATOR_LENGTH;
		}
		
		return new ValidationMessage(type, defaultMessage, valCode, args.toArray(new String[args.size()]));
	}
}
