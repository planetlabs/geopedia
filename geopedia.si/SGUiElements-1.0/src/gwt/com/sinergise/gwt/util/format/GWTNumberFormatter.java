/**
 * 
 */
package com.sinergise.gwt.util.format;

import com.google.gwt.i18n.client.CurrencyList;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.i18n.client.constants.NumberConstants;
import com.sinergise.common.util.format.NumberFormatter;
import com.sinergise.common.util.format.NumberFormatProvider.NumberFormatConstants;
import com.sinergise.common.util.lang.number.SGNumber;
import com.sinergise.common.util.lang.number.SGNumberApiMapping.RepresentableAsBigDecimal;
import com.sinergise.common.util.lang.number.SGNumberApiMapping.RepresentableAsDouble;

public class GWTNumberFormatter extends NumberFormat implements NumberFormatter {
	
	public static class ExtNumberConstants implements NumberConstants {
		NumberConstants nc       = LocaleInfo.getCurrentLocale().getNumberConstants();
		private String  decSep   = nc.decimalSeparator();
		private String  groupSep = nc.groupingSeparator();
		
		public ExtNumberConstants(final NumberFormatConstants constants) {
			this.decSep = constants.decimalSeparator;
			this.groupSep = constants.groupingSeparator;
		}
		
		@Override
		public String currencyPattern() {
			return nc.currencyPattern();
		}
		
		@Override
		public String decimalPattern() {
			return nc.decimalPattern();
		}
		
		@Override
		public String defCurrencyCode() {
			return nc.defCurrencyCode();
		}
		
		@Override
		public String exponentialSymbol() {
			return nc.exponentialSymbol();
		}
		
		@Override
		public String infinity() {
			return nc.infinity();
		}
		
		@Override
		public String minusSign() {
			return nc.minusSign();
		}
		
		@Override
		public String monetaryGroupingSeparator() {
			return nc.monetaryGroupingSeparator();
		}
		
		@Override
		public String monetarySeparator() {
			return nc.monetarySeparator();
		}
		
		@Override
		public String notANumber() {
			return nc.notANumber();
		}
		
		@Override
		public String percent() {
			return nc.percent();
		}
		
		@Override
		public String percentPattern() {
			return nc.percentPattern();
		}
		
		@Override
		public String perMill() {
			return nc.perMill();
		}
		
		@Override
		public String plusSign() {
			return nc.plusSign();
		}
		
		@Override
		public String scientificPattern() {
			return nc.scientificPattern();
		}
		
		@Override
		public String zeroDigit() {
			return nc.zeroDigit();
		}
		
		@Override
		public String decimalSeparator() {
			return decSep;
		}
		
		@Override
		public String groupingSeparator() {
			return groupSep;
		}
	}
	
	public GWTNumberFormatter(final String pattern, final NumberFormatConstants constants) {
		super(new ExtNumberConstants(constants), pattern, CurrencyList.get().getDefault(), true);
	}
	
	@Override
	public String format(final double number) {
		return super.format(number);
	}
	
	public String format(final SGNumber number) {
		if (number instanceof RepresentableAsDouble) {
			return format(((RepresentableAsDouble)number).doubleValue());
		}
		if (number instanceof RepresentableAsBigDecimal) {
			return super.format(((RepresentableAsBigDecimal)number).bigDecimalValue());
		}
		throw new UnsupportedOperationException("Unsupported SGNumber type: "+number.getClass() +" value: "+number);
	}
}