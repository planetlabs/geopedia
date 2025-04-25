package com.sinergise.geopedia.client.ui.feature;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.InlineLabel;
import com.sinergise.geopedia.client.core.ClientGlobals;
import com.sinergise.geopedia.client.core.search.FeatureByIdSearcher;
import com.sinergise.geopedia.core.entities.properties.ForeignReferenceProperty;

public class RepTextLabel extends InlineLabel implements ClickHandler {
	private int tableId;
	private final long featureId;

	public RepTextLabel(int tableId, long featureId, String text) {
		setStyleName("link-like");
		this.tableId = tableId;
		this.featureId = featureId;
		addClickHandler(this);
		setText(featureId, text);

	}

	public RepTextLabel(int tableId, ForeignReferenceProperty frp) {
		this(tableId, frp.getValue(), frp.getReptext());
	}

	public void setText(long id, String text) {
		if (text != null) {
			setText(text);
		} else {
			setText(String.valueOf(id));
		}

	}

	@Override
	public void onClick(ClickEvent event) {
		if (ClientGlobals.defaultSearchExecutor != null) {
			FeatureByIdSearcher fis = new FeatureByIdSearcher(tableId, (int) featureId);
			ClientGlobals.defaultSearchExecutor.executeSearch(fis);
		}
	}

}
