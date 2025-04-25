package com.sinergise.common.util.lang;

//TODO: Implement Callable<R> and make sure this works in GWT (add interface to emul if necessary)
public interface SGCallable<R> /*extends Callable<R>*/ {
//	@Override
	R call();
}
