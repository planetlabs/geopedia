package com.sinergise.common.util.lang;

public interface DeepCopyable <T extends DeepCopyable<?>> {

	T deepCopy();
	
}
