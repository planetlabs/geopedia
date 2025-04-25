package com.sinergise.util.collections;

import java.lang.reflect.Array;
import java.util.AbstractList;
import java.util.Collection;
import java.util.Iterator;
import java.util.RandomAccess;

public class Deque<T> extends AbstractList<T> implements RandomAccess
{
	T[] data;
	int start, len, alloced;

	public Deque()
	{
		this(16);
	}

	public Deque(int size)
	{
		if (size <= 0)
			size = 16;
		this.alloced = size;
		this.start = size >> 1;
		this.len = 0;
		this.data = (T[]) new Object[alloced];
	}

	private void grow()
	{
		int ns = alloced / 2 * 3;
		if (ns <= alloced)
			ns = alloced + 2;
		if (ns <= alloced)
			ns = Integer.MAX_VALUE;
		if (ns <= alloced)
			throw new OutOfMemoryError();

		Object[] ndata = new Object[ns];
		if (start + len <= alloced) {
			System.arraycopy(data, start, ndata, 0, len);
		} else {
			System.arraycopy(data, start, ndata, 0, alloced - start);
			System.arraycopy(data, 0, ndata, alloced - start, len - alloced + start);
		}
		start = 0;
		alloced = ns;
		data = (T[]) ndata;
	}

	public void add(int index, T o)
	{
		// ensure there is enough space for one more element
		if (len >= alloced) {
			grow();
		}

		if (index == len) {
			// if the element is added at the end, there's no need to
			// move any other elements
			if (start >= alloced - len) {
				// wrap
				data[start - alloced + len] = o;
			} else {
				// no wrap
				data[start + len] = o;
			}
			len++;
		} else if (index == 0) {
			// if the element is added at the beginning, there's also
			// no need to move any other elements
			start--;
			if (start < 0)
				start += alloced;
			data[start] = o;
			len++;
		} else {
			// otherwise, there are two cases
			if (start < alloced - len) {
				// the first case is that there is no wrap, even with the new
				// element
				System.arraycopy(data, start + index, data, start + index + 1, len - index);
				data[start + index] = o;
				len++;
			} else {
				// the other case involves wrapping
				// again, there are two cases
				if (start < alloced - index) {
					// the new element occurs before wrap

					int ll = len - alloced + start;
					System.arraycopy(data, 0, data, 1, ll);
					data[0] = data[alloced - 1];
					System.arraycopy(data, start + index, data, start + index + 1, alloced - start - index
					                - 1);
					data[start + index] = o;
					len++;
				} else {
					// the new element occurs after wrap

					int ll = len - alloced + start;
					int nindex = index - (alloced - start);
					System.arraycopy(data, nindex, data, nindex + 1, ll - nindex);
					data[nindex] = o;
					len++;
				}
			}
		}
	}

	public boolean add(T o)
	{
		if (len >= alloced) {
			grow();
		}

		// if the element is added at the end, there's no need to
		// move any other elements
		if (start >= alloced - len) {
			// wrap
			data[start - alloced + len] = o;
		} else {
			// no wrap
			data[start + len] = o;
		}
		len++;

		return true;
	}

	public int size()
	{
		return len;
	}

	public void clear()
	{
		int pos = start;
		for (int a = 0; a < len; a++) {
			data[pos] = null;
			pos++;
			if (pos >= alloced)
				pos = 0;
		}
		len = 0;
	}

	public boolean isEmpty()
	{
		return len == 0;
	}

	public Object[] toArray()
	{
		Object[] out = new Object[len];
		toArray(out);
		return out;
	}

	public T get(int index)
	{
		if (index < 0 || index >= len) {
			throw new IndexOutOfBoundsException();
		}
		if (start >= alloced - index) {
			return data[start - alloced + index];
		} else {
			return data[start + index];
		}
	}

	public T remove(int index)
	{
		if (index < 0 || index >= len)
			throw new IndexOutOfBoundsException();

		T result;

		if (index == 0) {
			result = data[start];
			data[start] = null;
			start++;
			len--;
			if (start >= alloced)
				start -= alloced;
		} else if (index == len - 1) {
			if (start >= alloced - len + 1) {
				int i = start - alloced + len - 1;
				result = data[i];
				data[i] = null;
			} else {
				int i = start + len - 1;
				result = data[i];
				data[i] = null;
			}
			len--;
		} else {
			if (start <= alloced - len) {
				result = data[start + index];
				System.arraycopy(data, start + index + 1, data, start + index, len - index - 1);
				len--;
				data[start + len] = null;
			} else {
				if (start >= alloced - index) {
					int ri = index - alloced + start;
					int rl = len - alloced + start;
					result = data[ri];
					System.arraycopy(data, ri + 1, data, ri, rl - ri - 1);
					data[rl - 1] = null;
					len--;
				} else {
					int rl = alloced - start - index - 1;
					result = data[start + index];
					System.arraycopy(data, start + index + 1, data, start + index, rl);
					data[alloced - 1] = data[0];
					int cl = len - alloced + start - 1;
					System.arraycopy(data, 1, data, 0, cl);
					data[cl] = null;
					len--;
				}
			}
		}

		return result;
	}

	public int indexOf(Object o)
	{
		int pos = start;
		if (o == null) {
			for (int a = 0; a < len; a++) {
				if (data[pos] == null)
					return a;
				pos++;
				if (pos >= alloced)
					pos = 0;
			}
		} else {
			for (int a = 0; a < len; a++) {
				if (o.equals(data[pos]))
					return a;
				pos++;
				if (pos >= alloced)
					pos = 0;
			}
		}

		return -1;
	}

	public int lastIndexOf(Object o)
	{
		int pos;
		if (start > alloced - len) {
			pos = start - alloced + len - 1;
		} else {
			pos = start + len - 1;
		}

		if (o == null) {
			for (int a = 0; a < len; a++) {
				if (data[pos] == null)
					return a;
				pos--;
				if (pos < 0)
					pos = alloced - 1;
			}
		} else {
			for (int a = 0; a < len; a++) {
				if (o.equals(data[pos]))
					return a;
				pos--;
				if (pos < 0)
					pos = alloced - 1;
			}
		}

		return -1;
	}

	public boolean contains(Object o)
	{
		return indexOf(o) >= 0;
	}

	public boolean remove(Object o)
	{
		int i = indexOf(o);
		if (i >= 0) {
			remove(i);
			return true;
		}

		return false;
	}

	public boolean containsAll(Collection<?> c)
	{
		Iterator<?> i = c.iterator();
		while (i.hasNext())
			if (indexOf(i.next()) < 0)
				return false;
		return true;
	}

	public boolean removeAll(Collection<?> c)
	{
		if (c.isEmpty()) {
			return false;
		}

		int inpos = start;
		int outpos = start;
		int removed = 0;

		for (int a = 0; a < len; a++) {
			if (c.contains(data[inpos])) {
				inpos++;
				removed++;
			} else {
				data[outpos++] = data[inpos++];
				if (outpos >= alloced)
					outpos -= alloced;
				if (inpos >= alloced)
					inpos -= alloced;
			}
		}

		if (removed > 0) {
			while (removed > 0) {
				removed--;
				data[outpos++] = null;
				if (outpos >= alloced)
					outpos -= alloced;
			}
			len -= removed;
			return true;
		}

		return false;
	}

	public boolean retainAll(Collection<?> c)
	{
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
				if (outpos >= alloced)
					outpos -= alloced;
				if (inpos >= alloced)
					inpos -= alloced;
			} else {
				inpos++;
				removed++;
			}
		}

		if (removed > 0) {
			while (removed > 0) {
				removed--;
				data[outpos++] = null;
				if (outpos >= alloced)
					outpos -= alloced;
			}
			len -= removed;
			return true;
		}

		return false;
	}

	public T set(int index, T element)
	{
		if (index < 0 || index >= len)
			throw new IndexOutOfBoundsException();

		int i;
		if (start >= alloced - index) {
			i = start - alloced + index;
		} else {
			i = start + index;
		}
		T out = data[i];
		data[i] = element;
		return out;
	}

	public <Q> Q[] toArray(Q[] a)
	{
		if (a == null) {
			a = (Q[]) new Object[len];
		} else if (len > a.length) {
			a = (Q[]) Array.newInstance(a.getClass().getComponentType(), len);
		}
		if (start > alloced - len) {
			System.arraycopy(data, start, a, 0, alloced - start);
			System.arraycopy(data, 0, a, alloced - start, len - alloced + start);
		} else {
			System.arraycopy(data, start, a, 0, len);
		}
		if (a.length > len)
			a[len] = null;

		return a;
	}
}
