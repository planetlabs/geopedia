package com.sinergise.java.ui.i18n;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import com.google.gwt.i18n.client.Constants;
import com.google.gwt.i18n.client.Constants.DefaultBooleanValue;
import com.google.gwt.i18n.client.Constants.DefaultDoubleValue;
import com.google.gwt.i18n.client.Constants.DefaultFloatValue;
import com.google.gwt.i18n.client.Constants.DefaultIntValue;
import com.google.gwt.i18n.client.Constants.DefaultStringArrayValue;
import com.google.gwt.i18n.client.Constants.DefaultStringMapValue;
import com.google.gwt.i18n.client.Constants.DefaultStringValue;
import com.google.gwt.i18n.client.ConstantsWithLookup;
import com.google.gwt.i18n.client.LocalizableResource.Key;
import com.google.gwt.i18n.server.GwtLocaleFactoryImpl;
import com.google.gwt.i18n.shared.GwtLocale;
import com.google.gwt.i18n.shared.GwtLocaleFactory;
import com.sinergise.common.ui.i18n.ResourceUtil;

public class ResourceCreatorJava implements ResourceUtil.ResourceCreator {
	private static final class ConstsReader implements InvocationHandler {
		
		private final ResourceBundle rb;
		private final Class<?> cls;
		
		public ConstsReader(Class<?> cls, ResourceBundle rb) {
			this.rb = rb;
			this.cls = cls;
		}
		
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			
			//Handle the case of ConstantsWithLookup
			if (ConstantsWithLookup.class.equals(method.getDeclaringClass()) && // 
				method.getParameterTypes().length == 1 && //
				String.class.equals(method.getParameterTypes()[0])) {

				method = cls.getMethod((String)args[0], new Class[0]);
			}
			
			Class<?> retType = method.getReturnType();
			
			if (String.class.equals(retType)) 			return readString(method);
			else if (String[].class.equals(retType))	return readStringArray(method);
			else if (Map.class.equals(retType))			return readStringMap(method);
			else if (Boolean.TYPE.equals(retType))		return Boolean.valueOf(readBoolean(method));
			else if (Double.TYPE.equals(retType))		return Double.valueOf(readDouble(method));
			else if (Float.TYPE.equals(retType))		return Float.valueOf(readFloat(method));
			else if (Integer.TYPE.equals(retType))		return Integer.valueOf(readInt(method));
			throw new UnsupportedOperationException("Only String and String[] are supported");
		}
		
		private static String keyFor(Method method) {
			Key k = method.getAnnotation(Key.class);
			if (k == null || k.value() == null) return method.getName();
			return k.value();
		}

		private int readInt(Method method) {
			String sVal = null;
			try {
				sVal = (rb != null) ? rb.getString(keyFor(method)) : null;
			} catch (MissingResourceException ignore) { }
			
			Integer ret = (sVal != null) ? Integer.valueOf(sVal) : null; 
			if (ret == null) {
				DefaultIntValue annot = method.getAnnotation(DefaultIntValue.class);
				if (annot != null) {
					ret = Integer.valueOf(annot.value());
				}
			}
			return ret == null ? 0 : ret.intValue();
		}

		private float readFloat(Method method) {
			String sVal = null;
			try {
				sVal  = (rb != null) ? rb.getString(keyFor(method)) : null;
			} catch (MissingResourceException ignore) { }
			
			Float ret = (sVal != null) ? Float.valueOf(sVal) : null; 
			if (ret == null) {
				DefaultFloatValue annot = method.getAnnotation(DefaultFloatValue.class);
				if (annot != null) {
					ret = Float.valueOf(annot.value());
				}
			}
			return ret == null ? Float.NaN : ret.floatValue();
		}

		private double readDouble(Method method) {
			String sVal = null;
			try {
				sVal = (rb != null) ? rb.getString(keyFor(method)) : null;
			} catch (MissingResourceException ignore) { }
			
			Double ret = (sVal != null) ? Double.valueOf(sVal) : null; 
			if (ret == null) {
				DefaultDoubleValue annot = method.getAnnotation(DefaultDoubleValue.class);
				if (annot != null) {
					ret = Double.valueOf(annot.value());
				}
			}
			return ret == null ? Double.NaN : ret.doubleValue();
		}

		private boolean readBoolean(Method method) {
			String sVal = null;
			try {
				sVal = (rb != null) ? rb.getString(keyFor(method)) : null;
			} catch (MissingResourceException ignore) { }
			
			Boolean ret = (sVal != null) ? Boolean.valueOf(sVal) : null; 
			if (ret == null) {
				DefaultBooleanValue annot = method.getAnnotation(DefaultBooleanValue.class);
				if (annot != null) {
					ret = Boolean.valueOf(annot.value());
				}
			}
			return ret == null ? false : ret.booleanValue();
		}

		private Map<String, String> readStringMap(Method method) {
			String[] keys = null;
			try {
				keys = (rb != null) ? rb.getStringArray(keyFor(method)) : null;
			} catch (MissingResourceException ignore) {}
			
			if (keys == null) {
				DefaultStringMapValue annot = method.getAnnotation(DefaultStringMapValue.class);
				if (annot != null && annot.value() != null) {
					String[] retArr = annot.value();
					Map<String, String> retMap = new HashMap<String, String>(retArr.length/2);
					for (int i = 0; i < retArr.length; i++) {
						retMap.put(retArr[i], retArr[++i]);
					}
					return retMap;
				}
				return Collections.emptyMap();
			} 
			Map<String, String> retMap = new HashMap<String, String>(keys.length);
			for (int i = 0; i < keys.length; i++) {
				retMap.put(keys[i], rb.getString(keys[i]));
			}
			return retMap;
		}

		private String readString(Method method) {
			String ret = null;
			try {
				ret = (rb != null) ? rb.getString(keyFor(method)) : null;
			} catch (MissingResourceException ignore) {} 
			
			if (ret == null) {
				DefaultStringValue annot = method.getAnnotation(DefaultStringValue.class);
				if (annot != null) {
					ret = annot.value();
				}
			}
			return ret;
		}
		
		private String[] readStringArray(Method method) {
			String[] ret = null;
			try {
				ret = (rb != null) ? rb.getStringArray(keyFor(method)) : null;
			} catch (MissingResourceException ignore) {}
			if (ret == null) {
				DefaultStringArrayValue annot = method.getAnnotation(DefaultStringArrayValue.class);
				if (annot != null) {
					ret = annot.value();
				}
			}
			return ret;
		}
	}
	
	private ThreadLocal<String> tlLocale = new ThreadLocal<String>();
	private GwtLocaleFactory factory = null;
	ResourceCreatorJava() {
	}
	
	public GwtLocale gwtLocaleFromString(String localeName) {
		 if (factory == null) factory = new GwtLocaleFactoryImpl();
		 return factory.fromString(localeName);
	}

	public void setThreadLocale(String locale) {
		tlLocale.set(locale);
	}
	
	public String getThreadLocale() {
		return tlLocale.get();
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Constants> T create(Class<T> cls) {
		return (T)createConstants((Class<? extends Constants>)cls);
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Constants> T createConstants(Class<T> cls) {
		ResourceBundle rb = null;
		try {
			Locale ll = Locale.getDefault();
			String setLocale = tlLocale.get();
			if (setLocale != null && !GwtLocale.DEFAULT_LOCALE.equals(setLocale)) {
				GwtLocale locale = gwtLocaleFromString(setLocale);
				ll = new Locale(locale.getLanguageNotNull(),locale.getRegionNotNull(),locale.getVariantNotNull());
			}
			rb = ResourceBundle.getBundle(cls.getName(), ll);
		} catch (Throwable t) {
		}
		return (T)Proxy.newProxyInstance(cls.getClassLoader(), new Class<?>[]{cls}, new ConstsReader(cls, rb));
	}

	public synchronized static void initialize() {
		if (!ResourceUtil.isInitialized()) {
			ResourceUtil.initResourceCreator(new ResourceCreatorJava());
		}
	}
}
