package com.sinergise.common.util.lang;

import java.io.Serializable;
import java.util.Comparator;

import com.sinergise.common.util.Util;

@SuppressWarnings("rawtypes")
public class Pair<T, S> implements Serializable, Comparable<Pair<? extends T, ? extends S>>
{
	private static final long serialVersionUID = 1L;

	public static final class PairComparator implements Comparator<Pair<?,?>> {
		private final boolean nullLast;
		private final boolean firstThenSecond;
		public PairComparator() {
			this(true);
		}
		public PairComparator(boolean firstThenSecond) {
			this(firstThenSecond, true);
		}
		public PairComparator(boolean firstThenSecond, boolean nullLast) {
			this.firstThenSecond = firstThenSecond;
			this.nullLast = nullLast;
		}

		@Override
		@SuppressWarnings("unchecked")
		public int compare(Pair<?,?> o1, Pair<?,?> o2) {
			if (firstThenSecond) {
				int cmp = Util.safeCompare(((Comparable)o1.getFirst()),o2.getFirst(), nullLast);
				if (cmp != 0) {
					return cmp;
				}
				return Util.safeCompare(((Comparable)o1.getSecond()), o2.getSecond(), nullLast);
			}
			int cmp = Util.safeCompare(((Comparable)o1.getSecond()),o2.getSecond(), nullLast);
			if (cmp != 0) {
				return cmp;
			}
			return Util.safeCompare(((Comparable)o1.getFirst()),o2.getFirst(), nullLast);
		}
	}
	/**
	 * should be final but GWT serialization prevents it
	 */
	private T first;
	/**
	 * should be final but GWT serialization prevents it
	 */
	private S second;
	
	//generic convenience constructor
	public static <T, S> Pair<T, S> newPair(T first, S second) {
		return new Pair<T,S>(first, second);
	}
	
	/**
	 * @deprecated GWT serialisation
	 */
	@Deprecated
	protected Pair() {
	}
	
	public Pair(T first, S second)
	{
		this.first = first;
		this.second = second;
	}
	
	public T getFirst()
	{
		return first;
	}
	
	public S getSecond()
	{
		return second;
	}

    @Override
	public int hashCode()
    {
	    return 31 * (31 + ((first == null) ? 0 : first.hashCode())) + ((second == null) ? 0 : second.hashCode());
    }
    
    @Override
    public boolean equals(Object obj)
    {
	    if (this == obj)
		    return true;
	    if (obj == null)
		    return false;
	    if (getClass() != obj.getClass())
		    return false;
		final Pair<?, ?> other = (Pair<?, ?>) obj;
	    if (first == null) {
		    if (other.first != null)
			    return false;
	    } else if (!first.equals(other.first))
		    return false;
	    if (second == null) {
		    if (other.second != null)
			    return false;
	    } else if (!second.equals(other.second))
		    return false;
	    return true;
    }

	@Override
	@SuppressWarnings("unchecked")
	public int compareTo(Pair<? extends T, ? extends S> o) {
		final int cmp = Util.safeCompare(((Comparable<? super T>)first), o.first, false);
		if (cmp != 0) {
			return cmp;
		}
		return Util.safeCompare(((Comparable<? super S>)second), o.second, false);
	}

	public boolean contains(Object e) {
		return Util.safeEquals(first, e) || Util.safeEquals(second, e);
	}
	
}
