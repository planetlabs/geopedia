package com.sinergise.generics.gwt.widgets;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

public class NoticeableWidgetWrapper<T extends Widget> extends FlowPanel{
	
	public static final String META_NOTIFY_ERROR="notifyError";
	public static final String META_NOTIFY_WARNING="notifyWarning";
	public static final String TYPE_ERROR = "error";
	public static final String TYPE_WARNING = "warning";

	private T wrappedWidget;	
	private Image iconError;
	private Image iconWarning;
	
	public static boolean isNotificationAttribute (String attribute) {
		if (META_NOTIFY_ERROR.equals(attribute) || 
			META_NOTIFY_WARNING.equals(attribute)) {
				return true;
		}
		return false;
	}
	public NoticeableWidgetWrapper(T widget) {
		setStyleName("NoticeableWrapper");
		wrappedWidget = widget;
		add(wrappedWidget);
	}
	
	public T getWrappedWidget() {
		return wrappedWidget;
	}
	
	
	public void showIcon(String style, String title) {
		if (style.equals(TYPE_ERROR)) {
			if (iconError==null) {
				iconError = new Image();
				add(iconError);
			}
			iconError.setUrl(GWT.getModuleBaseURL()+"img/stop.png");
			iconError.setStyleName(style);
			if (title!=null)
				iconError.setTitle(title);
		}
		else if (style.equals(TYPE_WARNING)) {
			if (iconWarning==null) {
				iconWarning = new Image();
				add(iconWarning);
			}
			iconWarning.setUrl(GWT.getModuleBaseURL()+"img/warning.png");
			iconWarning.setStyleName(style);
			if (title!=null)
				iconWarning.setTitle(title);
		}
	}
	
	public void hideIcon(String style) {
		if (style.equals(TYPE_ERROR)) {
			if (iconError!=null) {
				remove(iconError);
				iconError=null;
			}
		}
		else if (style.equals(TYPE_WARNING)) {
			if (iconWarning!=null) {
				remove(iconWarning);
				iconWarning=null;
			}
		}
	}
	
	public boolean isWarning() {
		return iconWarning != null;
	}
	
	public boolean isError() {
		return iconError != null;
	}
}
