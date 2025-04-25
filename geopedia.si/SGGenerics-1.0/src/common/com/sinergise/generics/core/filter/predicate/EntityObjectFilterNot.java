package com.sinergise.generics.core.filter.predicate;

import com.sinergise.common.util.lang.Predicate;
import com.sinergise.generics.core.AbstractEntityObject;

public class EntityObjectFilterNot extends Predicate.Not<AbstractEntityObject> implements IEntityObjectFilter {
	private static final long serialVersionUID = -6314190028331295390L;

	@Deprecated
	protected EntityObjectFilterNot() {
	}
	
	public EntityObjectFilterNot(IEntityObjectFilter ref) {
		super(ref);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!super.equals(obj)) return false;
		return obj instanceof EntityObjectFilterNot;
	}
	
	@Override
	public EntityObjectFilterNot not() {
		return (EntityObjectFilterNot)ref;
	}
}