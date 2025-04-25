package com.sinergise.geopedia.pro.client.ui.table;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.util.string.StringUtil;
import com.sinergise.geopedia.core.entities.GeomType;
import com.sinergise.geopedia.core.entities.Permissions;
import com.sinergise.geopedia.core.entities.Table;
import com.sinergise.geopedia.light.client.i18n.GeopediaTerms;
import com.sinergise.geopedia.pro.client.ui.AbstractEntityEditorPanel;
import com.sinergise.geopedia.pro.client.ui.TinyMCEEditor;
import com.sinergise.gwt.ui.ListBoxExt;
import com.sinergise.gwt.ui.maingui.extwidgets.SGTextBox;

public class TableBasicsEditorPanel extends AbstractEntityEditorPanel<Table> {

	private Table table;
	private SGTextBox tbName;
	private TinyMCEEditor mceDescription;
	private ListBoxExt lbGeometryType;
	private ListBoxExt lbPermissions;
	private FlowPanel basePanel;
	public TableBasicsEditorPanel () {
		basePanel = new FlowPanel();
		add(basePanel);
		addStyleName("general");
		
		tbName = new SGTextBox();
		tbName.setVisibleLength(70);		
		FlowPanel nameHolder = createHolderPanel(GeopediaTerms.INSTANCE.name()+":", tbName, true);
	
		mceDescription = new TinyMCEEditor();
		FlowPanel descriptionHolder = createHolderPanel(GeopediaTerms.INSTANCE.description()+":",mceDescription);		
		

		lbGeometryType = new ListBoxExt();
		for (GeomType gt:GeomType.values()) {
			switch(gt) {
				case NONE:
					lbGeometryType.addItem(GeopediaTerms.INSTANCE.codelist(), String.valueOf(gt.getIdentifier()));
				break;
				case POINTS:
					lbGeometryType.addItem(GeopediaTerms.INSTANCE.points(), String.valueOf(gt.getIdentifier()));
				break;
				case POINTS_M:
					lbGeometryType.addItem(GeopediaTerms.INSTANCE.multiPoints(), String.valueOf(gt.getIdentifier()));
				break;
				case POLYGONS:
					lbGeometryType.addItem(GeopediaTerms.INSTANCE.polygons(), String.valueOf(gt.getIdentifier()));
				break;
				case POLYGONS_M:
					lbGeometryType.addItem(GeopediaTerms.INSTANCE.multiPolygons(), String.valueOf(gt.getIdentifier()));
				break;
				case LINES:
					lbGeometryType.addItem(GeopediaTerms.INSTANCE.lines(), String.valueOf(gt.getIdentifier()));
				break;
				case LINES_M:
					lbGeometryType.addItem(GeopediaTerms.INSTANCE.multiLines(), String.valueOf(gt.getIdentifier()));
				break;

			}
		}
		lbGeometryType.addValueChangeHandler(new ValueChangeHandler<String>() {
			
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				if (!table.hasValidId()) { // new table
					table.setGeomType(getGeomType());
				}
				
			}
		});
		
		
		FlowPanel typeHolder = createHolderPanel(GeopediaTerms.INSTANCE.geometry()+":", lbGeometryType);

		
		
		// TODO: this will be overhauled when permissions are overhauled
		lbPermissions = new ListBoxExt();		
		lbPermissions.addItem(GeopediaTerms.INSTANCE.notAllowed(),String.valueOf(Permissions.NOTHING));
		lbPermissions.addItem(GeopediaTerms.INSTANCE.viewWithLink(),String.valueOf(Permissions.TABLE_VIEW));
		lbPermissions.addItem(GeopediaTerms.INSTANCE.onlyView(),String.valueOf(Permissions.TABLE_DISCOVER));
		lbPermissions.addItem(GeopediaTerms.INSTANCE.editData(),String.valueOf(Permissions.TABLE_EDITDATA));
		FlowPanel permsHolder = createHolderPanel(GeopediaTerms.INSTANCE.publicPerms()+":", lbPermissions);
		permsHolder.add(lbPermissions);
		
		
		basePanel.add(nameHolder);
		basePanel.add(typeHolder);		
		basePanel.add(permsHolder);
		basePanel.add(descriptionHolder);
	}
	

	private GeomType getGeomType () {
		return GeomType.forId(Integer.valueOf(lbGeometryType.getValue()));
	}
	@Override
	public void loadEntity(Table table) {
		this.table = table;
		tbName.setText(table.getName());
		mceDescription.setValue(table.descRawHtml);
		lbGeometryType.setValue(String.valueOf(table.geomType.getIdentifier()));
		lbPermissions.setValue(String.valueOf(table.public_perms));		
		if (table.hasValidId()) { // updating existing
			lbGeometryType.setEnabled(false);
		} else { // new 
			lbGeometryType.setEnabled(true);
		}
	}

	@Override
	public boolean saveEntity(Table entity) {		
		entity.setName(tbName.getText());
		entity.descRawHtml = mceDescription.getValue();
		entity.setGeomType(getGeomType());
		entity.public_perms = Integer.valueOf(lbPermissions.getValue());
		return true;
	}

	@Override
	public boolean validate() {
		
		for (int i=0;i<basePanel.getWidgetCount();i++) {
			Widget w = basePanel.getWidget(i);
			if (w instanceof HolderPanel) {
				((HolderPanel)w).missingMandatory(false);
			}
		}
		// validate name
		String name = tbName.getValue();
		if (StringUtil.isNullOrEmpty(name)) {
			HolderPanel holderPanel = (HolderPanel)tbName.getParent();
			holderPanel.missingMandatory(true);
			return false;
		}
		return true;
	}
}
