package com.sinergise.gwt.gis.map.ui.controls.mapLayersTree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sinergise.common.gis.map.model.style.StylePropertyElement;
import com.sinergise.common.util.state.gwt.PropertyChangeListener;
import com.sinergise.common.util.state.gwt.StateGWT;
import com.sinergise.gwt.ui.ActionToggleButton;

public class TextPropertyPanel extends PopupPanel {
	private final String styleSessionParamName;
	private final StateGWT state;
	private HashMap<String, CheckBox> cbList;
	ActionToggleButton parentButton;

	VerticalPanel vp = new VerticalPanel();

	public TextPropertyPanel(final StateGWT state, String styleSessionParamName, ArrayList<StylePropertyElement> auxParams, String compName) {
		super();
		addStyleName("textPropertyPanel");
		this.state = state;
		this.styleSessionParamName = styleSessionParamName;
		cbList = new HashMap<String, CheckBox>();
		init(auxParams, compName);
		this.add(vp);
		this.setAutoHideEnabled(true);
		
		this.addCloseHandler(new CloseHandler<PopupPanel>() {
			@Override
			public void onClose(CloseEvent<PopupPanel> event) {
				if (TextPropertyPanel.this.parentButton != null) {
					TextPropertyPanel.this.parentButton.setDown(false);
					TextPropertyPanel.this.hide();
				}
			}
		});
	}
	
	public void setParentButton(ActionToggleButton parentButton) {
		this.parentButton = parentButton;
	}

	@Override
	public void show() {
		synchronizeState();
		super.show();
	}

	private void init(ArrayList<StylePropertyElement> auxParams, String compName) {
		for (StylePropertyElement spe : auxParams) {
			if (compName.equals(spe.styleType)) {
				addCheckBox(spe.labelName, spe.columnName, spe.initState);
			}
		}
		synchronizeState();
	}

	private void addCheckBox(String label, final String stateKey, boolean isChecked) {
		final CheckBox cb = new CheckBox(label);
		cb.setValue(Boolean.valueOf(isChecked));
		putState(stateKey, isChecked);
		cb.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				boolean checked = ((CheckBox)event.getSource()).getValue().booleanValue();
				putState(stateKey, checked);
			}
		});
		
		state.addPropertyChangeListener(new PropertyChangeListener<Object>() {
			@Override
			public void propertyChange(Object sender, String propertyName, Object oldValue, Object newValue) {
				if (propertyName.equals(stateKey)) {
					cb.setValue(Boolean.valueOf(newValue.toString()));
				}
			}
		});
		cbList.put(stateKey, cb);
		vp.add(cb);
	}

	private void synchronizeState() {
		StateGWT localState = state.getState(styleSessionParamName);
		if(localState == null) return;
		Iterator<Entry<String, CheckBox>> iterator = cbList.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, CheckBox> entry = iterator.next();
			CheckBox cb = entry.getValue();
			@SuppressWarnings("boxing")
			Boolean value = localState.getBoolean(entry.getKey(), cb.getValue());
			cb.setValue(value);
		}
	}


	private void putState(final String stateKey, boolean checked) {
		StateGWT localState = state.getState(styleSessionParamName);
		if (localState == null) {
			localState = new StateGWT();
		}
		localState.putBoolean(stateKey, checked);
		state.putState(styleSessionParamName, localState);
	}

}
