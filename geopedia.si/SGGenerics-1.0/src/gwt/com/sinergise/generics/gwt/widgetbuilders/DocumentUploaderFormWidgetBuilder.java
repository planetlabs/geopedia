package com.sinergise.generics.gwt.widgetbuilders;

import java.util.Map;

import com.google.gwt.user.client.ui.Widget;
import com.sinergise.generics.core.MetaAttributes;
import com.sinergise.generics.gwt.widgets.NoticeableWidgetWrapper;
import com.sinergise.generics.gwt.widgets.upload.GenericDocumentUploader;
import com.sinergise.generics.gwt.widgets.upload.GenericDocumentUploaderWidget;
import com.sinergise.generics.gwt.widgets.upload.WidgetLabels;

public class DocumentUploaderFormWidgetBuilder extends FormWidgetBuilder {
	WidgetLabels widgetLabels;

	public DocumentUploaderFormWidgetBuilder(WidgetLabels widgetLabels) {
		this.widgetLabels = widgetLabels;
	}

	@Override
	public Widget buildWidget(String attributeName, Map<String, String> metaAttributes) {
		Widget w = super.buildWidget(attributeName, metaAttributes);
		if (metaAttributes.containsKey(MetaAttributes.FILE_UPLOAD_COUNT)) {
			w = new GenericDocumentUploaderWidget(widgetLabels);
		}
		return w;
	}
	
	
	@Override
	public Object getWidgetValue(Widget widget, String attributeName, Map<String, String> metaAttributes) {
		if (widget==null)
			return null;
		
		if (widget instanceof NoticeableWidgetWrapper<?>) {// extract wrapped widget
			widget = ((NoticeableWidgetWrapper<Widget>) widget).getWrappedWidget();
		}
		
		
		String widgetValue = null;
		if (widget instanceof GenericDocumentUploaderWidget) {
			GenericDocumentUploaderWidget uploader = (GenericDocumentUploaderWidget) widget;
			widgetValue = uploader.getValue();
			return widgetValue;
		}

		return super.getWidgetValue(widget, attributeName, metaAttributes);
	}
	
	@Override
	public void setWidgetValue(Widget widget, String attributeName, Map<String, String> metaAttributes, Object value) {
		if (widget instanceof GenericDocumentUploaderWidget) {
			GenericDocumentUploaderWidget uploader = (GenericDocumentUploaderWidget) widget;
			if (value != null && !value.equals("null") && GenericDocumentUploader.isIntegerValue((String) value)) {
				uploader.setValue(Integer.valueOf((String) value));
			}
		}

		super.setWidgetValue(widget, attributeName, metaAttributes, value);
	}

}
