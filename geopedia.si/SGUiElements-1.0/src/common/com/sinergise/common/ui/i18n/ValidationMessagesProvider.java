package com.sinergise.common.ui.i18n;


public class ValidationMessagesProvider {
	
	protected static ValidationMessagesProvider INSTANCE = null;

	public static synchronized void setInstance(ValidationMessagesProvider context) {
		if (INSTANCE != null) {
			throw new RuntimeException("ValidationMessagesProvider instance already set!");
		}
		INSTANCE = context;
	}

	public static synchronized ValidationMessagesProvider getInstance() {
		if (INSTANCE == null) {
			initialize();
		}
		return INSTANCE;
	}
	
	public static void initialize() {
		setInstance(new ValidationMessagesProvider());
		initializeInstance();
	}
	
	protected static void initializeInstance() {
		messages = new MessagesWithLookup(ResourceUtil.create(ValidationMessages.class));
	}
	
	protected static MessagesWithLookup messages;
	
	
	public MessagesWithLookup getValidationMessages(){
		return messages;
	}
	
}
