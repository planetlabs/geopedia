package com.sinergise.gwt.gis.query.struct;

import static com.sinergise.common.gis.filter.FilterCapabilities.SCALAR_OP_LOGICAL_AND;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sinergise.common.gis.feature.CFeatureUtils;
import com.sinergise.common.gis.feature.descriptor.CFeatureDescriptor;
import com.sinergise.common.gis.filter.ExpressionDescriptor;
import com.sinergise.common.gis.filter.FilterCapabilities;
import com.sinergise.common.gis.filter.FilterDescriptor;
import com.sinergise.common.gis.filter.InvalidFilterDescriptorException;
import com.sinergise.common.gis.filter.LogicalOperation;
import com.sinergise.common.gis.filter.NoOperation;
import com.sinergise.common.gis.query.Query;
import com.sinergise.common.util.property.descriptor.PropertyDescriptor;
import com.sinergise.gwt.gis.query.struct.cond.HasQueryCondition;
import com.sinergise.gwt.gis.query.struct.cond.QueryConditionFactory;
import com.sinergise.gwt.gis.query.struct.cond.QueryConditionsHolder;

/**
 * Structured query builder
 * 
 * @author tcerovski
 */
public class StructuredLayerQueryBuilder implements QueryConditionsHolder {

	protected final CFeatureDescriptor featureDesc;
	protected final FilterCapabilities filterCaps;
	
	protected final List<HasQueryCondition> conditions = new ArrayList<HasQueryCondition>();
	
	public StructuredLayerQueryBuilder(CFeatureDescriptor fDesc, FilterCapabilities filterCaps) {
		this(fDesc, filterCaps, new QueryConditionFactory());
	}
	
	public StructuredLayerQueryBuilder(CFeatureDescriptor fDesc, FilterCapabilities filterCaps, QueryConditionFactory qcFactory) {
		this.featureDesc = fDesc;
		this.filterCaps = filterCaps;
		init(qcFactory);
	}
	
	private void init(QueryConditionFactory qcFactory) {
		for (PropertyDescriptor<?> pd : featureDesc) {
			HasQueryCondition condition = qcFactory.createQueryCondition(pd, filterCaps);
			if (pd.isQueryable() && condition != null) {
				conditions.add(condition);
			}
		}
	}
	
	public String getFeatureTypeName() {
		return featureDesc.getFeatureTypeName();
	}
	
	@Override
	public List<HasQueryCondition> getConditions() {
		return Collections.unmodifiableList(conditions);
	}
	
	public boolean hasConditions() {
		return !conditions.isEmpty();
	}
	
	protected FilterDescriptor buildFilter(boolean allowEmptyQuery) throws InvalidFilterDescriptorException {
		List<FilterDescriptor> exprs = new ArrayList<FilterDescriptor>();
		
		for (HasQueryCondition condition : conditions) {
			FilterDescriptor expr = condition.getQueryCondition();
			if (expr != null) {
				exprs.add(expr);
			}
		}
		
		if (exprs.isEmpty()) {
			if (allowEmptyQuery) {
				return new NoOperation();
			}
			return null;
		} else if(exprs.size() == 1) {
			return exprs.get(0);
		}
		return new LogicalOperation(exprs.toArray(new ExpressionDescriptor[exprs.size()]), SCALAR_OP_LOGICAL_AND);
	}
	
	public void buildQuery(boolean allowEmptyQuery, AsyncCallback<Query> callback) {
		try {
			callback.onSuccess(new Query(featureDesc.getFeatureTypeName(), 
				CFeatureUtils.getPropertyNamesForQuery(featureDesc), buildFilter(allowEmptyQuery)));
		} catch (InvalidFilterDescriptorException e) {
			callback.onFailure(e);
		}
	}
	
	public void clearQueryFieldValues() {
		for (HasQueryCondition cond : conditions) {
			cond.setValue(null, true);
		}
	}
	
	public void setQueryFieldValues(Map<String, String> valuesMap, boolean clearOthers) {
		for (HasQueryCondition cond : conditions) {
			String propName = cond.getPropertyDescriptor().getSystemName();
			
			if (valuesMap.containsKey(propName)) {
				cond.setValue(valuesMap.get(propName), true);
			} else if (clearOthers) {
				cond.setValue(null, true);
			}
		}
	}
	
	public Map<String, String> getQueryFieldValues() {
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
