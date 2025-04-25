package com.sinergise.common.geometry.topo;

import java.util.Iterator;
import java.util.NoSuchElementException;

public abstract class RingIterator implements Iterator<DirectedEdge> {
	
	protected final ITopoMap map;
	protected final DirectedEdge firstEdge; 
	protected DirectedEdge next = null;

	public RingIterator(ITopoMap map, DirectedEdge firstEdge) {
		this.map = map;
		this.firstEdge = firstEdge;
		this.next = firstEdge;
	}
	
	@Override
	public boolean hasNext() {
		return next != null;
	}
	
	@Override
	public DirectedEdge next() {
		if (hasNext()) {
			DirectedEdge result = next;
			goNext();
			return result;
		}
		throw new NoSuchElementException();
	}
	
	private void goNext() {
		next = getNext();
		if (next.equals(firstEdge)) {
			next = null;
		}
	}
	
	protected abstract DirectedEdge getNext();
	
	@Override
	public void remove() {
		throw new UnsupportedOperationException("Modification not allowed");
	}
	
	public static class InnerRingIterator extends RingIterator {
		
		public InnerRingIterator(ITopoMap map, DirectedEdge firstEdge) {
			super(map, firstEdge);
		}
		
		@Override
		protected DirectedEdge getNext() {
			return map.getNodeStar(next, false).right(); //go left
		}
		
	}
	
	public static class OuterRingIterator extends RingIterator {
		
		public OuterRingIterator(ITopoMap map, DirectedEdge firstEdge) {
			super(map, firstEdge);
		}
		
		@Override
		protected DirectedEdge getNext() {
			return map.getNodeStar(next, false).left(); //go right;
		}
		
	}
	
	public static class InnerRingIterable implements Iterable<DirectedEdge> {
		
		private final ITopoMap map;
		private final DirectedEdge firstEdge;
		
		public InnerRingIterable(ITopoMap map, Node leftmostNode) {
			this(map, map.getNodeStar(leftmostNode).getLast()); //go CCW
		}
		
		public InnerRingIterable(ITopoMap map, DirectedEdge firstEdge) {
			this.map = map;
			this.firstEdge = firstEdge;
		}
		
		@Override
		public Iterator<DirectedEdge> iterator() {
			return new InnerRingIterator(map, firstEdge);
		}
	}
	
	public static class OuterRingIterable implements Iterable<DirectedEdge> {
		
		private final ITopoMap map;
		private final DirectedEdge firstEdge;
		
		public OuterRingIterable(ITopoMap map, Node leftmostNode) {
			this(map, map.getNodeStar(leftmostNode).getLast()); //go CCW
		}
		
		public OuterRingIterable(ITopoMap map, DirectedEdge firstEdge) {
			this.map = map;
			this.firstEdge = firstEdge;
		}
		
		@Override
		public Iterator<DirectedEdge> iterator() {
			return new OuterRingIterator(map, firstEdge);
		}
	}
}
