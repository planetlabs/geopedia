package com.sinergise.common.util.format;

public interface NumberFormatProvider {
	public static class NumberFormatConstants {
		public String decimalSeparator;
		public String groupingSeparator;
		
		public NumberFormatConstants() {
		}
		public NumberFormatConstants(String dec, String grouping) {
			this.decimalSeparator = dec;
			this.groupingSeparator = grouping;
		}
		public NumberFormatConstants(char decSep, char groupingSep) {
			this.decimalSeparator = String.valueOf(decSep);
			this.groupingSeparator = String.valueOf(groupingSep);
		}
	}
	
	//TODO: try to remove constants in favour of something else that can be used in both Java and GWT
	NumberFormatter create(String pattern, NumberFormatConstants constants);
	
	NumberFormatter createDefaultDecimal(NumberFormatConstants constants);

	NumberFormatter createDefaultCurrency();
	
	//TODO: remove this
	NumberFormatConstants getDefaultConstants();

	//TODO: remove this
	NumberFormatConstants getConstants(Locale locale);
}
