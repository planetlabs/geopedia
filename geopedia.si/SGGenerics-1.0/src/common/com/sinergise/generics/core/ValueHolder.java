package com.sinergise.generics.core;

import com.sinergise.common.util.settings.Settings;
import com.sinergise.common.util.settings.Settings.TypeMap;

@TypeMap(
		names = {"",					"ENTITY"},
		types = {PrimitiveValue.class, 	AbstractEntityObject.class}
		)
public interface ValueHolder extends Settings {
	public boolean isNull();
}
