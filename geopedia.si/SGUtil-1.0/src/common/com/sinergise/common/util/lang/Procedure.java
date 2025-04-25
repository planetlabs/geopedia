package com.sinergise.common.util.lang;


public abstract class Procedure<T> implements Function<T, Void> {
	@Override
	public final Void execute(T param) {
		call(param);
		return null;
	}

	protected abstract void call(T param);
}
