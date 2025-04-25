package com.sinergise.gwt.gis.map.ui.attributes;

import static com.sinergise.common.util.string.StringUtil.appendIfNotEmpty;
import static com.sinergise.common.util.string.StringUtil.isNullOrEmpty;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.gis.feature.CFeatureUtils.PropertyDisplayData;
import com.sinergise.common.util.property.descriptor.PropertyType;
import com.sinergise.gwt.util.history.HistoryLink;
import com.sinergise.gwt.util.history.HistoryManager;

public class ReadOnlyPropertyWidgetFactory implements PropertyWidgetFactory {
	
	@Override
	public Widget createLabelWidget(PropertyDisplayData<?> data) {
		return new Label(createLabelString(data));
	}

	protected String createLabelString(PropertyDisplayData<?> data) {
		return appendIfNotEmpty(data.title, ": ");
	}

	@Override
	public Widget createValueWidget(PropertyDisplayData<?> data) {
		Widget w = createValueWidgetInstance(data);
		decorateValueWidget(w, data);
		return w;
	}
	
	protected Widget createValueWidgetInstance(PropertyDisplayData<?> data) {
		if (isImage(data)) {
			return createImageWidget(data);
		} else if (hasNonEmptyLink(data)) {
			return createLinkWidget(data);
		} else if (isSafeHtml(data)) {
			return createSafeHtmlWidget(data);
		}
		
		return createPlainWidget(data);
	}
	
	protected void decorateValueWidget(Widget w, PropertyDisplayData<?> data) {
		if (data.tooltip != null) {
			w.setTitle(data.tooltip);
		}
	}
	
	
	protected Widget createPlainWidget(PropertyDisplayData<?> data) {
		return new Label(data.getValue());
	}
	
	protected Widget createImageWidget(PropertyDisplayData<?> data) {
		Widget w = new Image(data.getValue());
		w.setWidth("100%");
		return w;
	}
	
	protected Widget createLinkWidget(PropertyDisplayData<?> data) {
		if (isHistoryLink(data.link)) {
			return new HistoryLink(data.getValue(), false, HistoryManager.parseHistoryToken(data.link.substring(1)));
		} 
		return new Anchor(data.getValue(), data.link, "_blank");
	}
	
	protected Widget createSafeHtmlWidget(PropertyDisplayData<?> data) {
		Widget w = new HTML(data.getValue());
		w.setWidth("100%");
		return w;
	}
	
	protected static boolean isImage(PropertyDisplayData<?> data) {
		return data.getDesc().getInfoString(PropertyType.KEY_SEMANTIC_TYPE, "").equals(PropertyType.SEMANTIC_TYPE_IMAGE);
	}
	
	private static boolean isSafeHtml(PropertyDisplayData<?> data) {
		return data.getDesc().getInfoString(PropertyType.KEY_SEMANTIC_TYPE, "").equals(PropertyType.SEMANTIC_TYPE_SAFE_HTML);
	}

	protected static boolean hasNonEmptyLink(PropertyDisplayData<?> data) {
		return !isNullOrEmpty(data.link);
	}

	protected static boolean isHistoryLink(String url) {
		return url.charAt(0) == '#';
	}

}
