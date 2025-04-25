package com.sinergise.geopedia.client.components.routing;

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.maps.client.geocode.DirectionQueryOptions;
import com.google.gwt.maps.client.geocode.DirectionQueryOptions.TravelMode;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.sinergise.common.util.event.SourcesValueChangeEvents;
import com.sinergise.common.util.event.ValueChangeListener;
import com.sinergise.common.util.event.ValueChangeListenerCollection;
import com.sinergise.geopedia.client.core.i18n.MessagesWithLookup;

class TravelModeSelectionPanel extends Composite implements SourcesValueChangeEvents<TravelMode> {

	private Map<TravelMode, Button> modeButtons = new LinkedHashMap<TravelMode, Button>();
	private TravelMode selectedMode = null;
	private ValueChangeListenerCollection<TravelMode> selectionListeners = new ValueChangeListenerCollection<DirectionQueryOptions.TravelMode>();
	
	private FlowPanel panel;
	
	TravelModeSelectionPanel() {
		initWidget(panel = new FlowPanel());
		addStyleName("travelMode");
	}
	
	public TravelMode getSelectedMode() {
		return selectedMode;
	}
	
	public void setSelectedMode(TravelMode mode) {
		if (modeButtons.containsKey(mode) && mode != selectedMode) {
			TravelMode oldMode = selectedMode;
			selectedMode = mode;
			updateSelectedUI();
			selectionListeners.fireChange(this, oldMode, mode);
		}
	}
	
	public void addTravelModeOption(final TravelMode mode) {
		if (modeButtons.containsKey(mode)) return;
		
		Button but = new Button("<span></span>");
		
		but.setStyleName(mode.name()+" btn");
		but.setTitle(MessagesWithLookup.INSTANCE.getString("TravelMode_"+mode.name())); //TODO:
		but.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				setSelectedMode(mode);
			}
		});
		
		modeButtons.put(mode, but);
		panel.add(but);
		
		if (selectedMode == null) {
			setSelectedMode(mode);
		}
	}
	
	public boolean removeTravelMode(TravelMode mode) {
		if (modeButtons.containsKey(mode) 
			&& panel.remove(modeButtons.get(mode)))
		{
			if (selectedMode == mode) {
				setSelectedMode(!modeButtons.isEmpty() ? selectedMode = modeButtons.keySet().iterator().next() : null);
			}
			return true;
		}
		return false;
	}
	
	@Override
	public void addValueChangeListener(ValueChangeListener<? super TravelMode> l) {
		selectionListeners.add(l);
	}
	
	@Override
	public void removeValueChangeListener(ValueChangeListener<? super TravelMode> l) {
		selectionListeners.remove(l);
	}
	
	private void updateSelectedUI() {
		for (TravelMode mode : modeButtons.keySet()) {
			Button but = modeButtons.get(mode);
			but.removeStyleName("selected");
			if (mode == selectedMode) {
				but.addStyleName("selected");
			}
		}
	}
}
