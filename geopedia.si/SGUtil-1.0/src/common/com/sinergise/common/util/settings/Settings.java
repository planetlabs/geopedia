/*
 *
 */
package com.sinergise.common.util.settings;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.sinergise.common.util.string.StringTransformer;

public interface Settings {
	public static interface NeedsUpdateAfterDeserialization {
		void updateAfterDeserialization();
	}

	public static interface NeedsUpdateBeforeSerialization {
		void updateBeforeSerialization();
	}
	
	@Retention(RUNTIME)
	public static @interface TypeMap {
		Class<?>[] types();
		String[] names();
	}
	
	@Retention(RUNTIME)
	@Documented
	@Target({TYPE})
	public static @interface SerializeAsString {
		Class<? extends StringTransformer<?>> value(); 
	}
	
	@Retention(RUNTIME)
	@Documented
	@Target({FIELD, TYPE})	
	public static @interface SerializeAsComplex{}

	@Retention(RUNTIME)
	@Documented
	@Target({FIELD, TYPE})	
	public static @interface ItemsTagName {
		String value() default "item";
	}
}
