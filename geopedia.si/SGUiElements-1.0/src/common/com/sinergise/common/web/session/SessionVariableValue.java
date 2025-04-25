package com.sinergise.common.web.session;

import java.io.Serializable;

/**
 * Marker interface so that GWT can serialize session variables without complaints 
 */
public interface SessionVariableValue extends Serializable {

	
	public static abstract class ASessionVariableValue<T extends Serializable> implements SessionVariableValue {
		private static final long serialVersionUID = -2496843830222588609L;
		
		private T value;
		
		@Deprecated /** Serilization only */
		protected ASessionVariableValue() { }
		
		public ASessionVariableValue(T value) {
			this.value = value;
		}
		
		public T getValue() {
			return value;
		}
		
		@Override
		public String toString() {
			return String.valueOf(value);
		}
	}
	
	public static class StringSessionVariableValue extends ASessionVariableValue<String> {
		private static final long serialVersionUID = 6412830132142606011L;

		@Deprecated /** Serilization only */
		protected StringSessionVariableValue() { }
		
		public StringSessionVariableValue(String value) {
			super(value);
		}
	}
	
	public static class BooleanSessionVariableValue extends ASessionVariableValue<Boolean> {
		private static final long serialVersionUID = 6412830132142606011L;

		@Deprecated /** Serilization only */
		protected BooleanSessionVariableValue() { }
		
		public BooleanSessionVariableValue(boolean value) {
			super(Boolean.valueOf(value));
		}
		
		public BooleanSessionVariableValue(Boolean value) {
			super(value);
		}
	}
	
	public static class NumberSessionVariableValue extends ASessionVariableValue<Number> {
		private static final long serialVersionUID = 6412830132142606011L;

		@Deprecated /** Serilization only */
		protected NumberSessionVariableValue() { }
		
		public NumberSessionVariableValue(Number value) {
			super(value);
		}
	}
	
}
