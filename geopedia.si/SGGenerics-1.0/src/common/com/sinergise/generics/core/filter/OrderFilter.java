package com.sinergise.generics.core.filter;

import java.util.ArrayList;

import com.sinergise.common.util.settings.Settings;
import com.sinergise.generics.core.EntityType;
import com.sinergise.generics.core.TypeAttribute;

public class OrderFilter implements DataFilter, Settings {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5417021083868333677L;

	private OrderOption[] orderBy;
	private String entityTypeName;
	private ArrayList<Integer> orderSequence = new ArrayList<Integer>();
	
	
	protected OrderFilter() {		
	}
	
	
	public static OrderFilter createForAttribute(TypeAttribute attribute, EntityType entityType, OrderOption orderOption) {
		OrderOption[] oo = createEmptyOrderBy(entityType);
		oo[attribute.getId()] = orderOption;
		return new OrderFilter(oo, entityType.getName());
	}
	
	public static OrderOption createFromString (String order) {
		if ("ASC".equals(order))
			return OrderOption.ASC;
		else if ("DESC".equals(order))
			return OrderOption.DESC;
		return OrderOption.OFF;
	}
	public static OrderOption [] createEmptyOrderBy (EntityType et) {
		OrderOption [] ob = new OrderOption[et.getAttributeCount()];
		for (int i=0;i<ob.length;i++)
			ob[i] = OrderOption.OFF;
		return ob;
	}
	public OrderFilter (OrderOption[] orderBy, String entityTypeName) {
		this.orderBy = orderBy;
		this.entityTypeName = entityTypeName;
	}

	public OrderOption[] getOrderBy() {
		return orderBy;
	}
	
	public OrderOption getOrderBy(int idx) {
		if (idx<0 || idx>=orderBy.length) 
			throw new IllegalArgumentException("Index is invalid "+idx);
		return orderBy[idx];
	}
	
	public void setOrderBy(OrderOption order, int idx) {
		if (idx<0 || idx>=orderBy.length) 
			throw new IllegalArgumentException("Index is invalid "+idx);
		orderBy[idx] = order;
	}
	
	public void addToOrderSequence(int idx) {
		orderSequence.add(idx);
	}
	
	public ArrayList<Integer> getOrderSequence() {
		if (orderSequence.size()==0) { // generate sequence if it doesn't exist
			for (int i=0;i<orderBy.length;i++) {
				if (orderBy[i] != null && orderBy[i].isOn()) {
					orderSequence.add(i);
				}
			}
		}
		return orderSequence;
	}
	
	public void setOrderSequence(ArrayList<Integer> sequence) {
		orderSequence=sequence;
	}
	
	public void setOrderBy(OrderOption[] orderBy) {
		this.orderBy = orderBy;
	}
	public String getEntityTypeName() {
		return entityTypeName;
	}
}
