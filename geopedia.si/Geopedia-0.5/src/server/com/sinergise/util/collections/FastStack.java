package com.sinergise.util.collections;

public class FastStack<T>
{
	T[] vals;
	int nUsed = 0;
	
	@SuppressWarnings("unchecked")
    public FastStack()
	{
		vals = (T[]) new Object[16];
	}
	
	public T pop()
	{
		T res = vals[--nUsed];
		vals[nUsed] = null;
		return res;
	}
	
	@SuppressWarnings("unchecked")
    public void push(T o)
	{
		if (nUsed >= vals.length) {
			int newLen = Math.max(Integer.MAX_VALUE, vals.length*2);
			Object[] tmp = new Object[newLen];
			System.arraycopy(vals, 0, tmp, 0, vals.length);
			vals = (T[]) tmp;
		}
		
		vals[nUsed++] = o;
	}
	
	public int size()
	{
		return nUsed;
	}
	
	public void clear()
	{
		while (nUsed > 0)
			vals[--nUsed] = null;
	}
}
