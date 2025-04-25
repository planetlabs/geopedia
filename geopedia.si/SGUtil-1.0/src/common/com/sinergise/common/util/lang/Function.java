package com.sinergise.common.util.lang;

public interface Function<P, R> {
	class Util {
		private Util() {
		}
		public static <A, B, C> Function<A, C> compose(Function<A, B> f1, Function<B, C> f2) {
			return new CompositeFunction<A, C>(f1, f2);
		}
		public static <A> Function<A, A> identitiy() {
			return IdentityFunction.getInstance();
		}
	}
	
	class IdentityFunction<A> implements Function<A, A> {

		@SuppressWarnings("unchecked")
		public static final <T> IdentityFunction<T> getInstance() {
			return (IdentityFunction<T>)IDENTITY;
		} 
		
		@Override
		public A execute(A param) {
			return param;
		}
	}
	
	@SuppressWarnings({"unchecked","rawtypes"})
	class CompositeFunction<P, R> implements Function<P, R> {
		Function f1;
		Function f2;
		public <A> CompositeFunction(Function<P, A> f1, Function<A, R> f2) {
			this.f1 = f1;
			this.f2 = f2;
		}
		
		@Override
		public R execute(P param) {
			return (R)f2.execute(f1.execute(param));
		}
	}
	
	IdentityFunction<?> IDENTITY = new IdentityFunction<Object>();
	
	R execute(P param);
}
