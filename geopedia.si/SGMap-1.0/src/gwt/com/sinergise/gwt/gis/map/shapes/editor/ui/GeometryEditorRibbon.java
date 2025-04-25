package com.sinergise.gwt.gis.map.shapes.editor.ui;

import static com.sinergise.gwt.gis.i18n.Messages.UI_MESSAGES;
import static com.sinergise.gwt.gis.map.shapes.editor.GeometryEditorControllerBase.PROP_ACTIVE;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HasOneWidget;
import com.sinergise.common.util.collections.safe.DefaultTypeSafeKey;
import com.sinergise.common.util.state.gwt.PropertyChangeListenerAdapter.PropertyChangeListenerToggleAdapter;
import com.sinergise.gwt.gis.map.DefaultMap;
import com.sinergise.gwt.gis.map.shapes.editor.GeometryEditorControllerBase;
import com.sinergise.gwt.ui.WidgetsRibbon;
import com.sinergise.gwt.ui.maingui.extwidgets.SGPushButton;
import com.sinergise.gwt.ui.resources.Theme;

public class GeometryEditorRibbon extends WidgetsRibbon {
	public static class GeometryEditorRibbonSettings {}
	protected final GeometryEditorControllerBase controller;
	
	protected SGPushButton butConfirm;
	protected SGPushButton butCancel;
	
	protected GeometryEditorRibbonSettings settings;

	public GeometryEditorRibbon(GeometryEditorControllerBase controller, GeometryEditorRibbonSettings settings) {
		this.controller = controller;
		this.settings = settings;
		
		addDefaultWidgets();
	}
	
	public void bindToContainer(HasOneWidget container) {
		bindToContainer(container, PropertyChangeListenerToggleAdapter.bind(controller, 
			new DefaultTypeSafeKey<Boolean>(PROP_ACTIVE), 
			new RibbonVisibilityListener(this, container)));
	}
	
	public void bindToMap(DefaultMap map) {
		bindToContainer(map.getMapRibbonHolder());
	}

	protected void addDefaultWidgets() {
		addWidget(butConfirm = new ConfirmEditingButton(controller));
		addWidget(butCancel = new CancelEditingButton(controller));
	}
	
	static class ConfirmEditingButton extends SGPushButton {
		public ConfirmEditingButton(final GeometryEditorControllerBase controller) {
			super(UI_MESSAGES.geometryEditor_confirm(), Theme.getTheme().standardIcons().ok(), new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					controller.confirm();
				}
			});
		}
	}
	
	static class CancelEditingButton extends SGPushButton {
		public CancelEditingButton(final GeometryEditorControllerBase controller) {
			super(UI_MESSAGES.geometryEditor_cancel(), Theme.getTheme().standardIcons().cancel(), new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					controller.cancel();
				}
			});
		}
	}
}
