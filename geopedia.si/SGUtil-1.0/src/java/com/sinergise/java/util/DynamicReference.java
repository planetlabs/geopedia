package com.sinergise.java.util;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;

public class DynamicReference<T> {
	public static final int	TYPE_HARD	= 0;
	public static final int	TYPE_WEAK	= 1;
	public static final int	TYPE_SOFT	= 2;

	private int				type;
	private T				referent;			/* Hard */
	private Reference<T>	ref;

	public DynamicReference(final T referent, final int type) {
		this.referent = referent;
		setType(type);
	}

	public int getType() {
		return type;
	}

	public T get() {
		if (referent == null) { 
			return ref == null ? null : ref.get();
		}
		return referent;
	}

	public void set(final T obj) {
		if (obj == null) {
			clear();
		} else {
			internalSetType(type, obj);
		}
	}

	public void clear() {
		this.referent = null;
		if (ref != null) {
			ref.clear();
		}
		ref = null;
	}

	public boolean isEnqueued() {
		if (ref != null) {
			return ref.isEnqueued();
		}
		return false;
	}

	public boolean setType(final int type) {
		if (this.type == type) {
			return true;
		}
		final T obj = get();
		return internalSetType(type, obj);
	}

	protected boolean internalSetType(final int newType, T obj) {
		this.type = newType;
		referent = null;
		if (obj == null) {
			ref = null;
			return false;
		}
		if (newType == TYPE_HARD) {
			ref = null;
			referent = obj;
		} else if (newType == TYPE_WEAK) {
			// Set reference to the new value
			if (!(ref instanceof WeakReference<?>) || (obj != ref.get())) {
				ref = new WeakReference<T>(obj);
			}
			ref.get(); // refresh access
		} else if (newType == TYPE_SOFT) {
			// Set reference to the new value
			if (!(ref instanceof SoftReference<?>) || (obj != ref.get())) {
				ref = new SoftReference<T>(obj);
			}
			ref.get(); // refresh access
		}
		return true;
	}

	public boolean isEmpty() {
		if (referent != null) {
			return false;
		}
		return ref == null || ref.get() == null;
	}
}
