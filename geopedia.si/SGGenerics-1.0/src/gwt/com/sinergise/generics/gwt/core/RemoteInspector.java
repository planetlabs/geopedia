package com.sinergise.generics.gwt.core;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.XMLParser;
import com.sinergise.generics.core.MetaAttributes;
import com.sinergise.generics.core.services.GenericsService;
import com.sinergise.generics.core.services.GenericsServiceAsync;
import com.sinergise.generics.core.util.XMLUtils;

public class RemoteInspector implements WidgetInspector{

	private final org.slf4j.Logger logger =
        org.slf4j.LoggerFactory.getLogger(RemoteInspector.class); 
	private final GenericsServiceAsync genericsService = GWT.create(GenericsService.class);

	private String widgetId;
	private WidgetInspectorListener listener = null; 
	
	public RemoteInspector (String widgetId) {
		this.widgetId = widgetId;
	}
	
	@Override
	public void setWidgetInspectorListener (WidgetInspectorListener listener) {
		this.listener = listener;
	}
	
	@Override
	public void inspect() {
		logger.trace("Running remote inspection for widget with ID='"+widgetId+"'");
	genericsService.getWidgetMetadata(widgetId, LocaleInfo.getCurrentLocale().getLocaleName(),  new AsyncCallback<String>() {
		
		@Override
		public void onSuccess(String result) {
			if (result ==null || result.length() == 0) {
				noResultsError(widgetId);
				return;
			}
			logger.trace("Remote inspection for widgetId '"+widgetId+"' succeeded.");
			Document masterDocument = XMLParser.parse( result );
			Element metadata = (Element) masterDocument.getFirstChild();
			if (metadata==null) {
				noResultsError(widgetId);
				return;
			}
			Element widgetMetadata = XMLUtils.findElementByAttributeValue(metadata, MetaAttributes.NAME, widgetId);
			if (listener!=null)
				listener.inspectionCompleted(widgetMetadata);
		}
		
		@Override
		public void onFailure(Throwable caught) {
			logger.error("Remote inspection failed for widgetId '" + widgetId + "'!",caught);
			NotificationHandler.instance().handleException(caught);
		}
	});
	}
	
	private void noResultsError(String widgetId) {
		logger.error("Remote inspection for widgetId '"+widgetId+"' returned no results!");
		NotificationHandler.instance().handleException("Remote inspection for widgetId '"+widgetId+"' returned no results!");
	}
}
	
