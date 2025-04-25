package com.sinergise.common.util.collections.tree;

public interface TreeNodeFilter<T> {
	boolean accept(T node);
}
