package com.sinergise.geopedia.core.entities;

import java.io.Serializable;

public abstract class HasId implements Serializable {
	private static final long serialVersionUID = 2882373206525864795L;

	public static final int NO_VALID_ID=0; // TODO change to minvalue?
	public int id = NO_VALID_ID; 

	
	public int getId() {
		return id;
	}
	public boolean hasValidId() {
		if (id>NO_VALID_ID)
			return true;
		return false;
	}
	
	public void setId(int id) {
		this.id=id;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof HasId))
			return false;
		HasId other = (HasId) obj;
		if (id != other.id)
			return false;
		return true;
	}
	
}
