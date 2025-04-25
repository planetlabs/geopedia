package com.sinergise.common.util.rpc;

import java.io.Serializable;


public interface ValueHolder extends Serializable {
	
	public static abstract class AValueHolder<V extends Serializable> implements ValueHolder {
		
		private static final long serialVersionUID = 1L;
		
		private V value;
		
		@Deprecated /** Serialization only */
		protected AValueHolder() { }
		
		public AValueHolder(V value) {
			this.value = value;
		}
		
		public V getValue() {
			return value;
		}
		
		@Override
		public String toString() {
			return String.valueOf(value);
		}
	}

}
