package com.sinergise.common.util.collections.safe;

import com.sinergise.common.util.string.StringTransformUtil;
import com.sinergise.common.util.string.StringTransformer;


public abstract class StringTypedMapKey<T> extends DefaultTypeSafeKey<T> {
	@SuppressWarnings("unchecked")
	public static <E> StringTypedMapKey<E> create(String name, Class<E> klass) {
		if (String.class.equals(klass)) {
			return (StringTypedMapKey<E>)new ForString(name);
		}
		return new WithTransformer<E>(name, klass);
	}

	private static final long serialVersionUID = 2L;
	
	public static class ForString extends StringTypedMapKey<String> {
		private static final long serialVersionUID = 1L;
		
		public ForString(String name) {
			super(name);
		}
		@Override
		public String read(String valueString) {
			return valueString;
		}
		@Override
		public String write(String value) {
			return value;
		}
	}
	
	public static class WithTransformer<T> extends StringTypedMapKey<T> {
		private static final long serialVersionUID = 1L;
		
		private final StringTransformer<T> tr;
		public WithTransformer(String name, Class<T> klass) {
			this(name, StringTransformUtil.getTransformer(klass));
		}
		
		public WithTransformer(String name, StringTransformer<T> tr) {
			super(name);
			this.tr = tr;
		}
		
		@Override
		public T read(String valueString) {
			return tr.valueOf(valueString);
		}
		
		@Override
		public String write(T value) {
			return tr.store(value);
		}
	}
	
	public StringTypedMapKey(String name) {
		super(name);
	}
	
	public abstract String write(T value);
	
	public abstract T read(String valueString);
}
