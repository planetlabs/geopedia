package com.sinergise.geopedia.rendering;

import java.awt.Paint;

import com.sinergise.geopedia.core.symbology.FillSymbolizer.GPFillType;

class PaintNode
{
	PaintNode prev, next;
	Paint paint;
	long colors;
	
	public void remove()
	{
		prev.next = next;
		next.prev = prev;
	}
	
	public void insertAfter(PaintNode node)
	{
		node.next = next;
		next.prev = node;

		node.prev = this;
		next = node;
	}
}

public class TextureCache
{
	int[] sizes = new int[GPFillType.values().length];
	PaintNode[] sentinels = new PaintNode[sizes.length];
	
	public Paint get(int bgColor, int fgColor, GPFillType fillType)
	{
		if (fillType == GPFillType.SOLID)
			fgColor = 0; // normalize
		
		long color = ((long)bgColor << 32) | (fgColor & 0xFFFFFFFFL);
		
		int num = sizes[fillType.ordinal()];
		if (num == 0) {
			sizes[fillType.ordinal()] = 1;
			PaintNode sentinel = sentinels[fillType.ordinal()] = new PaintNode();
			PaintNode val = sentinel.next = sentinel.prev = new PaintNode();
			val.prev = val.next = sentinel;
			val.colors = color;
			return val.paint = TextureMaker.getPaint(bgColor, fgColor, fillType);
		}
		
		PaintNode sentinel = sentinels[fillType.ordinal()];
		for (PaintNode node = sentinel.next; node != sentinel; node = node.next) {
			if (node.colors == color) {
				node.remove();
				sentinel.insertAfter(node);
				return node.paint;
			}
		}
		
		if (num < 8) {
			PaintNode val = new PaintNode();
			sentinel.insertAfter(val);
			val.colors = color;
			return val.paint = TextureMaker.getPaint(bgColor, fgColor, fillType);
		} else {
			PaintNode val = sentinel.prev;
			val.remove();
			sentinel.insertAfter(val);
			val.colors = color;
			return val.paint = TextureMaker.getPaint(bgColor, fgColor, fillType);
		}
	}
}
