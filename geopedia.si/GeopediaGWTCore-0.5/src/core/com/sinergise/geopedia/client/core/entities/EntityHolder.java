package com.sinergise.geopedia.client.core.entities;

import com.sinergise.geopedia.core.entities.AbstractNamedEntity;
import com.sinergise.geopedia.core.util.event.IsEntityChangedSource;

public class EntityHolder<E extends AbstractNamedEntity> implements IsEntityChangedSource{

	private EntityChangeListenerCollection<E> listeners = new EntityChangeListenerCollection<E>();
	protected E entity = null;
	
		
	public void setEntity(E entity) {
		this.entity = entity;
		onEntityChanged();
	}
	
	
	public E getEntity() {
		return entity;
	}
	
	public void onEntityChanged() {
		listeners.fireValueChanged(this, getEntity());
	}
	

	public void addEntityChangedListener(EntityChangedListener<E> listener) {
		listeners.add(listener);
	}

	
	public boolean contains(E toCheck) {
		if (this.entity == null)
			return false;
		if (toCheck==null) 
			return false;
		if (entity.getId() == toCheck.getId())
			return true;
		return false;
	}
}
