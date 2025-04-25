package com.sinergise.gwt.ui.maingui.extwidgets;

import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ToggleButton;
import com.sinergise.common.util.event.selection.Selectable;
import com.sinergise.common.util.event.selection.SelectableImpl;
import com.sinergise.common.util.event.selection.SourcesToggleEvents;
import com.sinergise.common.util.event.selection.ToggleListener;

public class SGToggleButton extends ToggleButton implements Selectable {
	
	private Selectable model = null;

	public SGToggleButton() {
		super();
		setModel(new SelectableImpl());
	}
	
	public SGToggleButton(Selectable model) {
		super();
		setModel(model);
	}

	public SGToggleButton(Image upImage) {
		super(upImage);
		setModel(new SelectableImpl());
	}
	
	public SGToggleButton(Image upImage, Selectable model) {
		super(upImage);
		setModel(model);
	}

	public SGToggleButton(String upText) {
		super(upText);
		setModel(new SelectableImpl());
	}
	
	public SGToggleButton(String upText, Selectable model) {
		super(upText);
		setModel(model);
	}

	private void setModel(final Selectable model) {
		this.model = model;
		bindModel();
	}
	
	private void bindModel() {
		addAttachHandler(new AttachEvent.Handler() {
			@Override
			public void onAttachOrDetach(AttachEvent event) {
				setValue(Boolean.valueOf(model.isSelected()));
			}
		});
		
		addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				model.setSelected(event.getValue().booleanValue());
			}
		});
		
		model.addToggleListener(new ToggleListener() {
			@Override
			public void toggleStateChanged(SourcesToggleEvents source, boolean newOn) {
				setValue(Boolean.valueOf(newOn));
			}
		});
	}
	
	@Override
	public void addToggleListener(ToggleListener l) {
		model.addToggleListener(l);
	}
	
	@Override
	public void removeToggleListener(ToggleListener l) {
		model.removeToggleListener(l);
	}
	
	@Override
	public boolean isSelected() {
		return model.isSelected();
	}
	
	@Override
	public void setSelected(boolean sel) {
		model.setSelected(sel);
	}
	
	public Selectable getModel() {
		return model;
	}
	
}
