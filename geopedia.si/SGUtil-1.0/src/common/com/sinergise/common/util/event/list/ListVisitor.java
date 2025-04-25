/*
 *
 */
package com.sinergise.common.util.event.list;

public interface ListVisitor {
	boolean visit(Object element, int index);
}
