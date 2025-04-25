package com.sinergise.gwt.gis.query.spatial;

import static com.sinergise.gwt.gis.i18n.FilterCapabilityNames.OPERATION_PREFIX;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.sinergise.common.gis.filter.FilterCapabilities;
import com.sinergise.common.util.event.selection.ExcludeContext;
import com.sinergise.common.util.event.selection.SourcesToggleEvents;
import com.sinergise.common.util.event.selection.ToggleListener;
import com.sinergise.gwt.gis.i18n.FilterCapabilityNames;
import com.sinergise.gwt.gis.i18n.Labels;
import com.sinergise.gwt.gis.query.spatial.SpatialQueryOptions.OptionsChangedListener;
import com.sinergise.gwt.ui.Spinner;
import com.sinergise.gwt.ui.menu.SGMenu;
import com.sinergise.gwt.ui.menu.SGMenuItem;
import com.sinergise.gwt.ui.menu.SGSelectableMenuItem;

public class SpatialQueryOptionsMenu extends SGMenu {
	
	public interface TypeSelectedListener {
		void onTypeSelected();
	}
	
	private final SpatialQueryOptions options;
	
	ExcludeContext selectionTypeContext = new ExcludeContext();
	ExcludeContext operationContext = new ExcludeContext();
	
	private final List<TypeSelectedListener> typeSelectiedListeners = new ArrayList<SpatialQueryOptionsMenu.TypeSelectedListener>();
	
	public SpatialQueryOptionsMenu(EnumSet<SpatialQuerySelectionType> supportedTypes, SpatialQueryOptions options) {
		this.options = options;
		
		selectionTypeContext.setForceSelection(true);
		operationContext.setForceSelection(true);
		
		int operations = 0;
		
		for (SpatialQuerySelectionType type : supportedTypes) {
	    	addSelectionType(type);
	    	operations |= type.getSupportedOperations();
	    }
		
		addSeparator();
		addItem(createOperationsMenu(operations));
		
		options.addOptionsChangedListener(new OptionsChangedListener() {
			@Override
			public void onOptionsChanged() {
				updateUI();
			}
		});
		updateUI();
	}
	
	private void addSelectionType(final SpatialQuerySelectionType type) {
		
		final Spinner bufferSpinner = new Spinner(0, Double.MAX_VALUE, 1);
		
		
		SelectionTypeItem item = new SelectionTypeItem(type);
		typeItems.add(item);
		selectionTypeContext.register(item);
		
		//if supports buffer
		if ((type.getSupportedFunctions() & FilterCapabilities.SPATIAL_FUNCT_BUFFER) > 0) {
			bufferSpinner.setValue(0);
			bufferSpinner.addStyleName("option-buffer");
			
			HorizontalPanel spinnerPanel = new HorizontalPanel();
			spinnerPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
			spinnerPanel.addStyleName("option-buffer-panel");
			spinnerPanel.add(new Label("Buffer:"));
			spinnerPanel.add(bufferSpinner);
			spinnerPanel.add(new Label(" m"));
			
			SGMenu bufferMenu = new SGMenu();
			SGMenuItem bufferMenuItem = new SGMenuItem(spinnerPanel);
			bufferMenuItem.addStyleName("nonselectable");
			bufferMenu.addItem(bufferMenuItem);
			item.setSubMenu(bufferMenu);
			
			bufferSpinner.addValueChangeHandler(new ValueChangeHandler<Double>() {
				@Override
				public void onValueChange(ValueChangeEvent<Double> event) {
					options.setBuffer(event.getValue().doubleValue());
				}
			});
		}
		
		addItem(item);
	}
	
	private SGMenuItem createOperationsMenu(int operations) {
		SGMenuItem opItem = new SGMenuItem(new Label(Labels.INSTANCE.spatialQuery_operations()));
		SGMenu opMenu = new SGMenu();
		for (int op : FilterCapabilities.splitMaskToOps(operations)) {
			opMenu.addItem(createOperationItem(op, FilterCapabilityNames.INSTANCE.getString(OPERATION_PREFIX+op)));
		}
		
		opItem.setSubMenu(opMenu);
		return opItem;
	}
	
	private List<OperationsItem> opItems = new ArrayList<OperationsItem>();
	private List<SelectionTypeItem> typeItems = new ArrayList<SelectionTypeItem>();
	
	private SGMenuItem createOperationItem(final int op, String name) {
		OperationsItem opItem = new OperationsItem(op, name);
		opItems.add(opItem);
		operationContext.register(opItem);
		return opItem;
	}
	
	private void updateUI() {
		for (OperationsItem opItem : opItems) {
			opItem.setEnabled((options.getType().getSupportedOperations() & opItem.operation) > 0);
			if (opItem.operation == options.getOperation()) {
				opItem.setSelected(true);
			}
		}
		
		for (SelectionTypeItem typeItem : typeItems) {
			if (typeItem.type == options.getType()) {
				typeItem.setSelected(true);
			}
		}
	}
	
	public void addTypeSelectedListener(TypeSelectedListener listener) {
		typeSelectiedListeners.add(listener);
	}
	
	public void removeTypeSelectedListener(TypeSelectedListener listener) {
		typeSelectiedListeners.remove(listener);
	}
	
	private void fireTypeSelected() {
		for (TypeSelectedListener listener : typeSelectiedListeners) {
			listener.onTypeSelected();
		}
	}
	
	private class OperationsItem extends SGSelectableMenuItem {
		final int operation;
		OperationsItem(final int operation, String name) {
			super(new Label(name));
			this.operation = operation;
			addToggleListener(new ToggleListener() {
				@Override
				public void toggleStateChanged(SourcesToggleEvents source, boolean newOn) {
					if (newOn) options.setOperation(operation);
				}
			});
		}
	}
	
	private class SelectionTypeItem extends SGSelectableMenuItem {
		final SpatialQuerySelectionType type;
		SelectionTypeItem(final SpatialQuerySelectionType type) {
			super(new Label(type.getTypeName()));
			
			setSelectedIcon(type.getIcon());
			setDeselectedIcon(type.getIcon());
			
			addStyleName(SpatialQueryModeButton.STYLE_NAME+"-options-type");
			this.type = type;
			
			addToggleListener(new ToggleListener() {
				@Override
				public void toggleStateChanged(SourcesToggleEvents source, boolean newOn) {
					if (newOn) {
						options.setType(type);
						fireTypeSelected();
					}
				}
			});
			
			super.updateUI();
		}
	}
	
}