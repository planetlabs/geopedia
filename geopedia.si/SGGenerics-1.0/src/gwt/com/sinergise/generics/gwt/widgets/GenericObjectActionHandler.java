package com.sinergise.generics.gwt.widgets;

import com.sinergise.generics.core.EntityObject;
import com.sinergise.generics.core.GenericObjectProperty;

public interface GenericObjectActionHandler {
	void actionInvoked(GenericObjectProperty p, EntityObject eo);
}
