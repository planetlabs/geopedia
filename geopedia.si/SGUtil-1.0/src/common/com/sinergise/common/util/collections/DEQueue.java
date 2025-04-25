/*
 *
 */
package com.sinergise.common.util.collections;

import static com.sinergise.common.util.ArrayUtil.arraycopy;

import java.util.AbstractList;
import java.util.Collection;
import java.util.Iterator;
import java.util.RandomAccess;

import com.sinergise.common.util.lang.Predicate;

public class DEQueue<T> extends AbstractList<T> implements RandomAccess {
	Object[]	data;
	int			start, len, alloced;

	public DEQueue() {
		this(-1);
	}

	public DEQueue(int size) {
		if (size <= 0) size = 16;
		this.alloced = size;
		this.start = size >> 1;
		this.len = 0;
		this.data = new Object[alloced];
	}
	
	private void grow() {
		int ns = alloced / 2 * 3;
		if (ns <= alloced) ns = alloced + 2;
		if (ns <= alloced) ns = Integer.MAX_VALUE;
		if (ns <= alloced) throw new IllegalStateException("Out of memory");

		final Object[] ndata = new Object[ns];
		if (start + len <= alloced) {
			arraycopy(data, start, ndata, 0, len);
		} else {
			arraycopy(data, start, ndata, 0, alloced - start);
			arraycopy(data, 0, ndata, alloced - start, len - alloced + start);
		}
		start = 0;
		alloced = ns;
		data = ndata;
	}
	
	/**
	 * Inserts the element at the specified index
	 */
	@Override
	public void add(final int index, final Object o) {
		// ensure there is enough space for one more element
		if (len >= alloced) grow();
		
		if (index == len) {
			// if the element is added at the end, there's no need to
			// move any other elements
			if (start >= alloced - len) data[start - alloced + len] = o; // wrap
			else data[start + len] = o; // no wrap
			len++;
		} else if (index == 0) {
			// if the element is added at the beginning, there's also
			// no need to move any other elements
			start--;
			if (start < 0) start += alloced;
			data[start] = o;
			len++;
		} else {
			// otherwise, there are two cases
			if (start < alloced - len) {
				// the first case is that there is no wrap, even with the new
				// element
				arraycopy(data, start + index, data, start + index + 1, len - index);
				data[start + index] = o;
				len++;
			} else {
				// the other case involves wrapping
				// again, there are two cases
				if (start < alloced - index) {
					// the new element occurs before wrap
					
					final int ll = len - alloced + start;
					arraycopy(data, 0, data, 1, ll);
					data[0] = data[alloced - 1];
					arraycopy(data, start + index, data, start + index + 1, alloced - start - index - 1);
					data[start + index] = o;
					len++;
				} else {
					// the new element occurs after wrap
					
					final int ll = len - alloced + start;
					final int nindex = index - (alloced - start);
					arraycopy(data, nindex, data, nindex + 1, ll - nindex);
					data[nindex] = o;
					len++;
				}
			}
		}
	}
	
	@Override
	public boolean add(final Object o) {
		if (len >= alloced) grow();
		
		// if the element is added at the end, there's no need to
		// move any other elements
		if (start >= alloced - len) data[start - alloced + len] = o; // wrap
		else data[start + len] = o; // no wrap

		len++;
		return true;
	}
	
	@Override
	public int size() {
		return len;
	}
	
	@Override
	public void clear() {
		int pos = start;
		for (int a = 0; a < len; a++) {
			data[pos] = null;
			pos++;
			if (pos >= alloced) pos = 0;
		}
		len = 0;
	}
	
	@Override
	public boolean isEmpty() {
		return len == 0;
	}
	
	@Override
	public Object[] toArray() {
		final Object[] out = new Object[len];
		toArray(out);
		return out;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public T get(final int index) {
		if (index < 0 || index >= len) throw new IndexOutOfBoundsException();
		
		if (start >= alloced - index) return (T)data[start - alloced + index];
		return (T)data[start + index];
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public T remove(final int index) {
		if (index < 0 || index >= len) throw new IndexOutOfBoundsException();
		
		Object result;
		
		if (index == 0) {
			result = data[start];
			data[start] = null;
			start++;
			len--;
			if (start >= alloced) start -= alloced;
		} else if (index == len - 1) {
			if (start >= alloced - len + 1) {
				final int i = start - alloced + len - 1;
				result = data[i];
				data[i] = null;
			} else {
				final int i = start + len - 1;
				result = data[i];
				data[i] = null;
			}
			len--;
		} else {
			if (start <= alloced - len) {
				result = data[start + index];
				arraycopy(data, start + index + 1, data, start + index, len - index - 1);
				len--;
				data[start + len] = null;
			} else {
				if (start >= alloced - index) {
					final int ri = index - alloced + start;
					final int rl = len - alloced + start;
					result = data[ri];
					arraycopy(data, ri + 1, data, ri, rl - ri - 1);
					data[rl - 1] = null;
					len--;
				} else {
					final int rl = alloced - start - index - 1;
					result = data[start + index];
					arraycopy(data, start + index + 1, data, start + index, rl);
					data[alloced - 1] = data[0];
					final int cl = len - alloced + start - 1;
					arraycopy(data, 1, data, 0, cl);
					data[cl] = null;
					len--;
				}
			}
		}
		
		return (T)result;
	}
	
	@Override
	public int indexOf(final Object o) {
		int pos = start;
		if (o == null) {
			for (int a = 0; a < len; a++) {
				if (data[pos] == null) return a;
				pos++;
				if (pos >= alloced) pos = 0;
			}
		} else {
			for (int a = 0; a < len; a++) {
				if (o.equals(data[pos])) return a;
				pos++;
				if (pos >= alloced) pos = 0;
			}
		}
		
		return -1;
	}
	
	@Override
	public int lastIndexOf(final Object o) {
		int pos;
		if (start > alloced - len) pos = start - alloced + len - 1;
		else pos = start + len - 1;
		
		if (o == null) {
			for (int a = 0; a < len; a++) {
				if (data[pos] == null) return a;
				pos--;
				if (pos < 0) pos = alloced - 1;
			}
		} else {
			for (int a = 0; a < len; a++) {
				if (o.equals(data[pos])) return a;
				pos--;
				if (pos < 0) pos = alloced - 1;
			}
		}
		
		return -1;
	}
	
	@Override
	public boolean contains(final Object o) {
		return indexOf(o) >= 0;
	}
	
	@Override
	public boolean remove(final Object o) {
		final int i = indexOf(o);
		if (i >= 0) {
			remove(i);
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean containsAll(final Collection<?> c) {
		final Iterator<?> i = c.iterator();
		while (i.hasNext()) {
			if (indexOf(i.next()) < 0) return false;
		}
		return true;
	}
	
	@Override
	public boolean removeAll(final Collection<?> c) {
		if (c.isEmpty()) return false;
		
		int inpos = start;
		int outpos = start;
		int removed = 0;
		
		for (int a = 0; a < len; a++) {
			if (c.contains(data[inpos])) {
				inpos++;
				removed++;
			} else {
				data[outpos++] = data[inpos++];
				if (outpos >= alloced) outpos -= alloced;
				if (inpos >= alloced) inpos -= alloced;
			}
		}
		
		if (removed > 0) {
			while (removed > 0) {
				removed--;
				data[outpos++] = null;
				if (outpos >= alloced) outpos -= alloced;
			}
			len -= removed;
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean retainAll(final Collection<?> c) {
		if (c.isEmpty()) {
			clear();
			return true;
		}
		
		int inpos = start;
		int outpos = start;
		int removed = 0;
		
		for (int a = 0; a < len; a++) {
			if (c.contains(data[inpos])) {
				data[outpos++] = data[inpos++];
				if (outpos >= alloced) outpos -= alloced;
				if (inpos >= alloced) inpos -= alloced;
			} else {
				inpos++;
				removed++;
			}
		}
		
		if (removed > 0) {
			while (removed > 0) {
				removed--;
				data[outpos++] = null;
				if (outpos >= alloced) outpos -= alloced;
			}
			len -= removed;
			return true;
		}
		
		return false;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public T set(final int index, final T element) {
		if (index < 0 || index >= len) throw new IndexOutOfBoundsException();
		
		int i;
		if (start >= alloced - index) i = start - alloced + index;
		else i = start + index;

		final Object out = data[i];
		data[i] = element;
		return (T)out;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <U> U[] toArray(U[] a) {
		if (a == null || len > a.length) a = (U[])new Object[len]; ///XXX: Find a way to create an array with getClass() on GWT client-side

		if (start > alloced - len) {
			arraycopy(data, start, a, 0, alloced - start);
			arraycopy(data, 0, a, alloced - start, len - alloced + start);
		} else {
			arraycopy(data, start, a, 0, len);
		}
		if (a.length > len) a[len] = null;
		
		return a;
	}
	
	public void push(final T o) {
		add(o);
	}
	
	public T pop() {
		return remove(0);
	}
	
	public T peek() {
		return get(0);
	}

	public void remove(int index, int length) {
		if (index < 0 || index >= len) throw new IndexOutOfBoundsException();
		if (index+length > len) throw new IndexOutOfBoundsException();
		if (index == 0) {
			for (int i = 0; i < length; i++) {
				data[start] = null;
				start++;
				len--;
				if (start >= alloced) start -= alloced;
			}
		} else {
			for (int i = 0; i < length; i++) {
				remove(index);
			}
		}
	}

	public <A> A[] subListArray(int subListStart, int subListLen, A[] a) {
		if (a == null || subListLen > a.length) throw new IllegalArgumentException("Provided out array is null or wrong size");
		if (subListLen == 0) return a;
		
		final int i0 = (start + subListStart) % alloced;
		final int i1 = (i0 + subListLen) % alloced;
		
		if (i1 <= i0) {
			final int len1 = alloced - i0; 
			arraycopy(data, i0, a, 0, len1);
			arraycopy(data, 0, a, len1, subListLen - len1);
		} else {
			arraycopy(data, i0, a, 0, subListLen);
		}
		return a;
	}
	
	@SuppressWarnings("unchecked")
	public <A, C extends Collection<? super A>> C pickFromRange(int rangeStart, int rangeLen, Predicate<A> test, C ret) {
		if (rangeLen == 0) return ret;
		
		final int i0 = (start + rangeStart) % alloced;
		final int i1 = (i0 + rangeLen) % alloced;
		
		if (i1 <= i0) {
//			arraycopy(data, i0, a, 0, len1);
			for (int i = i0; i < alloced; i++) {
				final A o = (A)data[i];
				if (test.eval(o)) {
					ret.add(o);
				}
			}
			
			final int len2 = rangeLen - alloced + i0;
//			arraycopy(data, 0, a, len1, len2);
			for (int i = 0; i < len2; i++) {
				final A o = (A)data[i];
				if (test.eval(o)) ret.add(o);
			}
		} else {
//			arraycopy(data, i0, a, 0, rangeLen);
			for (int i = i0; i < i1; i++) {
				final A o = (A)data[i];
				if (test.eval(o)) ret.add(o);
			}
		}
		return ret;
	}
}
