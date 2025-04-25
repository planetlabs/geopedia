package com.sinergise.gwt.ui.combo;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

import java.util.Map;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.sinergise.gwt.ui.ImageAnchor;
import com.sinergise.gwt.ui.resources.Theme;

public abstract class MultiSelectDropDown extends FlowPanel {
	
	protected FlowPanel             popupPanel;
	protected ImageAnchor           openAnchor;
	protected PopupPanel            popup;
	protected Map<String, CheckBox> checkBoxes;
	protected FlowPanel             previewPanel;

	public MultiSelectDropDown(String headerTxt, Map<String, CheckBox> mapCB) {
		this.checkBoxes = mapCB;

		openAnchor = new ImageAnchor(headerTxt, Theme.getTheme().standardIcons().edit(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				showHideSelectionPanel();
			}
		});
		previewPanel = new FlowPanel();

		setStyleName("msDD");
		add(openAnchor);
		add(previewPanel);
		previewPanel.setStyleName("msDDPreview");
	}

	public Map<String, CheckBox> getCheckBoxes() {
		return checkBoxes;
	}

	public void setEnabled(boolean isDisabled) {
		openAnchor.setEnabled(isDisabled);
	}

	public FlowPanel getPreviewPanel() {
		return previewPanel;
	}

	public void showPreviewPanel(boolean show) {
		previewPanel.setVisible(show);
	}

	public void hide() {
		this.setVisible(false);
	}

	public void show() {
		this.setVisible(true);
	}

	public void resetWidget() {
		for (Map.Entry<String, CheckBox> entry : checkBoxes.entrySet()) {
			entry.getValue().setValue(FALSE);
		}
		updatePreviewPanel();
	}

	public void checkAllCheckBoxes() {
		for (Map.Entry<String, CheckBox> entry : checkBoxes.entrySet()) {
			entry.getValue().setValue(TRUE);
		}
		updatePreviewPanel();
	}

	public void updateWidget(String[] selection) {
		for (Map.Entry<String, CheckBox> e : checkBoxes.entrySet()) {
			Boolean found = FALSE;
			if (selection != null) {
				for (int i = 0; i < selection.length; i++) {
					if (selection[i] != null && selection[i].equals(e.getKey())) {
						found = TRUE;
						break;
					}
				}
			}
			e.getValue().setValue(found);
		}
		updatePreviewPanel();
	}

	protected abstract FlowPanel generateCheckboxFlowPanel();
	protected abstract void      updatePreviewPanel();

	public void showHideSelectionPanel() {
		popup = new PopupPanel(true);
		popup.setWidth(openAnchor.getElement().getOffsetWidth()-10+"px");
		
		FlowPanel innerPanel = new FlowPanel();
		innerPanel.setStyleName("multiSelectInnerPanel");
		innerPanel = generateCheckboxFlowPanel();
		popupPanel = new FlowPanel();
		popupPanel.add(innerPanel);
		popupPanel.addStyleName("withClosePopup");
		popupPanel.addStyleName("multiselectPanel");
		popupPanel.add(new Anchor() {
			{
				setStyleName("close");
				addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						popup.hide();
					}
				});
			}
		});
		popupPanel.add(new Anchor() {
			{
				setStyleName("close bottom");
				addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						popup.hide();
					}
				});
			}
		});
		popup.setWidget(popupPanel);
		if (!popup.isShowing())
			popup.showRelativeTo(openAnchor);

		popup.addCloseHandler(new CloseHandler<PopupPanel>() {
			
			public void onClose(CloseEvent<PopupPanel> event) {
				updatePreviewPanel();
				
			}
		});
	}
}
