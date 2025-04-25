package com.sinergise.common.util;

public class InstanceAlreadySetException extends IllegalArgumentException {

	private static final long serialVersionUID = 3595416228384995533L;

	public InstanceAlreadySetException(Object alreadySet, Object triedToSet) {
		this("Only one instance of "+alreadySet.getClass()+" allowed. Already set: "+alreadySet +", Tried to set: "+triedToSet);
	}

	public InstanceAlreadySetException(String s) {
		super(s);
	}

}
