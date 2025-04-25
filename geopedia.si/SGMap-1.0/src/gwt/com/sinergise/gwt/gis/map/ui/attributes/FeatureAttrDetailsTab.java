package com.sinergise.gwt.gis.map.ui.attributes;

import static com.google.gwt.user.client.ui.HasHorizontalAlignment.ALIGN_LEFT;
import static com.sinergise.gwt.gis.map.ui.attributes.AttributesHistoryHandler.HISTORY_PARAM_KEY_FEATURE;
import static com.sinergise.gwt.gis.map.util.StyleConsts.RESULT_ACTIONS_TOOLBAR;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.gis.feature.CFeature;
import com.sinergise.common.gis.feature.CFeatureCollection;
import com.sinergise.common.gis.feature.CFeatureUtils;
import com.sinergise.common.gis.feature.CFeatureUtils.PropertyDisplayData;
import com.sinergise.common.gis.feature.HasFeature;
import com.sinergise.common.gis.map.model.layer.info.FeatureInfoItem;
import com.sinergise.common.ui.action.Action;
import com.sinergise.gwt.gis.map.shapes.editor.event.FeaturesChangedEvent;
import com.sinergise.gwt.gis.map.shapes.editor.event.FeaturesChangedEvent.FeaturesUpdatedEventHandler;
import com.sinergise.gwt.gis.map.ui.MapComponent;
import com.sinergise.gwt.gis.map.ui.attributes.TabbedAttributesPanel.FeatureAttrDetailsTabDecorator;
import com.sinergise.gwt.gis.map.ui.attributes.TabbedAttributesPanel.FeatureAttrTabSettings;
import com.sinergise.gwt.gis.map.util.StyleConsts;
import com.sinergise.gwt.ui.ActionUtilGWT;
import com.sinergise.gwt.ui.maingui.extwidgets.SGFlowPanel;
import com.sinergise.gwt.ui.maingui.extwidgets.SGTabLayoutPanel;
import com.sinergise.gwt.ui.table.FlexTableBuilder;


public class FeatureAttrDetailsTab extends SGFlowPanel implements HasFeature, FeaturesUpdatedEventHandler {
	
	public static class FeatureAttrDetailsSettings extends FeatureAttrTabSettings { }
	
	protected MapComponent map;
	protected SGTabLayoutPanel parent;
	protected CombinedFeatureActionsProvider actionsProvider;
	protected FeatureAttrDetailsTabDecorator decorator;
	
	private PropertyWidgetFactory propWgtFactory = new ReadOnlyPropertyWidgetFactory();
	
	protected SGFlowPanel container;
	
	protected FeatureInfoItem item;
	
	public FeatureAttrDetailsTab(MapComponent map, SGTabLayoutPanel parent, CombinedFeatureActionsProvider actionsProvider, FeatureAttrDetailsTabDecorator decorator) {
		this.map = map;
		this.parent = parent;
		this.actionsProvider = actionsProvider != null ? actionsProvider : new CombinedFeatureActionsProvider();
		this.decorator = decorator;
		
		add(container = new SGFlowPanel());
	}
	
	public void setFeature(FeatureInfoItem fInfo) {
		this.item = fInfo;
		render();
	}
	
	public void render() {
		container.clear();
		container.add(buildDetailsWidget());
	}
	
	protected Widget buildDetailsWidget() {
		FlexTableBuilder ftb = new FlexTableBuilder();
		buildDetailsTable(ftb);
		if (decorator != null) decorator.decorate(ftb, item);
		FlexTable table = ftb.getTable();
		table.setWidth("100%");
		table.addStyleName(StyleConsts.TABBED_ATTRIBUTES_PANEL+"-details");
		return table;
	}

	protected void buildDetailsTable(FlexTableBuilder ftb) {
		ftb.addTitleWidget(buildHeaderWidget());
		ftb.setCurrentCellColSpan(2);
		ftb.newRow();
		fillPropertiesTable(item.f, ftb);
	}
	
	protected void fillPropertiesTable(CFeature f, FlexTableBuilder ftb) {
		fillPropertiesTable(f, ftb, Integer.MIN_VALUE);
	}

	protected void fillPropertiesTable(CFeature f, FlexTableBuilder ftb, int maxProperties) {
		CFeatureUtils.FeatureInfoDisplayBuilder builder = new CFeatureUtils.FeatureInfoDisplayBuilder(f.getDescriptor());
		builder.setMaxProperties(maxProperties);
		builder.setHistoryKey(HISTORY_PARAM_KEY_FEATURE);
		for (PropertyDisplayData<?> att : builder.getValues(f)) {
			addPropertyRow(att, ftb);
		}
	}
	
	protected void addPropertyRow(PropertyDisplayData<?> att, FlexTableBuilder ftb) {
		PropertyWidgetFactory factory = getPropertyWidgetFactory();
		ftb.addFieldLabelWidget(factory.createLabelWidget(att)).setCurrentCellHAlign(ALIGN_LEFT);
		ftb.addFieldValueWidget(factory.createValueWidget(att)).setCurrentCellWidth("100%");
		ftb.newRow();
	}
	
	protected PropertyWidgetFactory getPropertyWidgetFactory() {
		return propWgtFactory;
	}

	protected Widget buildHeaderWidget() {
		HorizontalPanel hp = new HorizontalPanel();
		hp.addStyleName(RESULT_ACTIONS_TOOLBAR);
		hp.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		
		Label lbTitle = new Label(item.f.getTitle());
		hp.add(lbTitle);
		hp.setCellWidth(lbTitle, "100%");
		hp.setCellHorizontalAlignment(lbTitle, HasHorizontalAlignment.ALIGN_LEFT);
		
		for(Widget w : createActionButtons()) {
			hp.add(w);
			hp.setCellHorizontalAlignment(w, HasHorizontalAlignment.ALIGN_RIGHT);
		}
		
		return hp;
	}
	
	protected List<Widget> createActionButtons() {
		List<Widget> buttons = new ArrayList<Widget>();
		
		for (Action  action : actionsProvider.getFeatureActions(CFeatureCollection.singleton(item.f), this)) {
			buttons.add(ActionUtilGWT.createActionButton(action));
		}
		return buttons;
	}

	public String getTabTitle() {
		if (item == null || item.f == null) return "?";
		String sn = item.f.getShortName();
		if (sn != null) return sn;
		return item.f.getTitle();
	}

	public void close() {
		parent.closeTab(this);
	}
	
	@Override
	public CFeature getFeature() {
		if (item != null) return item.f;
		return null;
	}
	
	public void onClosing() {
		
	}
	
	public boolean canClose() {
		return true;
	}
	
	public void applySettings(FeatureAttrDetailsSettings settings) {
		if (settings != null) {
			settings.registerExtraActions(actionsProvider);
		}
	}
	
	@Override
	public void onFeaturesUpdated(FeaturesChangedEvent event) {
		//TODO: handle feature update
		if (event.isDeleted(item.f)) {
			close();
		}
	}
}
