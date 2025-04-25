package com.sinergise.geopedia.core.util.event;

import java.util.Vector;

import com.sinergise.geopedia.core.entities.AbstractNamedEntity;

public interface IsEntityChangedSource {
	public interface EntityChangedListener<E extends AbstractNamedEntity> {
		public void onEntityChanged(IsEntityChangedSource source, E value);
	}
	public static class EntityChangeListenerCollection<E extends AbstractNamedEntity> extends Vector<EntityChangedListener<E>> {
		private static final long serialVersionUID = 6632276932331200422L;

		
		public boolean fireValueChanged(final IsEntityChangedSource sender, final E value) {			
			for (int i = size() - 1; i >= 0; i--) {
				get(i).onEntityChanged(sender, value);
			}
			return true;
		}
		
		
		
	}
}
