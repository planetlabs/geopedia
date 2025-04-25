package com.sinergise.util.io;

import java.lang.reflect.Array;

public abstract class ArrayUtils
{
	private ArrayUtils()
	{
	}

	public static Object[] append(Object[] base, Object value)
	{
		int baseLen = base.length;

		Object[] out = (Object[]) Array.newInstance(base.getClass().getComponentType(), baseLen + 1);
		System.arraycopy(base, 0, out, 0, baseLen);
		out[baseLen] = value;

		return out;
	}

	public static final int[] emptyInts = new int[0];
	public static final long[] emptyLongs = new long[0];

	public static void checkOffsetLength(int offset, int length, int arrLen)
	{
		if (length < 0 || offset < 0 || offset > arrLen - length)
			throw new IndexOutOfBoundsException();
	}
}
