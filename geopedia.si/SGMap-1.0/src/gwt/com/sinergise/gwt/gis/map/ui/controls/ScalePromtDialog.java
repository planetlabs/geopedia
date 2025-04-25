package com.sinergise.gwt.gis.map.ui.controls;


import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.ui.ButtonBase;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.gwt.gis.map.messages.AppMessages;
import com.sinergise.gwt.ui.ButtonFactory;
import com.sinergise.gwt.ui.editor.DoubleEditor;
import com.sinergise.gwt.ui.maingui.Breaker;


class ScalePromtDialog extends DialogBox {
	private static final AppMessages MESSAGES = (AppMessages) GWT.create(AppMessages.class);
	public final DoubleEditor newValueEdtor;
	Double newValue = null;

	ScalePromtDialog(String oldVal) {
		super(false, true);
		FlowPanel vp = new FlowPanel();
		FlowPanel scalePanel = new FlowPanel();
		scalePanel.add(new InlineLabel("1: "));
		newValueEdtor = new DoubleEditor();
		newValueEdtor.setEditorValue(Double.valueOf(oldVal));
		scalePanel.add(newValueEdtor);
		vp.add(scalePanel);
		vp.add(new Breaker(7));
		vp.add(buildStatus());
		setText(MESSAGES.SpireMapControls_COMPONENT_ZOOM_COMBO());
		newValueEdtor.setFocus(true);
		newValueEdtor.addKeyPressHandler(new KeyPressHandler() {
			@Override
			public void onKeyPress(KeyPressEvent event) {
				if(event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER){
					saveValue();
				}				
			}
		});
		this.setWidget(vp);
	}

	private Widget buildStatus() {
		ButtonBase save = ButtonFactory.createOkButton();
		save.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				saveValue();
			}
		});
		ButtonBase cancel = ButtonFactory.createCancelButton();
		cancel.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				cancelAction();
			}
		});

		FlowPanel fp = new FlowPanel();
		fp.add(save);
		fp.add(cancel);
		return (fp);
	}

	private void saveValue() {
		newValue = newValueEdtor.getEditorValue();
		ScalePromtDialog.this.hide();
	}

	private void cancelAction() {
		newValue = null;
		ScalePromtDialog.this.hide();
	}


}
