package com.sinergise.common.util.naming;



public abstract class IdentifiableEntityImpl implements IdentifiableEntity {
	private static final long serialVersionUID = 1998252825728325472L;
	
	protected EntityIdentifier id = null;
	
	/**
	 * To be used for cloning and serialization only; setId should be called immediately after creation 
	 */
	protected IdentifiableEntityImpl() {
	}
	
	public IdentifiableEntityImpl(EntityIdentifier id) {
		assert id != null;
		this.id = id;
	}
	
	/**
	 * To be used for cloning only; call immediately after creating this object with empty constructor.
	 * @param id
	 */
	protected void setId(EntityIdentifier id) {
		assert this.id == null;
		assert id != null;
		this.id = id;
	}
	
	@Override
	public String getLocalID() {
		return id.getLocalID();
	}

	@Override
	public EntityIdentifier getQualifiedID() {
		return id;
	}
	
	@Override
	public final boolean hasPermanentId() {
		return id.isResolved();
	}
	
	@Override
	public final boolean equals(Object obj) {
		if (obj==this) {
			return true;
		}
		if (!(obj instanceof IdentifiableEntityImpl)) {
			return false;
		}
		return ((IdentifiableEntityImpl)obj).id.equals(id);
	}
	
	@Override
	public final boolean equalsCheckResolved(Object obj) {
		if (obj==this) {
			return true;
		}
		if (!(obj instanceof IdentifiableEntityImpl)) {
			return false;
		}
		return ((IdentifiableEntityImpl)obj).id.equalsCheckResolved(id);
	}
	
	@Override
	public final int hashCode() {
		return id.hashCode();
	}
}
