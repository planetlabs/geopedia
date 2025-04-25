package com.sinergise.geopedia.pro.client.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.sinergise.common.ui.i18n.Buttons;
import com.sinergise.geopedia.core.entities.AbstractEntityWithDescription;
import com.sinergise.geopedia.core.entities.Table;
import com.sinergise.geopedia.light.client.i18n.LightMessages;
import com.sinergise.geopedia.pro.client.ui.table.TableSelectorPanel;
import com.sinergise.geopedia.pro.client.ui.theme.ThemeSelectorPanel;
import com.sinergise.geopedia.pro.theme.GeopediaProStyle;
import com.sinergise.gwt.ui.Heading;
import com.sinergise.gwt.ui.dialog.AbstractDialogBox;
import com.sinergise.gwt.ui.maingui.extwidgets.SGPushButton;
import com.sinergise.gwt.ui.resources.Theme;

public class EntitySelectorDialog<E extends AbstractEntityWithDescription> extends AbstractDialogBox {
	
	private AbstractEntitySelectorPanel<E>  entitySelectorPanel;

	
	public static SGPushButton createOKButton() {
		return new SGPushButton(Buttons.INSTANCE.ok(),Theme.getTheme().standardIcons().save());
	}

	public static SGPushButton createAddButton() {
		return new SGPushButton(Buttons.INSTANCE.add(),Theme.getTheme().standardIcons().plus());
	}

	public static SGPushButton createSelectButton() {
		return new SGPushButton(Buttons.INSTANCE.select(),Theme.getTheme().standardIcons().ok());
	}


	public static class TableSelectorDialog extends EntitySelectorDialog<Table> {
		public TableSelectorDialog() {
			this(createOKButton());
		}
		
		public TableSelectorDialog(SGPushButton applyButton) {
			super(applyButton, new TableSelectorPanel(), LightMessages.INSTANCE.layerSelection());
		}
	}
	
	
	public static class ThemeSelectorDialog extends EntitySelectorDialog<com.sinergise.geopedia.core.entities.Theme> {
		public ThemeSelectorDialog() {
			this(createOKButton());
		}
		
		public ThemeSelectorDialog(SGPushButton applyButton) {
			super(applyButton, new ThemeSelectorPanel(), LightMessages.INSTANCE.themeSelection());
		}
	}
	
	public EntitySelectorDialog(SGPushButton applyButton, AbstractEntitySelectorPanel<E> entitySelectorPanel_, String headingMessage) {
		super(false, true,false,true);
		GeopediaProStyle.INSTANCE.layerSelectionDialog().ensureInjected();
		addStyleName("dialogLayers");
		if (Window.getClientHeight() < 800) {
			setSize("800px", Window.getClientHeight()-150+"px");
		} else {
			setSize("800px", "675px");
		}
		if (Window.getClientWidth() < 700) {
			setWidth(Window.getClientWidth()-50+"px");
		}
		center();
		this.entitySelectorPanel=entitySelectorPanel_;
			
		FlowPanel buttonPanel = new FlowPanel();
		buttonPanel.setStyleName("btnPanel");
		
		applyButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				E entity = entitySelectorPanel.getSelectedEntity();
				if (entity!=null) {
					if (onEntitySelected(entity)) {
						hide();
					}						
				}			
			}
		});
		
		FlowPanel wrapper = new FlowPanel();
		wrapper.setStyleName("wrapper");
		wrapper.add(createCloseButton());
		wrapper.add(new Heading.H1(headingMessage));
		wrapper.add(applyButton);
		wrapper.add(entitySelectorPanel);
		
		setWidget(wrapper);
	}
	
	
	/**
	 * Entity, but not fully loaded!!
	 * @param result
	 * @return
	 */
	protected boolean onEntitySelected(E result) {
		return true;
	}
	
	@Override
	protected boolean dialogResizePending(int width, int height) {
		return true;
	}

}