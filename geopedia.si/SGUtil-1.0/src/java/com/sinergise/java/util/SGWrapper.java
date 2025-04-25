package com.sinergise.java.util;

public interface SGWrapper<T> {
	public static class SGWrapperImpl<T> implements SGWrapper<T> {
		protected T wrappedObj;
		
		public SGWrapperImpl(T wrappedObj) {
			this.wrappedObj = wrappedObj;
		}
		
		public T get() {
			return wrappedObj;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public boolean isWrapperFor(Class<? extends T> type) {
			if (type.isInstance(this) || type.isInstance(wrappedObj)) {
				return true;
			}
			if (wrappedObj instanceof SGWrapper) {
				return ((SGWrapper<T>)wrappedObj).isWrapperFor(type);
			}
			return false;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public <S extends T> S unwrap(Class<S> type) {
			if (type.isInstance(this)) {
				return (S)this;
			}
			if (type.isInstance(wrappedObj)) {
				return (S)wrappedObj;
			}
			if (wrappedObj instanceof SGWrapper) {
				return ((SGWrapper<T>)wrappedObj).unwrap(type);
			}
			throw new IllegalArgumentException("Not wrapper for "+type+". Call isWrapperFor first");
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public T unwrap() {
			if (wrappedObj instanceof SGWrapper) {
				return ((SGWrapper<? extends T>)wrappedObj).unwrap();
			}
			return wrappedObj;
		}
	}
	
	boolean isWrapperFor(Class<? extends T> type);
	<S extends T> S unwrap(Class<S> type);
	T unwrap();
}
