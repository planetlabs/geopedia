package com.sinergise.geopedia.pro.client.ui.theme;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.util.string.StringUtil;
import com.sinergise.geopedia.core.entities.Permissions;
import com.sinergise.geopedia.core.entities.Theme;
import com.sinergise.geopedia.light.client.i18n.GeopediaTerms;
import com.sinergise.geopedia.pro.client.ui.AbstractEntityEditorPanel;
import com.sinergise.geopedia.pro.client.ui.TinyMCEEditor;
import com.sinergise.gwt.ui.ListBoxExt;
import com.sinergise.gwt.ui.maingui.extwidgets.SGTextBox;

public class ThemeBasicsEditorPanel extends AbstractEntityEditorPanel<Theme> {

	private FlowPanel editorsPanel;
	private SGTextBox tbName;
	private ListBoxExt lbPermissions;
	private TinyMCEEditor mceDescription;
	public ThemeBasicsEditorPanel () {
		addStyleName("general");

		tbName = new SGTextBox();
		FlowPanel nameHolder = createHolderPanel(GeopediaTerms.INSTANCE.name()+":", tbName, true);

		mceDescription = new TinyMCEEditor();
		FlowPanel descriptionHolder = createHolderPanel(GeopediaTerms.INSTANCE.description()+":", mceDescription);		
	
		
		lbPermissions = new ListBoxExt();		
		lbPermissions.addItem(GeopediaTerms.INSTANCE.notAllowed(),String.valueOf(Permissions.NOTHING));
		lbPermissions.addItem(GeopediaTerms.INSTANCE.viewWithLink(),String.valueOf(Permissions.THEME_VIEW));
		lbPermissions.addItem(GeopediaTerms.INSTANCE.onlyView(),String.valueOf(Permissions.THEME_DISCOVER));
		FlowPanel permsHolder = createHolderPanel(GeopediaTerms.INSTANCE.publicPerms()+":", lbPermissions);
		permsHolder.add(lbPermissions);
		
		
		editorsPanel = new FlowPanel();
		editorsPanel.add(nameHolder);
		editorsPanel.add(permsHolder);
		editorsPanel.add(descriptionHolder);
		add(editorsPanel);
	}
	

	@Override
	public void loadEntity(Theme theme) {
		tbName.setText(theme.getName());
		mceDescription.setValue(theme.descRawHtml);
		lbPermissions.setValue(String.valueOf(theme.public_perms));
	}

	@Override
	public boolean saveEntity(Theme entity) {
		entity.setName(tbName.getText());
		entity.public_perms = Integer.valueOf(lbPermissions.getValue());		
		entity.descRawHtml = mceDescription.getValue();
		return true;
	}

	@Override
	public boolean validate() {

		for (int i=0;i<editorsPanel.getWidgetCount();i++) {
			Widget w = editorsPanel.getWidget(i);
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
