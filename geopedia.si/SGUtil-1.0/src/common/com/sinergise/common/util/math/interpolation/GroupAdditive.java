package com.sinergise.common.util.math.interpolation;

public interface GroupAdditive<T> {
	T plus(T other);
	T opposite();
}
