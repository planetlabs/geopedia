/**
 * 
 */
package com.sinergise.common.util.format;

import com.sinergise.common.util.lang.number.SGNumber;

public interface NumberFormatter {
	String format(double number);
	String format(SGNumber number);
}