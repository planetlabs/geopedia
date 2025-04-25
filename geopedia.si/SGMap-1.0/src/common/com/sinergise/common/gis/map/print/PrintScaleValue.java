package com.sinergise.common.gis.map.print;

import java.io.Serializable;

import com.sinergise.common.util.string.HasCanonicalStringRepresentation;
import com.sinergise.common.util.string.StringUtil;

public class PrintScaleValue implements Serializable, HasCanonicalStringRepresentation {
	private static final String ID_FEATURES = "FEATURES";
	private static final String UI_ID_MANUAL = "MANUAL";

	public static final PrintScaleValue fromCanonicalString(String value) {
		if (StringUtil.isNullOrEmpty(value)) {
			return AUTO_FROM_ENVELOPE;
			
		} else if (ID_FEATURES.equals(value)) {
			return AUTO_FROM_FEATURES;
			
		} else {
			return new PrintScaleValue(Integer.parseInt(value));
		}
	}
	
	public static final PrintScaleValue fromUiValueString(String value) {
		if (UI_ID_MANUAL.equals(value)) {
			return MANUAL;
		}
		return fromCanonicalString(value);
	}
	
	public static abstract class AutoScale extends PrintScaleValue {
		public AutoScale(int value) {
			this.value = value;
		}
		@Override
		public boolean isAuto() {
			return true;
		}
		@Override
		public boolean isManual() {
			return false;
		}
	}
	
	private static final int AUTO_FROM_ENVELOPE_VALUE = -1;
	public static PrintScaleValue AUTO_FROM_ENVELOPE = new AutoScale(AUTO_FROM_ENVELOPE_VALUE) {
		@Override
		public String toCanonicalString() {
			return "";
		}
	};

	private static final int AUTO_FROM_FEATURES_VALUE = -2;
	public static PrintScaleValue AUTO_FROM_FEATURES = new AutoScale(AUTO_FROM_FEATURES_VALUE) {
		@Override
		public String toCanonicalString() {
			return ID_FEATURES;
		}
	};
	
	public static PrintScaleValue MANUAL = new PrintScaleValue() {
		@Override
		public boolean isAuto() {
			return false;
		}
		@Override
		public boolean isManual() {
			return true;
		}
		@Override
		public String toCanonicalString() {
			throw new UnsupportedOperationException("Can't canonicalize manual scale; should be used in UI only");
		}
		
		@Override
		public String toUiValueString() {
			return UI_ID_MANUAL;
		}
	};
	
	
	int value = -1;

	protected PrintScaleValue() {
	}
	
	public PrintScaleValue(int val) {
		if (val <= 0) {
			throw new IllegalArgumentException("Invalid scale: "+val);
		}
		this.value = val;
	}

	public boolean isAuto() {
		return false;
	}
	
	public boolean isManual() {
		return false;
	}
	
	@Override
	public String toCanonicalString() {
		return String.valueOf(value);
	}
	
	public String toUiValueString() {
		return toCanonicalString();
	}
	
	@Override
	public String toString() {
		return toCanonicalString();
	}

	public int getValue() {
		return value;
	}

	public boolean isFixed() {
		return value > 0;
	}

	@Override
	public int hashCode() {
		if (value <= 0) {
			return super.hashCode();
		}
		return value;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof PrintScaleValue)) {
			return false;
		}
		
		PrintScaleValue other = ((PrintScaleValue)obj); 
		
		return isAuto() == other.isAuto()
			&& value == other.value;
	}
	
	
}