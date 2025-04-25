package com.sinergise.common.raster.index;

import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferUShort;
import java.util.Arrays;
import java.util.HashMap;

import com.sinergise.common.geometry.tiles.TiledCRS;
import com.sinergise.common.raster.core.PyramidIndex;
import com.sinergise.common.raster.core.TilesIndex;
import com.sinergise.common.raster.index.SimpleQuadIdx.SimpleQuadNode;
import com.sinergise.common.util.math.MathUtil;

//TODO: Support general pyramids (sparse and/or missing parent nodes) and create specific read-only versions from that
public class QuadIdxBuilder extends AbstractQuadIdx implements TilesIndex.Mutable {
	private class PackedIndexCreator {
		private int size;
		private DataBuffer data;
		private HashMap<NormalBuilderNode, Integer> existing;
		public PackedQuadIdx create() {
			size = nodesCache.size() + 2;
			data = createBuffer();
			existing = new HashMap<NormalBuilderNode, Integer>(size);
			write(root, 0);
			return new PackedQuadIdx(QuadIdxBuilder.this.rootLevel, maxLevel, data);
		}
		
		private void write(QuadBuilderNode node, int dataIdx) {
			if (node == EMPTY) {
				data.setElem(dataIdx, PackedQuadIdx.EMPTY_ID);
				return;
			}
			if (node == FULL) {
				data.setElem(dataIdx, PackedQuadIdx.FULL_ID);
				return;
			}
			NormalBuilderNode normalNode = (NormalBuilderNode)node;
			Integer id = existing.get(normalNode);
			if (id == null) {
				id = writeNewNode(normalNode);
			}
			data.setElem(dataIdx, id.intValue());
		}

		public Integer writeNewNode(NormalBuilderNode normalNode) {
			Integer id = nextId();
			existing.put(normalNode, id);
			int off = PackedQuadIdx.getNodeStart(id.intValue());
			for (int i = 0; i < normalNode.children.length; i++) {
				write(normalNode.children[i], off + i);
			}
			return id;
		}

		private Integer nextId() {
			return Integer.valueOf(existing.size()+2);
		}

		private DataBuffer createBuffer() {
			int bufSize = 4*(size - 2) + 1;
			if (size <= 0xFF) {
				return new DataBufferByte(bufSize);
				
			} else if (size <= 0xFFFF) {
				return new DataBufferUShort(bufSize);
				
			} else if (size <= 0xFFFFFF) {
				return new DataBufferInt(bufSize);
				
			} else {
				throw new IllegalArgumentException("Too many distinct values to store in int buffer");
			}
		}
		
	}
	
	private abstract static class QuadBuilderNode {
		private SimpleQuadNode resultNode = null; 
		
		public abstract boolean hasTile(int depth, int row, int col);
	
		public abstract QuadBuilderNode set(int depth, int row, int col);
		
		public abstract QuadBuilderNode setFull(int depth, int row, int col);
	
		public abstract QuadBuilderNode clear(int depth, int row, int col);
	
		public abstract String toString(String prefix);
		
		@Override
		public String toString() {
			return toString("");
		}

		public final SimpleQuadNode getSimpleNode() {
			if (resultNode == null) {
				resultNode = createSimpleNode();
			}
			return resultNode;
		}

		protected abstract SimpleQuadNode createSimpleNode();
	}

	private final class NormalBuilderNode extends QuadBuilderNode implements Comparable<NormalBuilderNode> {

		private int use = 0;
		
		private void incUse() {
			use++;
			if (use == 1) {
				nodesCache.put(this, this);
			}
		}

		private void decUse() {
			use--;
			if (use <= 0) {
				nodesCache.remove(this);
			}
		}

		private final QuadBuilderNode[] children;
		
		private int hashCode;
		
		public NormalBuilderNode(QuadBuilderNode[] children) {
			this.children = children;
			this.hashCode = Arrays.hashCode(children);
		}
		
		@Override
		public int hashCode() {
			return hashCode;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof NormalBuilderNode)) {
				return false;
			}
			if (hashCode() != obj.hashCode()) {
				return false;
			}
			return Arrays.equals(children, ((NormalBuilderNode)obj).children);
		}
		
		@Override
		public boolean hasTile(int depth, int row, int col) {
			if (depth == 0) {
				return true;
			}
			int factor = factor(depth);
			int childIdx = childIdx(row, col, factor);
			QuadBuilderNode child = children[childIdx];
			return child.hasTile(depth-1, row % factor, col % factor);
		}
	
		@Override
		public QuadBuilderNode set(int depth, int row, int col) {
			if (depth == 0) {
				return this;
			}
			int factor = factor(depth);
			int childIdx = childIdx(row, col, factor);
			QuadBuilderNode newChild = children[childIdx].set(depth-1, row % factor, col % factor);
			return QuadIdxBuilder.this.createNode(this, newChild, childIdx);
		}
		
		@Override
		public QuadBuilderNode setFull(int depth, int row, int col) {
			if (depth == 0) {
				decUse();
				return FULL;
			}
			final int factor = factor(depth);
			final int childIdx = childIdx(row, col, factor);
			QuadBuilderNode newChild = children[childIdx].setFull(depth-1, row % factor, col % factor);
			return QuadIdxBuilder.this.createNode(this, newChild, childIdx);
		}
		
		@Override
		public QuadBuilderNode clear(int depth, int row, int col) {
			if (depth == 0) {
				decUse();
				return EMPTY;
			}
			final int factor = factor(depth);
			final int childIdx = childIdx(row, col, factor);
			QuadBuilderNode newChild = children[childIdx].clear(depth-1, row % factor, col % factor);
			return QuadIdxBuilder.this.createNode(this, newChild, childIdx);
		}
		
		@Override
		public String toString(String prefix) {
			StringBuilder ret = new StringBuilder().append(MathUtil.mod(hashCode,73)).append("(");
			boolean first = true;
			for (QuadBuilderNode child : children) {
				if (first) {
					first = false;
				} else {
					ret.append("|");
				}
				ret.append(MathUtil.mod(child.hashCode(), 73));
			}
			ret.append(")\n").append(prefix);
			return ret.toString();
		}
		
		@Override
		public int compareTo(NormalBuilderNode o) {
			return o.use - use;
		}
		
		@Override
		protected SimpleQuadNode createSimpleNode() {
			return new SimpleQuadIdx.NormalNode(createSimpleChildrenArr());
		}

		private SimpleQuadNode[] createSimpleChildrenArr() {
			SimpleQuadNode[] ret = new SimpleQuadNode[4];
			for (int i = 0; i < ret.length; i++) {
				ret[i] = children[i].getSimpleNode();
			}
			return ret;
		}
	}

	private final QuadBuilderNode EMPTY = new QuadBuilderNode() {
		@Override
		public boolean hasTile(int depth, int row, int col) {
			return false;
		}
	
		@Override
		public QuadBuilderNode set(int depth, int row, int col) {
			return createEmptyNode().set(depth, row, col);
		}
	
		@Override
		public QuadBuilderNode setFull(int depth, int row, int col) {
			if (depth == 0) {
				return FULL;
			}
			return createEmptyNode().setFull(depth, row, col);
		}
		
		@Override
		public QuadBuilderNode clear(int depth, int row, int col) {
			return this;
		}
	
		@Override
		public String toString(String prefix) {
			return "-";
		}
		@Override
		public boolean equals(Object obj) {
			return obj == this;
		}
		@Override
		public int hashCode() {
			return 0;
		}
		@Override
		protected SimpleQuadNode createSimpleNode() {
			return SimpleQuadIdx.EMPTY;
		}		
	};
	private final QuadBuilderNode FULL = new QuadBuilderNode() {
		@Override
		public boolean hasTile(int depth, int row, int col) {
			return true;
		}
		
		@Override
		public QuadBuilderNode set(int depth, int row, int col) {
			return this;
		}
		
		@Override
		public QuadBuilderNode setFull(int depth, int row, int col) {
			return this;
		}
	
		@Override
		public QuadBuilderNode clear(int depth, int row, int col) {
			if (depth == 0) {
				assert row == 0 && col == 0;
				return EMPTY;
			}
			return QuadIdxBuilder.this.createFullNode().clear(depth, row, col);
		}
	
		@Override
		public String toString(String prefix) {
			return "+";
		}
		
		@Override
		public boolean equals(Object obj) {
			return obj == this;
		}
		
		@Override
		public int hashCode() {
			return 1;
		}
		@Override
		protected SimpleQuadNode createSimpleNode() {
			return SimpleQuadIdx.FULL;
		}
	};

	private HashMap<NormalBuilderNode, NormalBuilderNode> nodesCache = new HashMap<NormalBuilderNode, NormalBuilderNode>();
	private NormalBuilderNode empty4 = new NormalBuilderNode(new QuadBuilderNode[] {EMPTY, EMPTY, EMPTY, EMPTY});
	private NormalBuilderNode full4 = new NormalBuilderNode(new QuadBuilderNode[] {FULL, FULL, FULL, FULL});

	QuadBuilderNode root = EMPTY;
	
	public QuadIdxBuilder(TilesIndex other) {
		this(other.getMinIndexedLevel(), other.getMaxIndexedLevel());
		fill(other, rootLevel, 0, 0);
	}

	public QuadIdxBuilder(int minIndexedLevel, int maxIndexedLevel) {
		super(minIndexedLevel, maxIndexedLevel);
//		nodesCache.put(empty4, empty4);
//		nodesCache.put(full4, full4);
	}

	@Override
	public void clear(int level, int row, int col) {
		root = root.clear(level-rootLevel, row, col);
	}

	@Override
	public void expandTo(int newMaxLevel) {
		maxLevel = newMaxLevel;
	}

	private void fill(TilesIndex other, int level, int i, int j) {
		if (other.hasTile(level, i, j)) {
			if (level == other.getMaxIndexedLevel()) {
				set(level, i, j);
				return;
			} 
			final int i2 = 2*i;
			final int j2 = 2*j;
			fill(other, level+1, i2, j2);
			fill(other, level+1, i2, j2 + 1);
			fill(other, level+1, i2 + 1, j2);
			fill(other, level+1, i2 + 1, j2 + 1);
		}
	}

	@Override
	public void set(int level, int row, int col) {
		assert level >= rootLevel && level <= maxLevel;
		if (level == maxLevel) {
			root = root.setFull(level-rootLevel, row, col);
		} else {
			root = root.set(level - rootLevel, row, col);
		}
	}

	public void setFullPyramid(int level, int row, int col) {
		assert level >= rootLevel && level <= maxLevel;
		root = root.setFull(level-rootLevel, row, col);
	}

	public SimpleQuadIdx createSimple() {
		return new SimpleQuadIdx(root.getSimpleNode(), rootLevel, maxLevel);
	}
	
	public PackedQuadIdx createPacked() {
		return new PackedIndexCreator().create();
	}

	@Override
	protected boolean hasTileRelativeToRootLevel(int depth, int row, int col) {
		return root.hasTile(depth, row, col);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		NormalBuilderNode[] nodes = nodesCache.keySet().toArray(new NormalBuilderNode[nodesCache.size()]);
		Arrays.sort(nodes);
		
		for (NormalBuilderNode node : nodes) {
			sb.append(node.use).append(':').append(node);
		}
		return nodesCache.size() + "\n" + sb.toString() + "\n" + root.toString();
	}

	private NormalBuilderNode createEmptyNode() {
		empty4.incUse();
		return empty4;
	}

	private NormalBuilderNode createFullNode() {
		full4.incUse();
		return full4;
	}

	private QuadBuilderNode createNode(NormalBuilderNode prev, QuadBuilderNode newChild, int index) {
		QuadBuilderNode[] newChildren = new QuadBuilderNode[4];
		newChildren[0] = prev.children[0];
		newChildren[1] = prev.children[1];
		newChildren[2] = prev.children[2];
		newChildren[3] = prev.children[3];
		newChildren[index] = newChild;
		
		return createNode(prev, newChildren, newChild);
	}
	
	private QuadBuilderNode createNode(NormalBuilderNode prev, QuadBuilderNode[] newChildren, QuadBuilderNode newChild) {
		prev.decUse();
		if (newChild == FULL || newChild == EMPTY) {
			QuadBuilderNode collapsed = tryCollapseFullOrEmpty(newChildren, newChild);
			if (collapsed != null) {
				return collapsed;
			}
		}
		NormalBuilderNode query = new NormalBuilderNode(newChildren);
		NormalBuilderNode cachedRet = nodesCache.get(query);
		if (cachedRet == null) {
			cachedRet = query;
		}
		cachedRet.incUse();
		return cachedRet;
	}

	private static QuadBuilderNode tryCollapseFullOrEmpty(QuadBuilderNode[] newChildren, QuadBuilderNode newChild) {
		for (QuadBuilderNode child : newChildren) {
			if (child != newChild) {
				return null;
			}
		}
		return newChild;
	}

	public PyramidIndex createOld(TiledCRS cs) {
		PyramidIndex ret = new PyramidIndex(cs, maxLevel);
		for (int s = rootLevel; s <= maxLevel; s++) {
			int size = 1<<s-rootLevel;
			for (int i = 0; i < size; i++) {
				for (int j = 0; j < size; j++) {
					if (hasTile(s, i, j)) {
						ret.set(s, i, j);
					}
				}
			}
		}
		ret.pack();
		return ret;
	}
}
