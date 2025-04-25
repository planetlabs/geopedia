package com.sinergise.generics.core.filter.predicate;

import static com.sinergise.common.util.lang.Predicate.Composite.CompositionOp.AND;

import java.util.Collection;

import com.sinergise.common.util.lang.Predicate;
import com.sinergise.generics.core.AbstractEntityObject;

public class EntityObjectFilterAnd  extends Predicate.Composite<IEntityObjectFilter, AbstractEntityObject> implements IEntityObjectFilter {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6772828030697831873L;

	public EntityObjectFilterAnd() {
		super(AND);
	}
	
	public EntityObjectFilterAnd(IEntityObjectFilter[] filters) {
		super(AND, filters);
	}

	public EntityObjectFilterAnd(Collection<? extends IEntityObjectFilter> filters) {
		super(AND, filters);
	}
	
	public EntityObjectFilterAnd(IEntityObjectFilter filter) {
		super(AND, filter);
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
