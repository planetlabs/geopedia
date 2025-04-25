package com.sinergise.gwt.gis.map.ui.basket;

import com.google.gwt.user.client.ui.Widget;
import com.sinergise.gwt.gis.map.ui.attributes.CombinedFeatureActionsProvider;
import com.sinergise.gwt.gis.map.ui.attributes.FeatureActionsProvider;
import com.sinergise.gwt.gis.map.ui.basket.FeatureBasketRegistry.FeatureBasketRegistryListener;
import com.sinergise.gwt.gis.map.ui.basket.action.NewFeatureBasketAction;
import com.sinergise.gwt.gis.map.ui.basket.res.FeatureBasketResources;
import com.sinergise.gwt.ui.ActionSelectionButton;
import com.sinergise.gwt.ui.maingui.extwidgets.SGCloseableTab;
import com.sinergise.gwt.ui.maingui.extwidgets.SGTabLayoutPanel;

public class FeatureBasketsTabbedPane extends SGTabLayoutPanel {

	private final FeatureBasketRegistry basketsRegistry;
	protected final CombinedFeatureActionsProvider actionsProvider = new CombinedFeatureActionsProvider();
	
	public FeatureBasketsTabbedPane(FeatureBasketRegistry registry, FeatureActionsProvider actions) {
		this(registry);
		actionsProvider.registerProvider(0, actions);
	}
	
	public FeatureBasketsTabbedPane(FeatureBasketRegistry registry) {
		this.basketsRegistry = registry;
		
		setSubmenu();
		init();
		render();
	}
	
	private void init() {
		FeatureBasketResources.BASKET_RESOURCES.basketStyle().ensureInjected();
		
		addTabBarTailWidget(new ActionSelectionButton(
			new NewFeatureBasketAction(basketsRegistry, basketsRegistry.getSupportedLayers())){
			{
				addStyleName(FeatureBasketResources.BASKET_RESOURCES.basketStyle().newBasketTabButton());
			}
		});
		
		basketsRegistry.addListener(new FeatureBasketRegistryListener() {
			
			@Override
			public void onBasketAdded(FeatureBasket basket) {
				if (!hasBasketTab(basket)) {
					addBasketTab(basket).ensureVisible();
				}
			}
			
			@Override
			public void onBasketRemoved(FeatureBasket basket) {
				removeBasketTab(basket);
			}
		});
	}
	
	public CombinedFeatureActionsProvider getActionsProvider() {
		return actionsProvider;
	}
	
	protected FeatureBasketTab addBasketTab(final FeatureBasket basket) {
		FeatureBasketTab tab = createBasketTab(basket);
		add(tab, new SGCloseableTab(this, tab, tab.getTabTitle()) {
			@Override
			protected void onClose() {
				basketsRegistry.removeBasket(basket);
			}
		});
		return tab;
	}
	
	protected FeatureBasketTab createBasketTab(FeatureBasket basket) {
		return new FeatureBasketTab(basket, actionsProvider);
	}
	
	protected void removeBasketTab(FeatureBasket basket) {
		for (Widget w : this) {
			if (isBasketTab(w, basket)) {
				remove(w);
				break;
			}
		}
	}
	
	protected final boolean hasBasketTab(FeatureBasket basket) {
		for (Widget w : this) {
			if (isBasketTab(w, basket)) {
				return true;
			}
		}
		
		return false;
	}
	
	private void render() {
		clear();
		for (FeatureBasket basket : basketsRegistry.listBaskets()) {
			addBasketTab(basket);
		}
	}
	
	protected static boolean isBasketTab(Widget w, FeatureBasket basket) {
		return w instanceof FeatureBasketTab
			&& ((FeatureBasketTab)w).getBasket().equals(basket);
	}
	
}
