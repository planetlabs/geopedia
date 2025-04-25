package com.sinergise.gwt.gis.query.filter;

import static com.sinergise.common.gis.filter.FilterCapabilities.SCALAR_OP_LOGICAL_AND;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sinergise.common.gis.feature.descriptor.CFeatureDescriptor;
import com.sinergise.common.gis.filter.ExpressionDescriptor;
import com.sinergise.common.gis.filter.FilterCapabilities;
import com.sinergise.common.gis.filter.FilterDescriptor;
import com.sinergise.common.gis.filter.InvalidFilterDescriptorException;
import com.sinergise.common.gis.filter.LogicalOperation;
import com.sinergise.common.util.property.descriptor.PropertyDescriptor;
import com.sinergise.gwt.gis.query.struct.cond.HasQueryCondition;
import com.sinergise.gwt.gis.query.struct.cond.QueryConditionFactory;
import com.sinergise.gwt.gis.query.struct.cond.QueryConditionsHolder;

/**
 * Structured query builder
 * 
 * @author tcerovski
 */
public class LayerFilterBuilder implements QueryConditionsHolder {

	protected final CFeatureDescriptor featureDesc;
	protected final FilterCapabilities filterCaps;
	
	protected final List<HasQueryCondition> conditions = new ArrayList<HasQueryCondition>();
	
	public LayerFilterBuilder(CFeatureDescriptor fDesc, FilterCapabilities filterCaps) {
		this(fDesc, filterCaps, new QueryConditionFactory());
	}
	
	public LayerFilterBuilder(CFeatureDescriptor fDesc, FilterCapabilities filterCaps, QueryConditionFactory qcFactory) {
		this.featureDesc = fDesc;
		this.filterCaps = filterCaps;
		init(qcFactory);
	}
	
	private void init(QueryConditionFactory qcFactory) {
		for (PropertyDescriptor<?> pd : featureDesc) {
			HasQueryCondition condition = qcFactory.createQueryCondition(pd, filterCaps);
			if (isFilterableProperty(pd) && condition != null) {
				conditions.add(condition);
			}
		}
	}
	
	protected boolean isFilterableProperty(PropertyDescriptor<?> pd) {
		return pd.isQueryable();
	}
	
	public String getFeatureTypeName() {
		return featureDesc.getFeatureTypeName();
	}
	
	@Override
	public List<HasQueryCondition> getConditions() {
		return Collections.unmodifiableList(conditions);
	}
	
	public FilterDescriptor buildFilter() throws InvalidFilterDescriptorException {
		List<FilterDescriptor> exprs = new ArrayList<FilterDescriptor>();
		
		for (HasQueryCondition condition : conditions) {
			FilterDescriptor expr = condition.getQueryCondition();
			if (expr != null) {
				exprs.add(expr);
			}
		}
		
		if (exprs.isEmpty()) {
			return null;
		} else if(exprs.size() == 1) {
			return exprs.get(0);
		}
		return new LogicalOperation(exprs.toArray(new ExpressionDescriptor[exprs.size()]), SCALAR_OP_LOGICAL_AND);
		
	}
	
	public void clearFilterValues() {
		for (HasQueryCondition cond : conditions) {
			cond.setValue(null, true);
		}
	}
	
	public void setFilterFieldValues(Map<String, String> valuesMap, boolean clearOthers) {
		for (HasQueryCondition cond : conditions) {
			String propName = cond.getPropertyDescriptor().getSystemName();
			
			if (valuesMap != null && valuesMap.containsKey(propName)) {
				cond.setValue(valuesMap.get(propName), true);
			} else if (clearOthers) {
				cond.setValue(null, true);
			}
		}
	}
	
	public Map<String, String> getFilterFieldValues() {
		Map<String, String> map = new HashMap<String, String>();
		for (HasQueryCondition cond : conditions) {
			String propName = cond.getPropertyDescriptor().getSystemName();
			
			String value = cond.getValue();
			if (value != null && value.trim().length() > 0) {
				map.put(propName, value);
			}
		}
		
		return map;
	}
	
}
