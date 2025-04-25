package com.sinergise.common.util.string;

import java.io.IOException;

public class ListStringBuilder {
	private final Appendable out;
	private final String defaultStart;
	private final String defaultSeparator;
	private final String defaultEnd;

	private boolean atBeginning = true;

	public ListStringBuilder() {
		this(new StringBuilder());
	}

	public ListStringBuilder(Appendable out) {
		this(out, "(", ",", ")");
	}
	
	public ListStringBuilder(Appendable out, String startParen, String separator, String endParen) {
		this.out = out;
		this.defaultStart = startParen;
		this.defaultSeparator = separator;
		this.defaultEnd = endParen;
	}

	public ListStringBuilder(Appendable out, char startParen, char separator, char endParen) {
		this(out, Character.toString(startParen), Character.toString(separator), Character.toString(endParen));
	}

	public ListStringBuilder beforeListItem() {
		return beforeListItem(defaultSeparator);
	}
	
	public ListStringBuilder beforeListItem(String separator) {
		if (!atBeginning) {
			appendString(separator);
		}
		atBeginning = false;
		return this;
	}

	public ListStringBuilder startList() {
		return startList(defaultStart);
	}

	private ListStringBuilder startList(String start) {
		atBeginning = true;
		return appendString(start);
	}

	public ListStringBuilder endList() {
		return endList(defaultEnd);
	}
	
	public ListStringBuilder endList(String end) { 
		atBeginning = false;
		return appendString(end);
	}

	public ListStringBuilder appendChar(char ch) {
		try {
			out.append(ch);
			return this;
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	public ListStringBuilder appendDouble(double x) {
		return appendString(String.valueOf(x));
	}

	public ListStringBuilder appendString(String text) {
		try {
			out.append(text);
			return this;
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public ListStringBuilder appendListItem(String text) {
		beforeListItem();
		return appendString(text);
	}

	public ListStringBuilder appendList(String ... elements) {
		startList();
		for (String e : elements) {
			appendListItem(e);
		}
		return endList();
	}

	public ListStringBuilder appendList(Iterable<String> elements) {
		startList();
		for (String e : elements) {
			appendListItem(e);
		}
		return endList();
	}
	
	@Override
	public String toString() {
		return out.toString();
	}

	public Appendable getOutput() {
		return out;
	}
}