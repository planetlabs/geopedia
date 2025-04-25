package com.sinergise.gwt.ui.maingui.tabs;

import static com.sinergise.gwt.ui.maingui.StandardUIConstants.STANDARD_CONSTANTS;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.util.lang.SGAsyncCallback;
import com.sinergise.common.util.messages.MessageType;
import com.sinergise.common.util.naming.EntityIdentifier;
import com.sinergise.common.util.naming.IdentifiableEntity;
import com.sinergise.gwt.ui.dialog.MessageDialog;
import com.sinergise.gwt.ui.maingui.extwidgets.SGCloseableTab;
import com.sinergise.gwt.ui.maingui.extwidgets.SGTabLayoutPanel;

public abstract class IdentifiableEntityTabsController<T extends IdentifiableEntity> extends TabVisibilityController<IdentifiableEntityTab<T>> {

	private final Logger logger = LoggerFactory.getLogger(IdentifiableEntityTabsController.class);
	
	private final SGTabLayoutPanel 	tabPanel;
	
	public IdentifiableEntityTabsController(SGTabLayoutPanel tabPanel) {
		this.tabPanel = tabPanel;
	}
	
	public void openEntityTab(final EntityIdentifier id, final BeforeOpenningTabCallback<T> beforeOpenCB) {
		if (id == null) {
			return; //nothing to open
		}
		
		//check if tab already on the tab panel
		IdentifiableEntityTab<T> tab = findEntityTab(id);
		if (tab != null) {
			beforeOpenCB.beforeOpenningTab(tab);
			tab.ensureVisible();
			
		} else { //fetch data and add new tab if not
			loadEntity(id, new SGAsyncCallback<T>() {
				@Override
				public void onSuccess(T entity) {
					IdentifiableEntityTab<T> newTab = addEntityTab(entity);
					beforeOpenCB.beforeOpenningTab(newTab);
					newTab.ensureVisible();
				}
				
				@Override
				public void onFailure(Throwable caught) {
					logger.error(caught.getMessage(), caught);
					MessageDialog.createMessage(STANDARD_CONSTANTS.error(), caught.getMessage(), false, MessageType.ERROR).showCentered();
				}
			});
		}
	}
	
	@SuppressWarnings("unchecked")
	private IdentifiableEntityTab<T> findEntityTab(EntityIdentifier id) {
		for (Widget w : tabPanel) {
			if (w instanceof IdentifiableEntityTab && ((IdentifiableEntityTab<T>) w).getEntityId().equals(id)) {
				return (IdentifiableEntityTab<T>) w;
			}
		}
		return null;
	}
	
	private IdentifiableEntityTab<T> addEntityTab(T entity) {
		IdentifiableEntityTab<T> tab = createEntityTab(entity);
		
		tabPanel.add(tab, new SGCloseableTab(tabPanel, tab, tab.getTabTitle()));
		registerDeepVisibilityChangeListener(tab);
		return tab;
	}
	
	protected abstract IdentifiableEntityTab<T> createEntityTab(T entity);
	
	protected abstract void loadEntity(EntityIdentifier id, SGAsyncCallback<T> callback);
	
	public interface BeforeOpenningTabCallback<T extends IdentifiableEntity> {
		void beforeOpenningTab(IdentifiableEntityTab<T> tab);
	}
}
