package com.sinergise.java.util.xml;

import java.util.Map;

public class DefaultAttributes extends AttrsImplHelper {
	
	public static class Attr {
		public Attr(final String name, final String value) {
			super();
			this.name = name;
			this.value = value;
		}
		
		public final String name;
		public final String value;
	}
	
	public static final Attr[] fromMap(final Map<String, String> map) {
		if (map == null) {
			return null;
		}
		final Attr[] ret = new Attr[map.size()];
		int i = 0;
		for (final Map.Entry<String, String> e : map.entrySet()) {
			ret[i++] = new Attr(e.getKey(), e.getValue());
		}
		return ret;
	}
	
	public static final Attr[] fromArr(final String[][] attrs) {
		if (attrs == null || attrs.length == 0) {
			return null;
		}
		final Attr[] ret = new Attr[attrs.length];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = new Attr(attrs[i][0], attrs[i][1]);
		}
		return ret;
	}
	
	final Attr[] vals;
	
	public DefaultAttributes(final Attr... attributes) {
		super();
		this.vals = attributes;
	}
	
	public DefaultAttributes(final Map<String, String> map) {
		this(fromMap(map));
	}
	
	public DefaultAttributes(final String[][] attrs) {
		this(fromArr(attrs));
	}
	
	@Override
	public int getIndex(final String name) {
		if (name == null || vals == null || vals.length == 0) {
			return -1;
		}
		for (int i = 0; i < vals.length; i++) {
			if (name.equals(vals[i].name)) {
				return i;
			}
		}
		return -1;
	}
	
	@Override
	public int getLength() {
		return vals == null ? 0 : vals.length;
	}
	
	@Override
	public String getQName(final int index) {
		return vals[index].name;
	}
	
	@Override
	public String getValue(final int index) {
		return vals[index].value;
	}
}
