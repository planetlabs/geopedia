package com.sinergise.common.util.web;

import com.sinergise.common.util.string.StringUtil;

public class WebUtil {
	
	public static boolean isValidEmail(final String email) {
		if (email == null) {
			return false;
		}
		
		return email.toLowerCase().matches("^[a-z0-9._%-]+@([a-z0-9-]+[.])+([a-z]{2}|com|org|net|biz|info|name|aero|biz|info|jobs|museum|name)$");
	}
	
	public static boolean isValidLogin(final String login) {
		if (login == null) {
			return false;
		}
		
		final int len = login.length();
		for (int a = 0; a < len; a++) {
			if (!isValidLoginChar(login.charAt(a), a)) {
				return false;
			}
		}
		return true;
	}
	
	private static boolean isValidLoginChar(char c, int index) {
		if (c >= 'a' && c <= 'z') {
			return true;
		}
		if (c >= 'A' && c <= 'Z') {
			return true;
		}
		if (c >= '0' && c <= '9' && index > 0) {
			return true;
		}
		if (c == '_') {
			return true;
		}
		return false;
	}

	public static boolean isGoodEnoughPassword(final String password) {
		if (password == null) {
			return false;
		}
		
		if (!password.equals(password.trim())) {
			return false;
		}
		
		if (password.length() < 6) {
			return false;
		}
		
		return true;
	}
	
	public static final String toHTMLColor(final int color) {
		return "#" + StringUtil.padWith(Integer.toHexString(color & 0x00FFFFFF).toUpperCase(), '0', 6, true);
	}
	
}
