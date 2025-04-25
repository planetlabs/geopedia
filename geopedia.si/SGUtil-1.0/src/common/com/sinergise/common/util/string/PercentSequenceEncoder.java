package com.sinergise.common.util.string;

import java.io.IOException;
import java.util.Arrays;

import com.sinergise.common.util.math.MathUtil;

public class PercentSequenceEncoder {

	private final char percent;
	private final char separator;
	private final String encPercent;
	private final String encSeparator;

	public PercentSequenceEncoder(char percent, char separator) {
		if (separator > 255) {
			throw new IllegalArgumentException("Only ASCII separators are allowed, was " + separator);
		}

		this.percent = percent;
		this.encPercent = percent + "25";
		this.separator = separator;
		this.encSeparator = percent + MathUtil.toHex(separator, 2).toString();
	}

	public String encodePart(CharSequence src) {
		StringBuilder sb = new StringBuilder(src.length());
		encodeAndAppendPart(src, sb);
		return sb.toString();
	}

	public void encodeAndAppendPart(CharSequence src, Appendable out) {
		try {
			int srcLen = src.length();
			for (int i = 0; i < srcLen; i++) {
				char ch = src.charAt(i);
				if (ch == percent) {
					out.append(encPercent);

				} else if (ch == separator) {
					out.append(encSeparator);

				} else {
					out.append(ch);
				}
			}
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	public String decodePart(String encodedStr) {
		return encodedStr.replace(encSeparator, Character.toString(separator)).replace(encPercent, Character.toString(percent));
	}

	public String[] decode(CharSequence encoded) {
		String[] ret = StringUtil.split(encoded, separator);
		for (int i = 0; i < ret.length; i++) {
			ret[i] = decodePart(ret[i]);
		}
		return ret;
	}

	public String encode(CharSequence... parts) {
		return encode(Arrays.asList(parts));
	}

	public String encode(Iterable<? extends CharSequence> parts) {
		int len = -1;
		for (CharSequence cs : parts) {
			if (len < 0) {
				len = 0;
			} else {
				len++;
			}
			len += cs.length();
		}
		if (len < 0) {
			throw new IllegalArgumentException("Cannot encode empty sequence");
		}
		StringBuilder ret = new StringBuilder(len);
		encodeAndAppend(ret, parts);
		return ret.toString();
	}

	public void encodeAndAppend(Appendable out, CharSequence... parts) {
		encodeAndAppend(out, Arrays.asList(parts));
	}

	public void encodeAndAppend(Appendable out, Iterable<? extends CharSequence> parts) {
		try {
			boolean first = true;
			for (CharSequence cs : parts) {
				if (first) {
					first = false;
				} else {
					out.append(separator);
				}
				out.append(encodePart(cs));
			}
			if (first) {
				throw new IllegalArgumentException("Cannot encode empty sequence");
			}
		} catch(IOException ex) {
			throw new RuntimeException(ex);
		}
	}
}
