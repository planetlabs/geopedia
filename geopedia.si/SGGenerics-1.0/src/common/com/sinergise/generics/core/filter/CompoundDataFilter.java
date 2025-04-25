package com.sinergise.generics.core.filter;

import java.util.ArrayList;
import java.util.HashSet;

import com.sinergise.generics.core.TypeAttribute;

public class CompoundDataFilter implements DataFilter{
	/**
	 * 
	 */
	private static final long serialVersionUID = -1638792484125006231L;
	
	private ArrayList<DataFilter> filters = new ArrayList<DataFilter>();
	private ArrayList<Byte> operators = new ArrayList<Byte>();
	private OrderFilter orderFilter = null;
	private LimitFilter limitFilter = new LimitFilter(Integer.MIN_VALUE);
	private Integer distinctAttributeID = null;
	private HashSet<Integer> selectionAttributeIDs = null;
	
	
	
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((distinctAttributeID == null) ? 0 : distinctAttributeID.hashCode());
		result = prime * result + ((filters == null) ? 0 : filters.hashCode());
		result = prime * result + ((operators == null) ? 0 : operators.hashCode());
		result = prime * result + ((orderFilter == null) ? 0 : orderFilter.hashCode());
		result = prime * result + ((limitFilter == null) ? 0 : limitFilter.hashCode());
		result = prime * result + ((selectionAttributeIDs == null) ? 0 : selectionAttributeIDs.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CompoundDataFilter other = (CompoundDataFilter) obj;
		if (distinctAttributeID == null) {
			if (other.distinctAttributeID != null)
				return false;
		} else if (!distinctAttributeID.equals(other.distinctAttributeID))
			return false;
		if (filters == null) {
			if (other.filters != null)
				return false;
		} else if (!filters.equals(other.filters))
			return false;
		if (operators == null) {
			if (other.operators != null)
				return false;
		} else if (!operators.equals(other.operators))
			return false;
		if (orderFilter == null) {
			if (other.orderFilter != null)
				return false;
		} else if (!orderFilter.equals(other.orderFilter))
			return false;
		if (limitFilter == null) {
			if (other.limitFilter != null)
				return false;
		} else if (!limitFilter.equals(other.limitFilter))
			return false;
		if (selectionAttributeIDs == null) {
			if (other.selectionAttributeIDs != null)
				return false;
		} else if (!selectionAttributeIDs.equals(other.selectionAttributeIDs))
			return false;
		return true;
	}

	public void addDataFilter (DataFilter filter, byte operator) {
		filters.add(filter);
		operators.add(operator);
	}
	
	public int getLength() {
		return filters.size();
	}
	
	public DataFilter getFilter(int idx) {
		return filters.get(idx);
	}
	
	public byte getOperator(int idx) {
		return operators.get(idx);
	}
	
	public void addSelectionAttribute(TypeAttribute at) {
		if (selectionAttributeIDs == null) {
			selectionAttributeIDs = new HashSet<Integer>();
		}
		selectionAttributeIDs.add(at.getId());
	}
	
	public HashSet<Integer> getSelectionAttributeIDs() {
		return selectionAttributeIDs;
	}
	public Integer getDistinctAttributeId() {
		return distinctAttributeID;
	}
	
	public void setDistinctAttribute(TypeAttribute da) {
		this.distinctAttributeID=da.getId();
	}
	
	public OrderFilter getOrderFilter() {
		return orderFilter;
	}
	
	public void setOrderFilter(OrderFilter orderFilter) {
		this.orderFilter = orderFilter;
	}
	
	public LimitFilter getLimitFilter() {
		return limitFilter;
	}
	
	public void setLimitFilter(LimitFilter limitFilter) {
		this.limitFilter = limitFilter;
	}
}
