package com.sinergise.common.raster.index;

import java.io.Serializable;

import com.sinergise.common.raster.core.PyramidIndex;

/**
 * QuadTree index, storing 3 types of nodes:
 * <ul>
 * 	<li>EMPTY: the tile is missing, as are all its children</li>
 * 	<li>FULL: the tile's subtree is a completely full pyramid going to maxLevel</li>
 * 	<li>NORMAL: tile exists and has a subtree (may be all empty children in case of sparse pyramids)</li>
 * </ul>
 * 
 * This is appropriate for tile pyramids with the following properties:
 * <ul>
 * 	<li>Pyramid should be square</li>
 * 	<li>Matrix size should double on each subsequent level</li>
 * 	<li>Pyramid should be continuous (if there is data at a higher zoom level, there should be data at corresponding locations at lower zooms)</li>
 *  <li>Pyramid may be sparse, but the storage of such pyramids won't be very efficient 
 *  (a full pyramid with less-than-maximum depth will be represented by four empty nodes)</li>
 * </ul>
 *  
 * @author Miha
 */
public class SimpleQuadIdx extends AbstractQuadIdx implements Serializable {
	static abstract class SimpleQuadNode implements Serializable {
		public abstract boolean hasTile(int depth, int row, int col);

		public abstract String toString(String prefix);
		
		@Override
		public String toString() {
			return toString("");
		}
	}
	static final SimpleQuadNode EMPTY = new SimpleQuadNode() {
		@Override
		public boolean hasTile(int depth, int row, int col) {
			return false;
		}

		@Override
		public String toString(String prefix) {
			return "-";
		}
	};

	static final SimpleQuadNode FULL = new SimpleQuadNode() {
		@Override
		public boolean hasTile(int depth, int row, int col) {
			return true;
		}

		@Override
		public String toString(String prefix) {
			return "+";
		}
	};
	
	static final class NormalNode extends SimpleQuadNode {
		private final SimpleQuadNode[] children;
		
		public NormalNode(SimpleQuadNode[] children) {
			this.children = children;
		}
		
		@Override
		public boolean hasTile(int depth, int row, int col) {
			if (depth == 0) {
				return true;
			}
			int factor = factor(depth);
			SimpleQuadNode child = children[childIdx(row, col, factor)];
			return child.hasTile(depth-1, row % factor, col % factor);
		}

		@Override
		public String toString(String prefix) {
			StringBuilder ret = new StringBuilder().append("(");
			String newPref = prefix+" ";
			for (SimpleQuadNode child : children) {
				ret.append(child.toString(newPref));
			}
			ret.append(")\n").append(prefix);
			return ret.toString();
		}
	}
	
	private SimpleQuadNode root;
	SimpleQuadIdx(SimpleQuadNode root, int minIndexedLevel, int maxIndexedLevel) {
		super(minIndexedLevel, maxIndexedLevel);
		this.root = root;
	}

	@Override
	protected boolean hasTileRelativeToRootLevel(int depth, int row, int col) {
		return root.hasTile(depth, row, col);
	}
	
	public static SimpleQuadIdx create(PyramidIndex other) {
		return new QuadIdxBuilder(other).createSimple();
	}

	@Override
	public String toString() {
		return root.toString();
	}
}
