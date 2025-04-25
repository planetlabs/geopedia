package com.sinergise.common.util.naming;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import com.sinergise.common.util.collections.CollectionUtil;

public class EntityIdentifier extends Identifier {
	
	public static interface IdentifierResolveListener {
		void onIdentifierResolved(EntityIdentifier ID);
	}
	
	private static final long serialVersionUID = 1L;
	private static final AtomicLong nextTempId = new AtomicLong(0);
	
	private /*final*/ boolean temporary; 
	private EntityIdentifier resolvedId;
	
	private transient LinkedList<IdentifierResolveListener> resolveListeners = null;
	
	@Deprecated
	protected EntityIdentifier() {
		super();
		temporary = false;
	}
	
	public EntityIdentifier(Identifier entityTypeId) {
		this(entityTypeId, null);
	}

	public EntityIdentifier(Identifier entityTypeId, String localId) {
		super(entityTypeId, localId == null ? String.valueOf(nextTempId.getAndIncrement())+"*" : localId);
		temporary = (localId == null || localId.endsWith("*"));
	}
	
	public boolean isEntityType(Identifier entityTypeId) {
		Identifier myParent = getParent();
		if (myParent == null) {
			return false;
		}
		return myParent.equals(entityTypeId);
	}

	public boolean isTemporary() {
		return temporary;
	}
	
	public boolean isResolved() {
		return !temporary || resolvedId != null;
	}
	
	public EntityIdentifier getResolvedId() {
		return temporary || resolvedId != null ? resolvedId : this;
	}
	
	public void setResolvedId(EntityIdentifier resolvedId) {
		assert (getParent().equals(resolvedId.getParent()));
		this.resolvedId = resolvedId;
		fireResolved();
	}
	
	@Override
	public String getLocalID() {
		return resolvedId != null ? resolvedId.getLocalID() : super.getLocalID();
	}

	@Override
	protected int computeLocalHashCode() {
		return 31 * super.computeLocalHashCode() + (temporary ? 1231 : 1237);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (temporary != ((EntityIdentifier)obj).temporary) {
			return false;
		}
		return true;
	}

	public boolean equalsCheckResolved(EntityIdentifier other) {
		if (equals(other)) {
			return true;
		}
		EntityIdentifier otherResolved = other.getResolvedId();
		if (temporary && resolvedId != null) {
			return resolvedId.equals(other) || resolvedId.equals(otherResolved);
		}
		if (otherResolved != null) {
			return otherResolved.equals(this);
		}
		return false;
	}
	
	private void fireResolved() {
		if (resolveListeners == null) {
			return;
		}
		for (IdentifierResolveListener l : resolveListeners) {
			l.onIdentifierResolved(this);
		}
		
		resolveListeners = null; //will not happen again
	}
	
	public synchronized void addResolveListener(IdentifierResolveListener listener) {
		if (isResolved()) {
			return; //already resolved
		}
		
		if (resolveListeners == null) {
			resolveListeners = new LinkedList<IdentifierResolveListener>();
		}
		resolveListeners.add(listener);
	}
	
	public void removeResolveListener(IdentifierResolveListener listener) {
		if (resolveListeners != null) {
			resolveListeners.remove(listener);
		}
	}
	
	public static List<EntityIdentifier> extractEntityIdentifiers(Collection<? extends IdentifiableEntity> entities) {
		return CollectionUtil.mapToList(entities, IdentifiableEntity.FUNC_ENTITY_ID_GETTER);
	}
	
	public static EntityIdentifier extractEntityIdentifier(Object entity) {
		return entity instanceof IdentifiableEntity ? ((IdentifiableEntity) entity).getQualifiedID() : null;
	}
	
}