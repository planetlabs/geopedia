package com.sinergise.gwt.gis.map.ui.attributes.edit;


import static com.google.gwt.user.client.ui.HasHorizontalAlignment.ALIGN_LEFT;
import static com.sinergise.gwt.gis.i18n.Messages.UI_MESSAGES;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.gis.feature.CFeature;
import com.sinergise.common.gis.feature.CFeatureCollection;
import com.sinergise.common.gis.feature.CFeatureUtils;
import com.sinergise.common.gis.feature.CFeatureUtils.PropertyDisplayData;
import com.sinergise.common.gis.feature.HasFeatureRepresentations;
import com.sinergise.common.gis.map.model.layer.info.FeatureInfoItem;
import com.sinergise.common.ui.action.Action;
import com.sinergise.common.util.lang.SGAsyncCallback;
import com.sinergise.common.util.state.gwt.PropertyChangeListener;
import com.sinergise.gwt.gis.map.ui.MapComponent;
import com.sinergise.gwt.gis.map.ui.actions.HighlightFeaturesToggleAction;
import com.sinergise.gwt.gis.map.ui.attributes.CombinedFeatureActionsProvider;
import com.sinergise.gwt.gis.map.ui.attributes.FeatureActionsProvider;
import com.sinergise.gwt.gis.map.ui.attributes.FeatureAttrDetailsTab;
import com.sinergise.gwt.gis.map.ui.attributes.PropertyWidgetFactory;
import com.sinergise.gwt.gis.map.ui.attributes.TabbedAttributesPanel;
import com.sinergise.gwt.gis.map.ui.attributes.TabbedAttributesPanel.FeatureAttrDetailsTabDecorator;
import com.sinergise.gwt.gis.map.ui.attributes.WriteablePropertyWidgetFactory;
import com.sinergise.gwt.gis.map.ui.attributes.WriteablePropertyWidgetFactory.PropertyEditorWithNotification;
import com.sinergise.gwt.ui.NotificationPanel;
import com.sinergise.gwt.ui.maingui.extwidgets.SGPushButton;
import com.sinergise.gwt.ui.resources.Theme;
import com.sinergise.gwt.ui.table.FlexTableBuilder;

public class FeatureAttrDetailsTabWithEdit extends FeatureAttrDetailsTab {
	private SGPushButton confirm;
	private SGPushButton cancel;
	
	private FeatureInfoItem originalItem;
	private Widget headerWidget = null;
	private NotificationPanel notifications = new NotificationPanel();
	private AttributeEditorController editorControl;
	private PropertyWidgetFactory editablePropWgtFact = new WriteablePropertyWidgetFactory();
	
	private List<PropertyEditorWithNotification> widgets = new ArrayList<PropertyEditorWithNotification>();

	public FeatureAttrDetailsTabWithEdit(MapComponent map, TabbedAttributesPanel results,
		CombinedFeatureActionsProvider actionsProvider, FeatureAttrDetailsTabDecorator decorator, AttributeEditorController control) {
		super(map, results, actionsProvider = new CombinedFeatureActionsProvider(actionsProvider), decorator);
		this.editorControl = control;

        actionsProvider.registerProvider(new FeatureActionsProvider() {
            @Override
            public List<? extends Action> getFeatureActions(HasFeatureRepresentations fRep, Object requestor) {
				if (editorControl != null && CFeatureUtils.hasOnlyThisCFeature(fRep, item.f)) {
                    return getFeatureEditorActions();
                }
                return Collections.emptyList();
            }
        }); 
        
        editorControl.addPropertyChangeListener(activeListner);
		

		add(notifications);
	}
	
	private PropertyChangeListener<Object> activeListner = new PropertyChangeListener<Object>() {
		@Override
		public void propertyChange(Object sender, String propertyName, Object oldValue, Object newValue) {
			if(AttributeEditorController.PROP_ACTIVE.equals(propertyName) && newValue instanceof Boolean) {
				render();
			}
		}
	};
	
	private List<? extends Action> getFeatureEditorActions() {
		ArrayList<Action> actionList = new ArrayList<Action>();
		actionList.add(new EditAttributesAction(editorControl, this));
		return actionList;
	}


	public void turnFeatueHighlightOn(boolean isOn) {
		List<? extends Action> actionList = actionsProvider.getFeatureActions(new CFeatureCollection(item.f), this);
		for (int i = 0; i < actionList.size(); i++) {
			Action action = actionList.get(i);
			if (action instanceof HighlightFeaturesToggleAction) {
				if (((HighlightFeaturesToggleAction) action).isSelected() != isOn) {
					action.performAction();
				}
			}
		}
	}

	public void editFeature() {
		try {
			copyFeature();
			editorControl.editFeatureAttibutes(item.f, createEditCallBack());
		} catch (Exception e) {
			notifications.showErrorMsg("Error while starting editing feature  " + item.f.getIdentifier(), false);
		}
	}

	private AttributeEditorCallback createEditCallBack() {

		return new AttributeEditorCallback() {
			@Override
			public void onConfirm(CFeature newFeature, SGAsyncCallback<Void> confirmCallback) {
				if (newFeature == null) {
					return;
				}
				editorControl.doSave(confirmCallback);
			}

			@Override
			public void onCancel() {
				onCancelEdit();
			}
		};
	}
	
	protected final SGAsyncCallback<Void> confirmationCallback = new SGAsyncCallback<Void>() {
		@Override
		public void onSuccess(Void result) {}
		
		@Override
		public void onFailure(Throwable caught) {
			notifications.showErrorMsg("Error while editing feature  " + item.f.getIdentifier(), false);
		}
	};
	
	public void onCancelEdit() {
		notifications.hide();
		if (originalItem != null) {
			item = originalItem;
		}
		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
			@Override
			public void execute() {
				map.repaint(100);
				if(isAttached()){
					render();
				}
			}
		});
	}
	
	private void copyFeature() {
		originalItem = new FeatureInfoItem(new CFeature(item.f.getDescriptor()), item.layerName);
		CFeatureUtils.copy(item.f, originalItem.f);
		originalItem.f.setID(item.f.getIdentifier());
	}
	
	@Override
	public void render() {
		widgets.clear();
		container.clear();
		container.add(notifications);
		notifications.setVisible(notifications.getMessageType() != null);
		container.add(buildDetailsWidget());
	}


	@Override
	protected void onDetach() {
		originalItem = null;
		if(editorControl.isEditing(item.f)){
			editorControl.cancel();
		}
		editorControl.removePropertyChangeListener(activeListner);
		super.onDetach();
	}

	@Override
	protected void buildDetailsTable(final FlexTableBuilder ftb) {
		if (headerWidget == null) {
			headerWidget = buildHeaderWidget();
		}
		ftb.addTitleWidget(headerWidget);
		ftb.setCurrentCellColSpan(2);
		ftb.newRow();
		
		if (inEditMode()) {
			addEditOkCancelButton(ftb);
		}
		
		fillPropertiesTable(item.f, ftb);
	}
	
	@Override
	protected PropertyWidgetFactory getPropertyWidgetFactory() {
		if (inEditMode()) {
			return editablePropWgtFact;
		}
		return super.getPropertyWidgetFactory();
	}
	
	@Override
	protected void addPropertyRow(PropertyDisplayData<?> att, FlexTableBuilder ftb) {
		PropertyWidgetFactory factory = getPropertyWidgetFactory();
		ftb.addFieldLabelWidget(factory.createLabelWidget(att)).setCurrentCellHAlign(ALIGN_LEFT);
		Widget valueWidget = factory.createValueWidget(att);
		ftb.addFieldValueWidget(valueWidget).setCurrentCellWidth("100%");
		if(valueWidget instanceof PropertyEditorWithNotification) {
			widgets.add((PropertyEditorWithNotification)valueWidget);
		}		
		ftb.newRow();
	}

	private boolean inEditMode() {
		return editorControl != null && item != null && editorControl.isEditing(item.f);
	}

	private void addEditOkCancelButton(FlexTableBuilder ftb) {
		HorizontalPanel hp = new HorizontalPanel();
		hp.add(getConfirmButton());
		hp.add(getCancelButton());
		ftb.addFieldLabelWidget(hp);
		ftb.setCurrentCellColSpan(2);
		ftb.newRow();
	}

	private Widget getCancelButton() {
		if (cancel == null) {
			cancel = new SGPushButton(UI_MESSAGES.geometryEditor_cancel(), Theme.getTheme().standardIcons().cancel(), new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					editorControl.cancel();
				}
			});
		}
		return cancel;
	}
	


	private Widget getConfirmButton() {
		if(confirm == null) {
			confirm = new SGPushButton(UI_MESSAGES.geometryEditor_confirm(), Theme.getTheme().standardIcons().ok(), new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					if(validate()) {
						editorControl.confirm(confirmationCallback);
					}
				}
			});
		}
		return confirm;
		
	}
	
	protected boolean validate() {
		boolean ret = true;
		for (PropertyEditorWithNotification widget : widgets) {
			boolean temp = widget.validate();
			ret = temp && ret;
		}
		return ret;
	}
}