package com.sinergise.generics.core;

import java.util.ArrayList;

public class PrimitiveValueArray extends ArrayList<PrimitiveValue> implements ValueHolder {

	private static final long serialVersionUID = -4992030371981326272L;

	@Override
	public boolean isNull() {
		if (size()==0)
			return true;
		for (int i=0;i<size();i++) {
			PrimitiveValue pv = get(i);
			if (!pv.isNull())
				return false;
		}
		return true;
	}
}
