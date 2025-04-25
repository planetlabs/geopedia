/*
 *
 */
package com.sinergise.common.util.settings;

import java.util.Arrays;

public class ResolvedType<T> {
	public static <C> ResolvedType<C> create(Class<C> klass) {
		return new ResolvedType<C>(klass);
	}
	
	public final Class<T> rawType;
	public final ResolvedType<?>[]     parameterTypes;
	
	public ResolvedType(final Class<T> rawType) {
		this(rawType, new ResolvedType[0]);
	}
	
	/**
	 * @param rawType
	 * @param parameterTypes
	 */
	public ResolvedType(final Class<T> rawType, final ResolvedType<?>[] parameterTypes) {
		this.rawType = rawType;
		this.parameterTypes = parameterTypes;
	}
	
	@Override
	public String toString() {
		return toString(this);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(parameterTypes);
		result = prime * result + ((rawType == null) ? 0 : rawType.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final ResolvedType<?> other = (ResolvedType<?>)obj;
		if (!Arrays.equals(parameterTypes, other.parameterTypes)) {
			return false;
		}
		if (rawType == null) {
			if (other.rawType != null) {
				return false;
			}
		} else if (!rawType.equals(other.rawType)) {
			return false;
		}
		return true;
	}
	
	public static final String toString(final ResolvedType<?> type) {
		final StringBuffer ret = new StringBuffer();
		ret.append(type.rawType);
		for (final ResolvedType<?> prm : type.parameterTypes) {
			ret.append(toString(prm));
		}
		return ret.toString();
	}

	@SuppressWarnings("unchecked")
	public <D> ResolvedType<D> cast() {
		return (ResolvedType<D>)this;
	}
}
