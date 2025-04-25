package com.sinergise.gwt.ui.menu;

import static com.sinergise.gwt.ui.menu.SGMenuItem.ICON;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.UIObject;
import com.sinergise.common.util.state.gwt.PropertyChangeListener;

public class SGMenu extends PopupPanel {
	
	public static final String STYLE_NAME = "sgwebui-menu";

	protected List<SGMenuItem> items = new ArrayList<SGMenuItem>();
	
	private final PropertyChangeListener<Object> itemPropertyChangeListener = new PropertyChangeListener<Object>() {
		@Override
		public void propertyChange(Object sender, String propertyName, Object oldValue, Object newValue) {
			if (ICON.equals(propertyName)) {
				updateUI();
			}
		}
	};
	
	public SGMenu() {
		super(true, false);
		setStyleName(STYLE_NAME);
	}
	
	private void updateUI() {
		FlowPanel ftb = new FlowPanel();
		
		for (final SGMenuItem item : items) {
			if (!item.isVisible()) {
				continue;
			}
			
			FlowPanel itemCont = new FlowPanel();
			itemCont.setStyleName("itemCont");
			
			if (item.getItemIcon() != null) {
				itemCont.addStyleName("withIcon");
				itemCont.add(item.getItemIcon());
			}
			itemCont.add(item);
			itemCont.addDomHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					item.onClick();
				}
			}, ClickEvent.getType());
			
			itemCont.addDomHandler(new MouseOverHandler() {
				public void onMouseOver(MouseOverEvent event) {
					item.onMouseOver();
				}
			}, MouseOverEvent.getType());
			
			if(!item.isSelectable()) {
				itemCont.addStyleName("separator");
			}
			
			ftb.add(itemCont);
		}
		
		setWidget(ftb);
	}
	
	public void addItem(SGMenuItem item) {
		items.add(item);
		item.setParentMenu(this);
		item.addPropertyChangeListener(itemPropertyChangeListener);
		structureChanged();
	}
	
	@Override
	protected void onAttach() {
		super.onAttach();
		updateUI();
	}
	
	public void addSeparator() {
		addItem(new Separator());
		structureChanged();
	}
	
	void menuItemSelectionChanged(SGMenuItem selected) {
		for (SGMenuItem item : items) {
			item.menuItemSelectionChanged(selected);
		}
	}
	
	void structureChanged() {
		if (isAttached()) {
			updateUI();
		}
	}
	
	public List<SGMenuItem> getItems() {
		return Collections.unmodifiableList(items);
	}
	
	@Override
	public void hide(boolean autoClosed) {
		for (SGMenuItem item : getItems()) {
			if (item.submenu != null) {
				item.submenu.hide();
			}
		}
		super.hide(autoClosed);
	}
	
	public void showUnder(final UIObject target) {
		setPopupPositionAndShow(new PositionCallback() {
			public void setPosition(int offsetWidth, int offsetHeight) {
				int top = target.getAbsoluteTop() + target.getOffsetHeight();
				int left = target.getAbsoluteLeft();
				
				//show left if no space on the right
				if (left + getOffsetWidth() > Window.getClientWidth()) {
					left = target.getAbsoluteLeft() + target.getOffsetWidth() - offsetWidth;
				}
				
				//show above if no space under
				if (top + getOffsetHeight() > Window.getClientHeight()) {
					top = target.getAbsoluteTop() - offsetHeight ;
				}
				
				setPopupPosition(left, top);
			}
		});
	}
	
	public void showNextTo(final UIObject target) {
		setPopupPositionAndShow(new PositionCallback() {
			public void setPosition(int offsetWidth, int offsetHeight) {
				int top = target.getAbsoluteTop();
				
				int left = target.getAbsoluteLeft()+target.getOffsetWidth();
				
				//show left if no space on the right
				if (left + getOffsetWidth() > Window.getClientWidth()) {
					left = target.getAbsoluteLeft() - offsetWidth;
				}
				
				//show above if no space under
				if (top + getOffsetHeight() > Window.getClientHeight()) {
					top = target.getAbsoluteTop() + target.getOffsetHeight() - offsetHeight ;
				}
				
				setPopupPosition(left, top);
			}
		});
	}
	
	
	private static final class Separator extends SGMenuItem {
		Separator() {
			super(new SimplePanel());
		}
		
		@Override
		protected boolean isSelectable() {
			return false;
		}
	}
}
