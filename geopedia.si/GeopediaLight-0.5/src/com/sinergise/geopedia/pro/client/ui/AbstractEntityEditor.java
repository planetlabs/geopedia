package com.sinergise.geopedia.pro.client.ui;

import static com.sinergise.gwt.ui.maingui.Buttons.YES;

import java.util.ArrayList;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.sinergise.common.ui.i18n.Buttons;
import com.sinergise.common.util.messages.MessageType;
import com.sinergise.geopedia.core.entities.AbstractEntityWithDescription;
import com.sinergise.geopedia.pro.client.i18n.ProConstants;
import com.sinergise.geopedia.themebundle.ui.GeopediaStandardIcons;
import com.sinergise.gwt.ui.ImageAnchor;
import com.sinergise.gwt.ui.dialog.MessageDialog;
import com.sinergise.gwt.ui.dialog.OptionDialog.ButtonsListener;
import com.sinergise.gwt.ui.maingui.extwidgets.SGFlowPanel;
import com.sinergise.gwt.ui.maingui.extwidgets.SGPushButton;

public abstract class AbstractEntityEditor<T extends AbstractEntityWithDescription> {
	
	protected T entity;
	protected ArrayList<AbstractEntityEditorPanel<T>> tabs = new ArrayList<AbstractEntityEditorPanel<T>>();
	
	protected abstract void selectPanel(AbstractEntityEditorPanel<T> panel);
	protected abstract void close(boolean saved);
	protected abstract void showLoadingIndicator(boolean show);
	protected abstract void showError(String message, boolean show);
	protected abstract void saveEntity(T entity);
	private static String question ="";
	
	
	public FlowPanel createButtonPanel() {
		return createButtonPanel(false, null);
	}
	public FlowPanel createButtonPanel(boolean addDelete, final String deleteConfirmationmessage) {
		SGPushButton editorBtnSave = new SGPushButton(Buttons.INSTANCE.save(),GeopediaStandardIcons.INSTANCE.saveWhite(),new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				onSave();
			}
		});
		editorBtnSave.addStyleName("blue");

		Anchor editorBtnCancel = new Anchor(Buttons.INSTANCE.cancel());
		editorBtnCancel.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				close(false);
			}
		});
		
		SGFlowPanel btnPanel = new SGFlowPanel("editingPanel clearfix");
		if (addDelete) {
			
			ImageAnchor editorBtnDelete = new ImageAnchor(GeopediaStandardIcons.INSTANCE.deleteWhite(),new ClickHandler() {
				
				@Override
				public void onClick(ClickEvent event) {
					
					MessageDialog.createYesNo("", MessageType.QUESTION, deleteConfirmationmessage,
							new ButtonsListener() {
								@Override
								public boolean buttonClicked(int whichButton) {
									if (whichButton == YES) {
										entity.setDeleted(true);
										onSave();
									}
									return true;
								}
							}, false).center();
					
					
				}
	
			});
			editorBtnDelete.setTitle(Buttons.INSTANCE.delete());
			editorBtnDelete.addStyleName("fl-right");
			btnPanel.insert(editorBtnDelete,0);
		}
		btnPanel.addWidgets(editorBtnSave, editorBtnCancel);
		return btnPanel;
	}
	
	
	protected void setEntity(T entity) {
		this.entity = entity;
		for (AbstractEntityEditorPanel<T> tab:tabs)
			tab.loadEntity(entity);
	}
	
	private boolean validate() {
		showError(null, false);
		for (AbstractEntityEditorPanel<T> tab:tabs) {
			if (!tab.validate()) {
				selectPanel(tab);
				showError(ProConstants.INSTANCE.wrongData(), true);
				return false;
			}
		}
		return true;
	}
	
	private void onSave() {
		if (!validate())
			return;
		
		for (AbstractEntityEditorPanel<T> tab:tabs) {
			if (!tab.saveEntity(entity)) {
				selectPanel(tab);
				return;
			}
		}
		showLoadingIndicator(true);
		showError(null,false);
		saveEntity(entity);		
	}
}
