package com.sinergise.generics.gwt.core;

import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.util.messages.MessageType;


public class NotificationHandler {

	protected static NotificationHandler instance = null;

	public static void setInstance(NotificationHandler inst) {
		instance = inst;
	}

	public static NotificationHandler instance() {
		if (instance == null) {
			instance = new NotificationHandler();
		}
		return instance;
	}

	public void processingStart() {
		System.out.println("Generics NotificationHandler processingStart");
	}

	public void processingStop() {
		System.out.println("Generics NotificationHandler processingStop");
	}

	public void processingStart(Widget w) {
		processingStart();
	}

	public void processingStop(Widget w) {
		processingStop();
	}

	public void handleException(String exception) {
		System.err.println("Generics NotificationHandler exception " + exception);
	}

	public void handleException(Throwable caught) {
		System.err.println("Generics NotificationHandler exception:");
		caught.printStackTrace();
	}

	public void showMessage(String title, String message, MessageType type) {
		System.err.println("Generics NotificationHandler showMessage [" + type + "] " + title + ": " + message);
	}

	public void showMessageNextTo(Widget w, String title, String message, MessageType type) {
		showMessage(title, message, type);
	}
}
