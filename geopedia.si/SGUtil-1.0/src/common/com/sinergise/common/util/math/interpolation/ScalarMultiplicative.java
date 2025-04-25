package com.sinergise.common.util.math.interpolation;

public interface ScalarMultiplicative<T> extends GroupAdditive<T> {
	T multiply(double scalar);
}
