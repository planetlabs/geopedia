package com.sinergise.geopedia.style.processor;

public class Pair<T,S>
{
	public final T first;
	public final S second;
	
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

    public int hashCode()
    {
	    return 31 * (31 + ((first == null) ? 0 : first.hashCode())) + ((second == null) ? 0 : second.hashCode());
    }

    @SuppressWarnings("unchecked")
    public boolean equals(Object obj)
    {
	    if (this == obj)
		    return true;
	    if (obj == null)
		    return false;
	    if (getClass() != obj.getClass())
		    return false;
	    final Pair other = (Pair) obj;
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
	
}
