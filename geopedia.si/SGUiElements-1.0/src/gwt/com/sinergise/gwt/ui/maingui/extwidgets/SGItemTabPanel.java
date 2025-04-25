package com.sinergise.gwt.ui.maingui.extwidgets;

import static com.sinergise.common.util.string.StringUtil.isNullOrEmpty;
import static com.sinergise.gwt.ui.i18n.UiMessages.UI_MESSAGES;
import static com.sinergise.gwt.util.DefaultWindowClosingHandler.isWindowClosing;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasText;
import com.sinergise.common.ui.controls.DeepVisibilityChangeListener;
import com.sinergise.common.ui.controls.VisibilityChangeListener;
import com.sinergise.common.util.messages.MessageType;
import com.sinergise.common.util.naming.IdentifiableEntity;
import com.sinergise.common.util.naming.Identifier;
import com.sinergise.gwt.ui.NotificationPanel;
import com.sinergise.gwt.ui.controls.EnsureVisibilityUtil;
import com.sinergise.gwt.ui.dialog.MessageDialog;
import com.sinergise.gwt.ui.dialog.OptionDialog.ButtonsListener;
import com.sinergise.gwt.ui.maingui.Buttons;
import com.sinergise.gwt.ui.maingui.ILoadingWidget;
import com.sinergise.gwt.ui.maingui.StandardUIConstants;
import com.sinergise.gwt.ui.resources.Theme;
import com.sinergise.gwt.util.history.HistoryManager;

public abstract class SGItemTabPanel<ItemType extends IdentifiableEntity> extends SGFlowPanel implements HasText, DeepVisibilityChangeListener {
	
	public static final String DEFAULT_NEW_HISTORY_ARG = "NEW";
	
	protected ItemType              data;
	protected Identifier            id;
	protected DetailTabsLayoutPanel parent;
	protected String                text;
	protected boolean               isPinned;
	protected boolean 				readOnly = true;
	protected CommandButtons        commandButtons;
	public    static ILoadingWidget loadingWidget;
	
	protected final String 			historyParamKey;
	protected VisibilityChangeListener visibilityListener;
	
	public SGItemTabPanel() {
		this(null);
	}
	
	public SGItemTabPanel(String historyArgForId){
		this.historyParamKey = historyArgForId;
	}
	
	@Override
	public void deepVisibilityChanged(boolean newVisible) {
		updateHistoryParameter(newVisible);
	}
	
	public void updateHistoryParameter(boolean tabVisible) {
		if (!isNullOrEmpty(historyParamKey) && data != null) {
			HistoryManager histMgr = HistoryManager.getInstance();
			
			String historyParamValue = getCurrentHistoryParamValue();
			if (isNullOrEmpty(historyParamValue)) {
				histMgr.removeHistoryParam(historyParamKey);
			} else if (tabVisible) {
				histMgr.setHistoryParam(historyParamKey, historyParamValue);
			} else if (!isWindowClosing()
				&& historyParamValue != null
				&& historyParamValue.equals(histMgr.getHistoryParam(historyParamKey))) 
			{
				histMgr.removeHistoryParam(historyParamKey);
			}
		}
	}
	
	protected String getCurrentHistoryParamValue() {
		if (data == null || !data.hasPermanentId()) {
			return null;
		}
		return data.getLocalID();
	}
	
	public void load(Identifier idToLoad) {
		this.id = idToLoad;
	}
	
	protected void render() {
	}
	
	protected boolean isNewHistoryArg(String historyArg){
		return DEFAULT_NEW_HISTORY_ARG.equals(historyArg);
	}
	
	protected void processingStart() {
		if (loadingWidget != null) {
			loadingWidget.showLoading(10000);
		}
	}
	
	protected void processingSuccess() {
		if (loadingWidget != null) {
			loadingWidget.hideLoading();
		}
		
		if (EnsureVisibilityUtil.isDeepVisible(this)) {
			updateHistoryParameter(true);
		}
	}
	
	protected void processingFailure(Throwable caught) {
		if (loadingWidget != null) {
			loadingWidget.hideLoading();
		}
		
		displayErrorNotification(null, caught);
	}

	public Identifier getItemIdentifier() {
		return id;
	}

	public void setParent(DetailTabsLayoutPanel parent) {
		this.parent = parent;
	}
	
	public ImageResource getIcon() {
		return null;
	}

	public void setTextAndIcon(String text, ImageResource icon) {
		this.text = text;
		if (parent == null) {
			return;
		}
		
		int index = parent.getWidgetIndex(this);
		if (index < 0) {
			return;
		}
		
		if (parent != null) {
			parent.setTabTextAndImage(index, text, icon);
		}
	}
	
	@Override
	public void setText(String text) {
		this.text = text;
		if (parent == null) {
			return;
		}
		
		int index = parent.getWidgetIndex(this);
		if (index < 0) {
			return;
		}
		
		if (parent != null) {
			parent.setTabText(index, text);
		}
	}
	
	@Override
	public String getText() {
		return text;
	}
	
	public void setPinned(boolean isPinned) {
		this.isPinned = isPinned;
	}
	
	public boolean isPinned() {
		return isPinned;
	}
	
	public boolean isReadOnly(){
		return readOnly;
	}
	
	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}
	
	public ItemType getData(){
		return data;
	}
	
	@SuppressWarnings("unused")
	protected void save(AsyncCallback<Void> asyncCallback) {
	}
	
	public class CommandButtons extends SGFlowPanel {
		private SGPushButton edit;
		private SGPushButton save;
		private SGPushButton cancel;
		private SGPushButton retire;
		
		private SGItemTabPanel<?> itemPanel;
		private MessageDialog messageDialog;
		private NotificationPanel error;

		public CommandButtons(SGItemTabPanel<?> itemPanel) {
			this(itemPanel, false, false);
		}
		
		public CommandButtons(SGItemTabPanel<?> itemPanel, boolean hasRetireButtons, boolean defaultEditMode) {
			this.itemPanel = itemPanel;
			setStyleName("sgItemTab-commandButtons");
			add(edit   = new SGPushButton(StandardUIConstants.STANDARD_CONSTANTS.buttonEdit(), Theme.getTheme().standardIcons().edit(),new ClickHandler() {
				@Override
				public void onClick(ClickEvent arg0) {
					cleanupError();
					
					setEditMode(true);
					
					CommandButtons.this.itemPanel.setReadOnly(false);
					CommandButtons.this.itemPanel.render();
				}
			}));
			add(save   = new SGPushButton(StandardUIConstants.STANDARD_CONSTANTS.buttonSave(), Theme.getTheme().standardIcons().save(),new ClickHandler() {
				@Override
				public void onClick(ClickEvent arg0) {
					cleanupError();
					
					CommandButtons.this.itemPanel.save(new AsyncCallback<Void>() {
						@Override
						public void onSuccess(Void result) {
							setEditMode(false);
							
							CommandButtons.this.itemPanel.setReadOnly(true);
							CommandButtons.this.itemPanel.render();
						}
						
						@Override
						public void onFailure(Throwable caught) {
							displayErrorNotification("Error while saving form", caught);
						}
					});
				}
			}));
			add(cancel = new SGPushButton(StandardUIConstants.STANDARD_CONSTANTS.buttonCancel(), Theme.getTheme().standardIcons().cancel(),new ClickHandler() {
				@Override
				public void onClick(ClickEvent arg0) {
					cleanupError();
					CommandButtons.this.itemPanel.cancelEdit();
					
					setEditMode(false);
					
					CommandButtons.this.itemPanel.setReadOnly(true);
					if(data.hasPermanentId())
						CommandButtons.this.itemPanel.load(data.getQualifiedID());
					else 
						CommandButtons.this.itemPanel.render();//I think we should close the tab here but I leave it like this because it was like this before
				}
			}));
			
			if (hasRetireButtons) {
				add (retire = new SGPushButton(StandardUIConstants.STANDARD_CONSTANTS.buttonRetire(), Theme.getTheme().standardIcons().delete(), new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						event.stopPropagation();
						messageDialog = MessageDialog.createConfirm(
							UI_MESSAGES.itemPanel_retireDialog_title(), 
							SafeHtmlUtils.fromSafeConstant(UI_MESSAGES.itemPanel_retireDialog_msg(getObjectRetireText())),
							new ButtonsListener() {
								
								@Override
								public boolean buttonClicked(int whichButton) {
									if (whichButton == Buttons.OK) {
										CommandButtons.this.itemPanel.changeStatus("R");
										return true;
									}
									else if (whichButton == Buttons.CANCEL) {
										return true;
									}
									else {
										return false;
									}
								}
							});
						messageDialog.showCentered();
						}
					}));
			}

			setEditMode(defaultEditMode);
			itemPanel.addStyleName("panelWithToolbar");
		}
		
		public SGPushButton getEdit() {
			return edit;
		}

		public SGPushButton getSave() {
			return save;
		}

		public SGPushButton getCancel() {
			return cancel;
		}

		public SGPushButton getRetire() {
			return retire;
		}

		public void setEditMode(boolean editMode) {
			edit.setVisible  (!editMode);
			if(retire != null){
				retire.setVisible  (!editMode);
			}
			save.setVisible  ( editMode);
			cancel.setVisible( editMode);
		}
		
		private void cleanupError() {
			if (error != null) {
				this.remove(error);
				error = null;
			}
		}
		
		public void displayErrorNotification(String message) {
			if (error != null)
				remove(error);
			
			CommandButtons.this.add(
					error = new NotificationPanel(message, MessageType.ERROR));
		}

		public void displayErrorNotification(String preludeMessage, Throwable e) {
			if (preludeMessage == null) {
				preludeMessage = "";
			} else {
				preludeMessage = preludeMessage + ": ";
			}
			
			displayErrorNotification(preludeMessage+e.getMessage());
		}
	}

	@SuppressWarnings("unused")
	protected void changeStatus(String statusCode) {
		
	}
	
	protected void cancelEdit() {
		cleanupEditState();
	}		
	
	protected void displayErrorNotification(String preludeMessage, Throwable e) {
		if (commandButtons != null) {
			commandButtons.displayErrorNotification(preludeMessage, e);
		}
	}
	
	@Override
	protected void onLoad() {
		super.onLoad();
		visibilityListener = EnsureVisibilityUtil.registerDeepVisibilityChangeListener(this, this);
	}
	
	@Override
	public void onUnload() {
		super.onUnload();
		
		if (visibilityListener != null) {
			EnsureVisibilityUtil.deregisterVisibilityChangeListener(this, visibilityListener);
		}
		
		if (!readOnly) {
			cleanupEditState();
		}
	}

	protected void cleanupEditState() {
	}
	
	protected String getObjectRetireText() {
		return "";
	}
	
}
