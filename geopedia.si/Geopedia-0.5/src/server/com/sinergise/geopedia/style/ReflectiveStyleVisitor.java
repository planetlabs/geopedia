package com.sinergise.geopedia.style;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import com.sinergise.geopedia.core.style.StyleVisitor;
import com.sinergise.geopedia.core.style.model.StyleSpecPart;

public abstract class ReflectiveStyleVisitor implements StyleVisitor
{
	private static final HashMap<Class<?>,ConcurrentHashMap<Class<?>, Method>> cache = new HashMap<Class<?>, ConcurrentHashMap<Class<?>,Method>>();
	
	public ReflectiveStyleVisitor()
	{
		synchronized(cache) {
			ConcurrentHashMap<Class<?>, Method> fifu = cache.get(this.getClass());
			if (fifu == null) {
				fifu = new ConcurrentHashMap<Class<?>, Method>();
				cache.put(this.getClass(), fifu);
			}
			classToMethod = fifu;
		}
	}
	private final ConcurrentHashMap<Class<?>, Method> classToMethod;

	public boolean visitSSP(StyleSpecPart o, boolean entering)
	{
		return true;
	}

	public final boolean visit(StyleSpecPart o, boolean entering)
	{
		Method m = getMethod(o.getClass());
		try {
			return ((Boolean) m.invoke(this, new Object[] { o, entering ? Boolean.TRUE : Boolean.FALSE })).booleanValue();
		} catch (Exception e) {
			e.printStackTrace();
			// ignore
		}

		return true;
	}

	private Method getMethod(Class<?> visitableClass)
	{
		Method m = classToMethod.get(visitableClass);
		if (m != null)
			return m;

		Class<?> visitorClass = getClass();
		Class<?> newc = visitableClass;

		// Try the superclasses
		while (m == null && newc != StyleSpecPart.class) {
			try {
				m = visitorClass.getMethod("visit", new Class[] { newc, boolean.class });
				if (m.getReturnType() != boolean.class) {
					m = null;
					newc = newc.getSuperclass();
				}
			} catch (NoSuchMethodException e) {
				newc = newc.getSuperclass();
			}
		}

		if (m == null) {
			try {
				m = visitorClass.getMethod("visitSSP", new Class[] { StyleSpecPart.class, boolean.class });
			} catch (Exception e) {
				// Can't happen
				throw new InternalError();
			}
		}

		// by Miha to prevent funny java.lang.IllegalAccessException on inner classes (non-public)
		m.setAccessible(true);
		classToMethod.put(visitableClass, m);
		return m;
	}
}