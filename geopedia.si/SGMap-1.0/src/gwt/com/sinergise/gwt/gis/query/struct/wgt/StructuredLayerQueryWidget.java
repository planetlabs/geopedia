package com.sinergise.gwt.gis.query.struct.wgt;

import static com.sinergise.common.util.string.StringUtil.isNullOrEmpty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.util.collections.CollectionUtil;
import com.sinergise.common.util.lang.Function;
import com.sinergise.common.util.property.descriptor.PropertyDescriptor;
import com.sinergise.gwt.gis.map.util.StyleConsts;
import com.sinergise.gwt.gis.query.struct.cond.HasQueryCondition;
import com.sinergise.gwt.gis.query.struct.cond.QueryConditionsHolder;
import com.sinergise.gwt.ui.HasIcon;
import com.sinergise.gwt.ui.table.FlexTableBuilder;

/**
 * Widget with query condition widgets in a label-widget pair table.
 * 
 * @author tcerovski
 */
public class StructuredLayerQueryWidget extends Composite {
	private Comparator<HasQueryCondition> COND_ORDER_COMP = CollectionUtil.createFunctionComparator(new Function<HasQueryCondition, Integer>() {
		@Override
		public Integer execute(HasQueryCondition param) {
			return Integer.valueOf(param.getPropertyDescriptor().getOrder());
		}
	});
	
	protected List<Widget> widgets = new ArrayList<Widget>();
	protected Map<HasQueryCondition, Label> labels = new HashMap<HasQueryCondition, Label>();
	private final QueryConditionsHolder condHolder;

	public StructuredLayerQueryWidget(QueryConditionsHolder condHolder) {
		this(condHolder, new QueryConditionWidgetFactory());
	}
	
	public StructuredLayerQueryWidget(QueryConditionsHolder condHolder, QueryConditionWidgetFactory widgetFactory) {
		this.condHolder = condHolder;
		
		FlexTableBuilder tableBld = new FlexTableBuilder(StyleConsts.STRUCTURED_QUERY_TABLE);
		
		List<HasQueryCondition> conditions = new ArrayList<HasQueryCondition>(condHolder.getConditions());
		Collections.sort(conditions, COND_ORDER_COMP);
		
		boolean withIcon = false;
		for (HasQueryCondition condition : conditions) {
			PropertyDescriptor<?> pd = condition.getPropertyDescriptor();
			String label = pd.getTitle();
			if (pd.isQueryRequired()) {
				label = "*"+label;
			}
			
			Label wLabel = new Label(label);
			Widget w = widgetFactory.createQueryConditionWidget(condition);
			
			labels.put(condition, wLabel);
			widgets.add(w);
			
			tableBld.addFieldLabel(wLabel);
			tableBld.addFieldValueWidget(w);
			tableBld.newRow();
			
			if (w instanceof HasIcon) {
				withIcon = true;
			}
			
		}
		
		if(withIcon) {
			tableBld.getTable().addStyleName("withIcon");
		}
		
		initWidget(tableBld.buildTable());	
		setWidth("100%");
	}
	
	public void clearConditions() {
		for (HasQueryCondition condition : condHolder.getConditions()) {
			condition.setValue(null, true);
			labels.get(condition).removeStyleName(com.sinergise.gwt.ui.StyleConsts.ERROR_LABEL);
		}
	}
	
	public Collection<Widget> getValueWidgets() {
		return Collections.unmodifiableCollection(widgets);
	}
	
	public boolean validateInput() {
		boolean valid = true;
		for (HasQueryCondition cond : condHolder.getConditions()) {
			Label lb = labels.get(cond); 
			if (cond.getPropertyDescriptor().isQueryRequired() && isNullOrEmpty(cond.getValue())) {
				lb.addStyleName(com.sinergise.gwt.ui.StyleConsts.ERROR_LABEL);
				valid = false;
			} else {
				lb.removeStyleName(com.sinergise.gwt.ui.StyleConsts.ERROR_LABEL);
			}
		}
		return valid;
	}
}
