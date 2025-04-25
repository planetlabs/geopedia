package com.sinergise.gwt.ui.menu;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.ui.action.Action;
import com.sinergise.common.util.event.selection.Selectable;
import com.sinergise.common.util.event.selection.ToggleListener;
import com.sinergise.common.util.event.selection.ToggleListenerCollection;
import com.sinergise.gwt.ui.resources.Theme;

public class SGSelectableMenuItem extends SGActionMenuItem implements Selectable {
	
	private boolean selected = false;
	
	private ImageResource selectedIcon = Theme.getTheme().standardIcons().check();
	private ImageResource dummyIcon = Theme.getTheme().standardIcons().dummy();
	
	protected ToggleListenerCollection tlc = new ToggleListenerCollection();
	
	public SGSelectableMenuItem(Widget itemWidget, ToggleListener listener) {
		this(itemWidget);
		addToggleListener(listener);
	}
	
	public SGSelectableMenuItem(Widget itemWidget) {
		super(null, itemWidget);
		
		setAction(new Action("selectionAction") {
			@Override
			protected void actionPerformed() {
				if (SGSelectableMenuItem.this.isEnabled()) {
					setSelected(!isSelected());
				}
			}
		});
		
		icon = new Image(dummyIcon);
		icon.setStyleName("itemImg");
	}
	
	public boolean isSelected() {
		return selected;
	}
	
	@Override
	protected boolean shouldHideParentOnAction() {
		return false;
	}
	
	public void setSelected(boolean selected) {
		if (this.selected == selected) {
			return;
		}
		
		this.selected = selected;
		updateUI();
		tlc.fireActionPerformed(this, !selected, selected);
	}
	
	public void addToggleListener(ToggleListener listener) {
		tlc.add(listener);
	}
	
	public void removeToggleListener(ToggleListener listener) {
		tlc.remove(listener);
	}
	
	@Override
	protected void updateUI() {
		super.updateUI();
		
		if (selected) {
			addStyleName("selected");
		} else {
			removeStyleName("selected");
		}
		
		if (icon != null) {
			icon.setResource(selected ? selectedIcon : dummyIcon);
		} else {
			addStyleName(selected ? "iconSelect" : "iconDeselect");
		}
	}
	
	public void setSelectedIcon(ImageResource icon) {
		selectedIcon = icon;
	}
	
	public void setDeselectedIcon(ImageResource icon) {
		dummyIcon = icon;
	}
	
}
