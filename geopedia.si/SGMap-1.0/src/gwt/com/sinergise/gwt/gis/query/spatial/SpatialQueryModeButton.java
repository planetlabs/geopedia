package com.sinergise.gwt.gis.query.spatial;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.sinergise.common.ui.action.Action;
import com.sinergise.common.ui.action.ToggleAction;
import com.sinergise.common.util.event.selection.Selectable;
import com.sinergise.gwt.gis.i18n.Tooltips;
import com.sinergise.gwt.gis.query.spatial.SpatialQueryOptions.OptionsChangedListener;
import com.sinergise.gwt.gis.query.spatial.SpatialQueryOptionsMenu.TypeSelectedListener;
import com.sinergise.gwt.ui.ActionPushButton;
import com.sinergise.gwt.ui.ActionToggleButton;

public class SpatialQueryModeButton extends Composite implements OptionsChangedListener{
	
	public static final String STYLE_NAME = "sgwebgis-spatialQueryButton";
	
	private final SpatialQueryMapController control;
	
	private ToggleAction action;
	private ActionToggleButton actionButton;
	private ActionPushButton optionsButton;
	private SpatialQueryOptionsMenu optionsMenu;
	
	private final SpatialQueryOptions options;
	
	public SpatialQueryModeButton(SpatialQueryMapController control) {
		this.control = control;
		
		this.options = control.getDefaultOptions();
		this.optionsMenu = new SpatialQueryOptionsMenu(control.getSupportedSelectionTypes(), options);
		this.optionsMenu.addTypeSelectedListener(new TypeSelectedListener() {
			@Override
			public void onTypeSelected() {
				action.setSelected(true);
			}
		});
		
		actionButton = new ActionToggleButton(action = new ToggleAction("SpatialQueryMode") {
			{
				setDescription(Tooltips.INSTANCE.toolbar_spatialQuery());
			}
			
			@Override
			protected void selectionChanged(boolean newSelected) {
				actionSelected(newSelected);
			}
		});
		actionButton.addStyleName("actionButton");
		
		optionsButton = new ActionPushButton(new Action("SpatialQueryModeOptions") {
			@Override
			protected void actionPerformed() {
				showOptions();
			}
		});
		optionsButton.setTitle(Tooltips.INSTANCE.toolbar_chooseSpatialQuery());
		optionsButton.setStyleName("optionsButton");
		
		options.addOptionsChangedListener(this);
		onOptionsChanged(); //call for initial settings
		
		FlowPanel panel = new FlowPanel();
		panel.add(actionButton);
		panel.add(optionsButton);
		panel.setStyleName("optionsPanel");
		
		initWidget(panel);
		
		addStyleName(STYLE_NAME);
	}
	
	private void actionSelected(boolean selected) {
		if (selected) {
			control.startSelection(options);
		} else {
			control.cancelSelection();
		}
	}
	
	@Override
	public void onOptionsChanged() {
		actionButton.setEnabledIcon(options.getType().getIcon());
		if (actionButton.isDown()) {
			actionSelected(true); //reset if already started
		}
	}
		
	private void showOptions() {
		optionsMenu.showUnder(actionButton);
	}
	
	public Selectable getActionSelectable() {
		return action;
	}
	
}
