package com.sinergise.geopedia.pro.client.ui.table;

import static com.sinergise.gwt.ui.maingui.Buttons.YES;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.ui.i18n.Buttons;
import com.sinergise.common.util.messages.MessageType;
import com.sinergise.geopedia.client.core.ClientGlobals;
import com.sinergise.geopedia.client.core.entities.Repo;
import com.sinergise.geopedia.client.core.events.OpenSidebarPanelEvent;
import com.sinergise.geopedia.client.core.events.OpenSidebarPanelEvent.SidebarPanelType;
import com.sinergise.geopedia.client.core.i18n.ExceptionI18N;
import com.sinergise.geopedia.client.core.i18n.Messages;
import com.sinergise.geopedia.client.ui.LoadingIndicator;
import com.sinergise.geopedia.core.entities.Table;
import com.sinergise.geopedia.light.client.i18n.GeopediaTerms;
import com.sinergise.geopedia.pro.client.i18n.ProConstants;
import com.sinergise.geopedia.pro.client.ui.AbstractEntityEditorPanel;
import com.sinergise.geopedia.pro.theme.GeopediaProStyle;
import com.sinergise.geopedia.pro.theme.dialogs.ProDialogsStyle;
import com.sinergise.geopedia.pro.theme.layeredit.LayerEditStyle;
import com.sinergise.geopedia.themebundle.ui.GeopediaStandardIcons;
import com.sinergise.gwt.gis.resources.GisTheme;
import com.sinergise.gwt.ui.BoldText;
import com.sinergise.gwt.ui.Heading;
import com.sinergise.gwt.ui.NotificationPanel;
import com.sinergise.gwt.ui.dialog.AbstractDialogBox;
import com.sinergise.gwt.ui.dialog.MessageDialog;
import com.sinergise.gwt.ui.dialog.OptionDialog.ButtonsListener;
import com.sinergise.gwt.ui.maingui.extwidgets.SGPushButton;
import com.sinergise.gwt.ui.resources.Theme;

public class TableWizardDialog  extends AbstractDialogBox {
	private static final Logger logger = LoggerFactory.getLogger(TableWizardDialog.class);

	private WizardStepPanel stepsPanel = new WizardStepPanel();	
	private ArrayList<WizardStep> wizardSteps = new ArrayList<WizardStep>();
	
	
	private FlowPanel buttonsHolder;
	private FlowPanel stepHolder;
	private SGPushButton btnPrev;
	private SGPushButton btnNext;
	private SGPushButton btnSave;
	private SGPushButton btnCancel;
	private FlowPanel contentPanel;
	private int currentStep;
	
	private WizardStep stepStyle;
	private WizardStep stepBasic;
	private WizardStep stepFields;
	
	private Table table;
	private LoadingIndicator loadingIndicator;
	private NotificationPanel notificationPanel;
	
	private static class WizardStep {
		public WizardStep(AbstractEntityEditorPanel<Table> stepPanel, String stepName) {
			this.stepName=stepName;
			this.stepPanel=stepPanel;
		}
		public String stepName;
		public AbstractEntityEditorPanel<Table> stepPanel;
	}
	
	
	
	private class WizardStepPanel extends FlowPanel {
		private static final String STYLE_ACTIVE_STEP ="active";
		public WizardStepPanel() {
			setStyleName("wizardStepPanel");
		}
		
		public void rebuild() {
			clear();
			for (int i=0;i<wizardSteps.size();i++) {
				WizardStep step = wizardSteps.get(i);
				FlowPanel wPanel = new FlowPanel();		
				wPanel.setStyleName("step step"+(i+1));
				wPanel.add(new BoldText());
				if (i==0) {
					wPanel.add(new Image(Theme.getTheme().standardIcons().info()));
				} else if (i==1) {
					wPanel.add(new Image(Theme.getTheme().standardIcons().input()));
				} else {
					wPanel.add(new Image(GisTheme.getGisTheme().gisStandardIcons().layerStyle()));
				}
				wPanel.add(new InlineLabel(step.stepName));
				add(wPanel);
				if (i==currentStep) {
					wPanel.addStyleName(STYLE_ACTIVE_STEP);
				}
				/*if (i==0) {
					wPanel.addStyleName("first");
				} else if (i==(wizardSteps.size()-1)) {
					wPanel.addStyleName("last");
				}*/
					
			}
		}

		public void updateStepSelection() {
			for (int i=0;i<getWidgetCount();i++) {
				Widget pnl = getWidget(i);
				pnl.removeStyleName(STYLE_ACTIVE_STEP);
				if (i==currentStep)
					pnl.addStyleName(STYLE_ACTIVE_STEP);	
			}
		}
	}

	
	private void removeWizardStep(WizardStep step) {
		if (currentStep < wizardSteps.indexOf(step)) {
			wizardSteps.remove(step);
			stepsPanel.rebuild();
		}
	}
	private void addWizardStep(WizardStep step) {
		if (wizardSteps.contains(step))
			return;
		wizardSteps.add(step);	
		stepsPanel.rebuild();
	}
	
	
	
	private void selectWizardStep(int stepIdx) {
		currentStep=stepIdx;
		stepHolder.clear();
		stepHolder.add(wizardSteps.get(currentStep).stepPanel);
		stepsPanel.updateStepSelection();
		updateButtons();
		getCurrentStep().stepPanel.loadEntity(table);
	}
	
	
	private void updateButtons() {
		buttonsHolder.clear();
		buttonsHolder.add(new Image(GeopediaProStyle.INSTANCE.shadowPro()));
		buttonsHolder.add(btnCancel);
		if (!isFirstStep()) {
			buttonsHolder.add(btnPrev);
		}
		if (!isLastStep()) {
			buttonsHolder.add(btnNext);
		} else {
			buttonsHolder.add(btnSave);
		}
	}
	
	
	private boolean isLastStep() {
		if (currentStep==(wizardSteps.size()-1))
			return true;
		return false;
	}
	
	private boolean isFirstStep() {
		if (currentStep==0)
			return true;
		return false;
	}
	
	private void previousStep() {
		selectWizardStep(currentStep-1);
		
	}
	
	private WizardStep getCurrentStep() {
		return wizardSteps.get(currentStep);
	}
	
	private boolean saveStep() {
		WizardStep step = getCurrentStep();
		if (step.stepPanel.validate()) {
			return step.stepPanel.saveEntity(table);
		}
		return false;		
	}
	private void nextStep() {
		WizardStep step = getCurrentStep();
		if (saveStep()) {
			if (step==stepBasic) {
				if (table.getGeometryType().isGeom()) {
					addWizardStep(stepStyle);
				} else {
					removeWizardStep(stepStyle);
				}
			}
			selectWizardStep(currentStep+1);
		}
		
	}
	private void save() {
		if (saveStep()) {
			loadingIndicator.setVisible(true);
			notificationPanel.hide();
			Repo.instance().saveTable(table, new AsyncCallback<Table>() {
				
				@Override
				public void onSuccess(Table result) {
					loadingIndicator.setVisible(false);
					hide();
					ClientGlobals.getMapLayers().addTableToVirtualTheme(result.getId(), Messages.INSTANCE.virtualLayersGroupTitle());
					ClientGlobals.eventBus.fireEvent(new OpenSidebarPanelEvent(SidebarPanelType.CONTENT_TAB));					
				}
				
				@Override
				public void onFailure(Throwable caught) {
					loadingIndicator.setVisible(false);
					notificationPanel.showErrorMsg(ExceptionI18N.getLocalizedMessage(caught));
					logger.error("Save failed!",caught);				
				}
			});
		}
	}
	
	
	private void cancel() {
		MessageDialog.createYesNo("", MessageType.QUESTION, ProConstants.INSTANCE.layerWizardCancel(), 
				new ButtonsListener() {
					@Override
					public boolean buttonClicked(int whichButton) {
						if (whichButton == YES) {
							hide();
						}
						return true;
					}
				}, false).center();
	}
	
	public TableWizardDialog() {
		super(false, true,true,true);
		LayerEditStyle.INSTANCE.layerEdit().ensureInjected();
		ProDialogsStyle.INSTANCE.wizardDialog().ensureInjected();
		
		table = new Table();
		if (Window.getClientHeight() < 800) {
			setSize("800px", Window.getClientHeight()-150+"px");
		} else {
			setSize("800px", "675px");
		}
		addStyleName("editor table wizard");
		center();
		setText("Table wizard");
		buttonsHolder = new FlowPanel();
		buttonsHolder.setStyleName("buttonsPanel");
		
		stepHolder = new FlowPanel();
		stepHolder.setStyleName("stepHolder");
		
		btnPrev = new SGPushButton(Buttons.INSTANCE.back(), Theme.getTheme().standardIcons().arrowLeft(), new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				previousStep();				
			}
		});
		btnPrev.addStyleName("prev");
		btnNext = new SGPushButton(Buttons.INSTANCE.next(), Theme.getTheme().standardIcons().arrowRight(), new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				nextStep();				
			}
		});
		btnCancel = new SGPushButton(Buttons.INSTANCE.cancel(), Theme.getTheme().standardIcons().close(), new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				cancel();				
			}
		});
		btnCancel.addStyleName("cancel");
		btnSave = new SGPushButton(Buttons.INSTANCE.save(), GeopediaStandardIcons.INSTANCE.saveWhite(), new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				save();				
			}
		});
		btnSave.addStyleName("blue");
		
		stepBasic = new WizardStep(new TableBasicsEditorPanel(), GeopediaTerms.INSTANCE.general());
		addWizardStep(stepBasic);
		stepFields = new WizardStep(new TableFieldsEditorPanel(), GeopediaTerms.INSTANCE.fields());
		addWizardStep(stepFields);
		stepStyle = new WizardStep(new TableStyleEditorPanel(), GeopediaTerms.INSTANCE.style());
		selectWizardStep(0);
		
		contentPanel = new FlowPanel();
		notificationPanel = new  NotificationPanel();
		notificationPanel.hide();
//		contentPanel.add(createCloseButton());
		contentPanel.add(new Heading.H1(ProConstants.INSTANCE.tableWizard()));
		contentPanel.add(buttonsHolder);
		contentPanel.add(notificationPanel);
		contentPanel.add(stepsPanel);
		contentPanel.add(stepHolder);
		
		loadingIndicator = new LoadingIndicator(false, false);
		loadingIndicator.setVisible(false);
		contentPanel.add(loadingIndicator);
		
		setWidget(contentPanel);
	}
	
	
	@Override
	protected boolean dialogResizePending(int width, int height) {
		return true;
	}
}
