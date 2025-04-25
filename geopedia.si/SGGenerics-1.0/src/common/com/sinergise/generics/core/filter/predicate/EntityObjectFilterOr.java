package com.sinergise.generics.core.filter.predicate;

import static com.sinergise.common.util.lang.Predicate.Composite.CompositionOp.OR;

import java.util.Collection;

import com.sinergise.common.util.lang.Predicate;
import com.sinergise.generics.core.AbstractEntityObject;

public class EntityObjectFilterOr  extends Predicate.Composite<IEntityObjectFilter, AbstractEntityObject> implements IEntityObjectFilter {	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3053918958315501882L;

	public EntityObjectFilterOr() {
		super(OR);
	}
	
	public EntityObjectFilterOr(IEntityObjectFilter[] filters) {
		super(OR, filters);
	}

	public EntityObjectFilterOr(Collection<? extends IEntityObjectFilter> filters) {
		super(OR, filters);
	}
	
	public EntityObjectFilterOr(IEntityObjectFilter filter) {
		super(OR, filter);
	}
	
	
	@Override
	public EntityObjectFilterNot not() {
		return (EntityObjectFilterNot)super.not();
	}
	
	@Override
	protected EntityObjectFilterNot createNegation() {
		return new EntityObjectFilterNot(this);
	}
	
}
