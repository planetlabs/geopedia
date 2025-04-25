package com.sinergise.gwt.ui.menu;

import static com.sinergise.common.ui.action.UIObjectInfo.ENABLED;
import static com.sinergise.common.ui.action.UIObjectInfo.VISIBLE;

import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.util.state.gwt.PropertyChangeListener;
import com.sinergise.common.util.state.gwt.PropertyChangeListenerCollection;
import com.sinergise.common.util.state.gwt.SourcesPropertyChangeEvents;

public class SGMenuItem extends Composite implements SourcesPropertyChangeEvents<Object> {
	
	public static final String ICON = "icon";
	
	public static final String STYLE_NAME = "item";

	protected SGMenu parent;
	protected SGMenu submenu;
	
	protected ComplexPanel pContent;
	protected Widget wExpand = null;
	protected Image icon = null;
	
	private boolean enabled = true;
	private boolean visible = true;
	
	private PropertyChangeListenerCollection<Object> propListeners = new PropertyChangeListenerCollection<Object>();
	
	public SGMenuItem(Widget itemWidget, SGMenu submenu) {
		this(itemWidget);
		setSubMenu(submenu);
	}
	
	public SGMenuItem(Widget itemWidget) {
		pContent = new FlowPanel();
		itemWidget.setStyleName("label");
		pContent.add(itemWidget);
		
		initWidget(pContent);
		
		setStyleName(STYLE_NAME);
	}
	
	public void setSubMenu(SGMenu submenu) {
		//if removing submenu
		if (submenu == null && wExpand != null) {
			pContent.remove(wExpand);
		}
		
		this.submenu = submenu;
		if (submenu != null) {
			pContent.addStyleName("expanded");
		}
	}
	
	public void setItemIcon(Image icon) {
		Image prev = this.icon;
		
		this.icon = icon;
		this.icon.setStyleName("itemImg");
		
		propListeners.fireChange(this, ICON, prev, icon);
	}
	
	public Image getItemIcon() {
		return icon;
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public void setEnabled(boolean enabled) {
		boolean prev = this.enabled;
		
		this.enabled = enabled;
		updateUI();
		
		propListeners.fireChange(this, ENABLED, Boolean.valueOf(prev), Boolean.valueOf(enabled));
	}
	
	@Override
	public boolean isVisible() {
		return visible;
	}
	
	@Override
	public void setVisible(boolean visible) {
		boolean oldVisible = this.visible;
		this.visible = visible;
		
		if (parent != null && visible != oldVisible) {
			parent.structureChanged();
		}
		
		propListeners.fireChange(this, VISIBLE, Boolean.valueOf(oldVisible), Boolean.valueOf(visible));
	}
	
	protected void onMouseOver() {
		//notify selection
		if (parent != null) {
			parent.menuItemSelectionChanged(this);
		}
		
		showSubmenu();
	}
	
	protected void onClick() {
		
	}
	
	void menuItemSelectionChanged(SGMenuItem selected) {
		if (selected != this && submenu != null) {
			submenu.hide(true); //if other item was selected
		}
	}
	
	void setParentMenu(SGMenu parent) {
		if (this.parent != null && this.parent != parent) {
			throw new IllegalStateException("Different parent menu already set");
		}
		this.parent = parent;
	}
	
	protected void showSubmenu() {
		if (submenu != null) {
			submenu.showNextTo(this);
		}
	}
	
	protected boolean isSelectable() {
		return true;
	}
	
	protected void updateUI() {
		if (!enabled) {
			addStyleName("disabled");
		} else {
			removeStyleName("disabled");
		}
	}
	
	@Override
	public void addPropertyChangeListener(PropertyChangeListener<Object> listener) {
		propListeners.add(listener);
	}
	
	@Override
	public void removePropertyChangeListener(PropertyChangeListener<Object> listener) {
		propListeners.remove(listener);
	}
	
}
