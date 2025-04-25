/*
 * Copyright (c) 2003 by Cosylab d.o.o.
 *
 * The full license specifying the redistribution, modification, usage and other
 * rights and obligations is included with the distribution of this project in
 * the file license.html. If the license is not included you may find a copy at
 * http://www.cosylab.com/legal/abeans_license.htm or may write to Cosylab, d.o.o.
 *
 * THIS SOFTWARE IS PROVIDED AS-IS WITHOUT WARRANTY OF ANY KIND, NOT EVEN THE
 * IMPLIED WARRANTY OF MERCHANTABILITY. THE AUTHOR OF THIS SOFTWARE, ASSUMES
 * _NO_ RESPONSIBILITY FOR ANY CONSEQUENCE RESULTING FROM THE USE, MODIFICATION,
 * OR REDISTRIBUTION OF THIS SOFTWARE.
 */

package com.sinergise.java.util.collections;

/**
 * A listener list is an efficient data structure for holding listeners. Its design is based on the fact that listeners are extensively
 * accessed for event dispatching, but the collection of listeners changes rarely. Under these circumstances (especilly rare invocations of
 * <code>addListener()</code> and <code>removeListener</code> methods) this class is a good choice. It holds listeners in an array that is
 * trimmed every time the listener membership changes (which is quite inefficient). On the other hand, accessing the elements of the array
 * is quick, <b>on the condition and contract that the client does not modify the array being returned</b>. Also note that the array is of
 * the correct run-time type specified by the constructor of this class, and can therefore be cast. For example, if you speficy
 * <code>WindowListener</code> as the RTT of this list, you can perform <code>WindowListener[] wla =
 * (WindowListener[])toArray()</code> without raising a <code>ClassCastException</code>. For cases where the listener membership changes
 * often, but events are dispatched rarely, use <code>ArrayList</code> instead. <b>This class is thread safe, there is no need for
 * additional synchronization.</b> See also swing based listener registration for an approach that is more time consuming during
 * dispatching, but can store different classes of listeners in a single array.
 * 
 * @author <a href="mailto:gasper.tkacik@cosylab.com">Gasper Tkacik</a>
 * @version $id$
 * @see javax.swing.event.EventListenerList
 */
public class ListenerList<T> extends ObjectList<T> {
	/**
	 * Constructs a new instance of the listener list that will be used to hold elements of type <code>type</code>. The specified class
	 * should be a subclass of <code>java.util.EventListener</code>.
	 * 
	 * @param type a run-time type of the elements in this list
	 * @throws NullPointerException DOCUMENT ME!
	 * @throws IllegalArgumentException DOCUMENT ME!
	 */
	public ListenerList(final Class<T> type) {
		super(type);
		
		if (type == null) {
			throw new NullPointerException("type");
		}
		
		if (!java.util.EventListener.class.isAssignableFrom(type)) {
			throw new IllegalArgumentException("Type '" + type + "' does not extend 'java.util.EventListener'.");
		}
	}
}

/* __oOo__ */
