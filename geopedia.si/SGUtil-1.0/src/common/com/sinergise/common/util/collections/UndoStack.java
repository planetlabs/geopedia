/*
 *
 */
package com.sinergise.common.util.collections;

import java.util.ArrayList;
import java.util.List;

public class UndoStack<T> {
	
	private final List<UpdateListener> listeners = new ArrayList<UpdateListener>();
	
	private final ArrayList<T> stack   = new ArrayList<T>();
	private int                curPos  = 0;
	
	/**
	 * Max stack size - should be limited to prevent memory leaks. Integer.MIN_VALUE for no limit.
	 */
	private int                maxSize = Integer.MIN_VALUE;
	
	/**
	 * Creates a new limited size stack.
	 * 
	 * @param maxSize
	 */
	public UndoStack(final int maxSize) {
		this.maxSize = maxSize;
	}
	
	/**
	 * Creates a new stack with unlimited size.
	 */
	public UndoStack() {
		super();
	}
	
	public T getCurrent() {
		return stack.get(curPos);
	}
	
	/**
	 * @param obj
	 * @return true if the stack can reverse (i.e. it contains more than one value)
	 */
	public boolean store(final T cur) {
		// Remove all redo from curPos on
		for (int i = stack.size() - 1; i > curPos; i--) {
			stack.remove(i);
		}
		stack.add(cur);
		
		// check new stack size
		if (isLimited() && stack.size() > maxSize) {
			stack.remove(0); // remove oldest entry if too big
		}
		
		curPos = stack.size() - 1;
		fireUpdate();
		return curPos > 0;
	}
	
	public void replaceCurrent(final T cur) {
		stack.set(curPos, cur);
	}
	
	public boolean canGoBack() {
		return curPos > 0;
	}
	
	public boolean canGoForward() {
		return curPos < stack.size() - 1;
	}
	
	public T goBack() {
		if (curPos <= 0) {
			throw new IllegalStateException("Reached the end of undo stack.");
		}
		T ret = stack.get(--curPos);
		fireUpdate();
		return ret;
	}
	
	public T goForward() {
		if (curPos >= stack.size() - 1) {
			throw new IllegalStateException("Reached the end of redo stack.");
		}
		T ret = stack.get(++curPos);
		fireUpdate();
		return ret;
	}
	
	public void clear() {
		curPos = 0;
		stack.clear();
		fireUpdate();
	}
	
	private boolean isLimited() {
		return maxSize > Integer.MIN_VALUE;
	}
	
	public void addUpdateListener(UpdateListener l) {
		listeners.add(l);
	}
	
	public void removeUpdateListener(UpdateListener l) {
		listeners.remove(l);
	}
	
	protected void fireUpdate() {
		for (UpdateListener l : listeners) {
			l.stackUpdated();
		}
	}
	
	public interface UpdateListener {
		void stackUpdated();
	}
	
	@SuppressWarnings({"unchecked", "rawtypes"})
	public static void main(final String[] args) {
		final UndoStack testStack = new UndoStack();
		System.out.println(testStack.store("First"));
		System.out.println(testStack.store("Item 1"));
		System.out.println(testStack.store("Item 2"));
		testStack.store("Item 3");
		System.out.println("<-" + testStack.goBack());
		System.out.println("<-" + testStack.goBack());
		System.out.println("->" + testStack.goForward());
		System.out.println("->" + testStack.goForward());
		System.out.println("<-" + testStack.goBack());
		testStack.store("Item 3_again");
		testStack.store("Item 4_again");
		System.out.println("<-" + testStack.goBack());
		System.out.println("<-" + testStack.goBack());
		System.out.println("->" + testStack.goForward());
		System.out.println("->" + testStack.goForward());
		testStack.clear();
		System.out.println("---");
		System.out.println(testStack.store("First"));
		System.out.println(testStack.store("Item 1"));
		System.out.println(testStack.store("Item 2"));
		System.out.println("<-" + testStack.goBack());
		System.out.println("<-" + testStack.goBack());
		System.out.println("->" + testStack.goForward());
		System.out.println("->" + testStack.goForward());
		System.out.println("<-" + testStack.goBack());
	}
}
