package com.sinergise.gwt.gis.map.ui.basket;

import static com.google.gwt.user.client.ui.HasHorizontalAlignment.ALIGN_RIGHT;
import static com.sinergise.gwt.gis.map.ui.basket.i18n.UiConstants.BASKET_UI_CONSTANTS;
import static com.sinergise.gwt.gis.map.ui.basket.i18n.UiMessages.BASKET_UI_MSGS;
import static com.sinergise.gwt.gis.map.ui.basket.res.FeatureBasketResources.BASKET_RESOURCES;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.sinergise.common.gis.app.ApplicationContext;
import com.sinergise.common.gis.feature.CFeature;
import com.sinergise.common.gis.feature.HasFeatures;
import com.sinergise.common.gis.map.model.layer.system.SelectionSetLayer;
import com.sinergise.common.ui.action.Action;
import com.sinergise.common.util.messages.MessageType;
import com.sinergise.generics.core.MetaAttributes;
import com.sinergise.gwt.gis.map.ui.MapComponent;
import com.sinergise.gwt.gis.map.ui.actions.HighlightFeaturesToggleAction;
import com.sinergise.gwt.gis.map.ui.actions.ZoomToMBRAction;
import com.sinergise.gwt.gis.map.ui.attributes.FeatureActionsProvider;
import com.sinergise.gwt.gis.map.ui.basket.FeatureBasket.BasketContentChangeListener;
import com.sinergise.gwt.gis.map.ui.basket.action.PickBasketFeaturesAction;
import com.sinergise.gwt.gis.map.ui.basket.res.FeatureBasketResources;
import com.sinergise.gwt.gis.map.ui.util.VisibleFeatureHighlightController;
import com.sinergise.gwt.ui.ActionUtilGWT;
import com.sinergise.gwt.ui.NotificationPanel;
import com.sinergise.gwt.ui.maingui.extwidgets.SGFlowPanel;
import com.sinergise.gwt.ui.maingui.extwidgets.SGHeaderPanel;
import com.sinergise.gwt.ui.maingui.extwidgets.SGPushButton;
import com.sinergise.gwt.ui.maingui.extwidgets.SGTitledPanel;

public class FeatureBasketTab extends SGFlowPanel implements HasFeatures {
	
	private final FeatureBasket basket;
	private final FeatureActionsProvider actionsProvider;
	
	private FeatureBasketTableView tableView;
	private SelectedFeaturesActionsPanel pSelected;
	private SGFlowPanel bottom;
	private NotificationPanel lbEmpty = new NotificationPanel(BASKET_UI_CONSTANTS.isEmpty(), MessageType.INFO);
	private SGHeaderPanel pMain;
	
	public FeatureBasketTab(FeatureBasket basket, FeatureActionsProvider actionsProvider) {
		this.basket = basket;
		this.actionsProvider = actionsProvider;
		
		init();
		refresh();
	}
	
	private void init() {
		addStyleName(FeatureBasketResources.BASKET_RESOURCES.basketStyle().basketTab());
		setHeight("100%");
		
		pMain = new SGHeaderPanel();
		pMain.setHeaderWidget(createTabHeader());
		pMain.setContentWidget(tableView = new FeatureBasketTableView(basket));
		tableView.addStyleName(FeatureBasketResources.BASKET_RESOURCES.basketStyle().basketTableView());
		pMain.setFooterWidget(bottom = new SGFlowPanel(new BasketCountView(), pSelected = new SelectedFeaturesActionsPanel()));
		bottom.setStyleName(FeatureBasketResources.BASKET_RESOURCES.basketStyle().bottomWrapper());
		
		add(pMain);
		
		basket.addContentChangeListener(new BasketContentChangeListener() {
			@Override
			public void onContentChanged() {
				refresh();
				ensureVisible();
			}
		});
	}
	
	private void removeSelected() {
		basket.removeAll(basket.getSelectedFeatures());
	}
	
	private void retainSelected() {
		basket.retainAll(basket.getSelectedFeatures());
	}
	
	public void refresh() {
		tableView.refresh();
		pSelected.updateUI();

		if (basket.isEmpty()) {
			bottom.removeFromParent();
			tableView.removeFromParent();
			pMain.setContentWidget(lbEmpty);
		} else {
			lbEmpty.removeFromParent();
			pMain.setContentWidget(tableView);
			pMain.setFooterWidget(bottom);
		}
		
	}
	
	public FeatureBasket getBasket() {
		return basket;
	}
	
	public String getTabTitle() {
		return basket.getLayer().getTitle();
	}

	@Override
	public Collection<CFeature> getFeatures() {
		return basket.getFeatures();
	}
	
	protected SGTitledPanel createTabHeader() {
		
		HorizontalPanel pActions = new HorizontalPanel();
		for(Action action : getTabHeaderActions()) {
			Widget w = ActionUtilGWT.createActionButton(action);
			pActions.add(w);
			pActions.setCellHorizontalAlignment(w, ALIGN_RIGHT);
		}
		
		SGTitledPanel pHeader = new SGTitledPanel(basket.getLayer().getTitle());
		pHeader.addTitleWidget(pActions, ALIGN_RIGHT);
		
		return pHeader;
	}
	
	private List<Action> getTabHeaderActions() {
		
		MapComponent map = (MapComponent) ApplicationContext.getInstance().getPrimaryMap();
		List<Action> actions = new ArrayList<Action>();
		
		HltSelectedFeaturesAction hltAction 
			= new HltSelectedFeaturesAction(map.getDefaultHighlightLayer(), basket);
		VisibleFeatureHighlightController.register(this, hltAction, map.getDefaultHighlightLayer());
		
		actions.add(new PickBasketFeaturesAction(map, basket));
		actions.addAll(actionsProvider.getFeatureActions(basket, this));
		actions.add(hltAction);
		actions.add(new ZoomToMBRAction(map, basket));
		
		
		return actions;
	}

	private static class HltSelectedFeaturesAction extends HighlightFeaturesToggleAction {
		
		HltSelectedFeaturesAction(final SelectionSetLayer hltLayer, final FeatureBasket basket) {
			super(hltLayer, new HasFeatures() {
				@Override
				public Collection<CFeature> getFeatures() {
					return basket.getSelectionModel().getSelectedSet();
				}
			});
			
			basket.getSelectionModel().addSelectionChangeHandler(new Handler() {
				@Override
				public void onSelectionChange(SelectionChangeEvent event) {
					hltLayer.updateCollection(toHighlight);
				}
			});
			
			setSelected(true);
			setStyle("featureHighlightButton");
		}
		
	}
	
	private class SelectedFeaturesActionsPanel extends FlowPanel {
		SGPushButton butRemoveSelected;
		SGPushButton butRetainSelected;
		
		SelectedFeaturesActionsPanel() {
			init();
			updateUI();
		}
		
		private void init() {
			butRemoveSelected = new SGPushButton(BASKET_UI_CONSTANTS.BasketAction_remove());
			butRemoveSelected.setImage(BASKET_RESOURCES.basket_remove());
			butRemoveSelected.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					removeSelected();
				}
			});
			
			butRetainSelected = new SGPushButton(BASKET_UI_CONSTANTS.BasketAction_retain());
			butRetainSelected.setImage(BASKET_RESOURCES.basket_retain());
			butRetainSelected.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					retainSelected();
				}
			});
			
			add(butRemoveSelected);
			add(butRetainSelected);
			setStyleName(FeatureBasketResources.BASKET_RESOURCES.basketStyle().basketSelectionPanel());
			
			basket.getSelectionModel().addSelectionChangeHandler(new Handler() {
				@Override
				public void onSelectionChange(SelectionChangeEvent event) {
					updateUI();
				}
			});
		}
		
		void updateUI() {
			boolean hasSelection = !basket.isSelectionEmpty();
			butRemoveSelected.setEnabled(hasSelection);
			butRetainSelected.setEnabled(hasSelection);
		}
	}
	
	private class BasketCountView extends SGFlowPanel {
		InlineLabel lbCount;
		InlineLabel lbArea;
		
		BasketCountView() {
			init();
			updateUI();
		}
		
		private void init() {
			setStyleName(FeatureBasketResources.BASKET_RESOURCES.basketStyle().bottomToolbar());
			add(lbArea = new InlineLabel());
			add(lbCount = new InlineLabel());
			lbArea.addStyleName("fl-right");
			
			basket.getSelectionModel().addSelectionChangeHandler(new Handler() {
				@Override
				public void onSelectionChange(SelectionChangeEvent event) {
					updateUI();
				}
			});
		}
		
		void updateUI() {
			lbCount.setText(BASKET_UI_MSGS.FeatureBasket_count(basket.size(), basket.selectionSize()));
			
			double totalArea = basket.totalArea();
			if (totalArea > 0) {
				lbArea.setText(BASKET_UI_MSGS.FeatureBasket_graphArea(
					ApplicationContext.getInstance().getDefaultAreaFormatter().format(basket.totalArea())));
			} else {
				lbArea.setText("");
			}
		}
		
	}

	public String getLayerType() {
		return basket.getLayer().getGenericProperty(MetaAttributes.TYPE, 0);
	}
	
}
