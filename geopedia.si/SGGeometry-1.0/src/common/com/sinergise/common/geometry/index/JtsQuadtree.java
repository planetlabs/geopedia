package com.sinergise.common.geometry.index;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.sinergise.common.geometry.geom.Geometry;
import com.sinergise.common.util.collections.SearchItemReceiver;
import com.sinergise.common.util.collections.SearchItemReceiver.ListCollector;
import com.sinergise.common.util.geom.Envelope;
import com.sinergise.common.util.geom.EnvelopeBuilder;
import com.sinergise.common.util.math.MathUtil;

/**
 * A Quadtree is a spatial index structure for efficient range querying of items bounded by 2D rectangles.
 * {@link Geometry}s can be indexed by using their {@link Envelope}s. Any type of Object can also be indexed as long as
 * it has an extent that can be represented by an {@link Envelope}.
 * <p>
 * This Quadtree index provides a <b>primary filter</b> for range rectangle queries. The various query methods return a
 * list of all items which <i>may</i> intersect the query rectangle. Note that it may thus return items which do
 * <b>not</b> in fact intersect the query rectangle. A secondary filter is required to test for actual intersection
 * between the query rectangle and the envelope of each candidate item. The secondary filter may be performed
 * explicitly, or it may be provided implicitly by subsequent operations executed on the items (for instance, if the
 * index query is followed by computing a spatial predicate between the query geometry and tree items, the envelope
 * intersection check is performed automatically.
 * <p>
 * This implementation does not require specifying the extent of the inserted items beforehand. It will automatically
 * expand to accomodate any extent of dataset.
 * <p>
 * This data structure is also known as an <i>MX-CIF quadtree</i> following the terminology of Samet and others.
 * 
 * @version 1.7
 */
public class JtsQuadtree<E> implements Serializable {
	/**
	 * The base class for nodes in a {@link JtsQuadtree}.
	 * 
	 * @version 1.7
	 */
	public static abstract class JtsNodeBase<E> implements Serializable {
		private static final long serialVersionUID = 1L;

		protected ArrayList<E> items = new ArrayList<E>();

		/**
		 * subquads are numbered as follows:
		 * 
		 * <pre>
		 *  2 | 3
		 *  --+--
		 *  0 | 1
		 * </pre>
		 */
		@SuppressWarnings("unchecked")
		protected JtsQuadNode<E>[] subnode = new JtsQuadNode[4];

		public JtsNodeBase() {}

		public boolean hasItems() {
			return !items.isEmpty();
		}

		public void add(E item) {
			items.add(item);
		}

		/**
		 * Removes a single item from subtree.
		 * @return <code>true</code> if the item was found and removed
		 */
		public boolean remove(Envelope itemEnv, Object item) {
			// use envelope to restrict nodes scanned
			if (!isSearchMatch(itemEnv)) {
				return false;
			}
			for (int i = 0; i < 4; i++) {
				if (subnode[i] != null && subnode[i].remove(itemEnv, item)) {
					// trim subtree if empty
					if (subnode[i].isPrunable()) {
						subnode[i] = null;
					}
					return true;
				}
			}
			// otherwise, try and remove the item from the list of items in this node
			return items.remove(item);
		}
		
		/**
		 * Removes a single item from subtree by scanning the whole tree.
		 * @return <code>true</code> if the item was found and removed
		 */
		public boolean remove(Object item) {
			for (int i = 0; i < 4; i++) {
				if (subnode[i] != null && subnode[i].remove(item)) {
					// trim subtree if empty
					if (subnode[i].isPrunable()) {
						subnode[i] = null;
					}
					return true;
				}
			}
			// otherwise, try and remove the item from the list of items in this node
			return items.remove(item);
		}
		
		public boolean contains(Envelope itemEnv, Object item) {
			// use envelope to restrict nodes scanned
			if (!isSearchMatch(itemEnv)) {
				return false;
			}
			for (int i = 0; i < 4; i++) {
				if (subnode[i] != null && subnode[i].contains(itemEnv, item)) {
					return true;
				}
			}
			return items.contains(item);
		}

		public boolean isPrunable() {
			return !(hasChildren() || hasItems());
		}

		public boolean hasChildren() {
			for (JtsQuadNode<E> sn : subnode) {
				if (sn != null) {
					return true;
				}
			}
			return false;
		}

		protected abstract boolean isSearchMatch(Envelope searchEnv);

		/**
		 * @return true to continue search
		 */
		public boolean visit(Envelope searchEnv, SearchItemReceiver<? super E> visitor) {
			if (!isSearchMatch(searchEnv)) {
				return true;
			}
			if (!visitItems(visitor)) {
				return false;
			}
			for (JtsQuadNode<E> sn : subnode) {
				if (sn != null) {
					if (!sn.visit(searchEnv, visitor)) {
						return false;
					}
				}
			}
			return true;
		}

		private boolean visitItems(SearchItemReceiver<? super E> visitor) {
			for (E e : items) {
				if (!visitor.execute(e).booleanValue()) {
					return false;
				}
			}
			return true;
		}
	}
	
	static class JtsQuadNode<T> extends JtsNodeBase<T> {
		private static final long serialVersionUID = 1L;

		private Envelope env;
		private double centrex;
		private double centrey;

		JtsQuadNode(Envelope env) {
			this.env = env;
			centrex = (env.getMinX() + env.getMaxX()) / 2;
			centrey = (env.getMinY() + env.getMaxY()) / 2;
		}

		Envelope getEnvelope() {
			return env;
		}

		@Override
		protected boolean isSearchMatch(Envelope searchEnv) {
			return env.intersects(searchEnv);
		}

		/**
		 * Returns the subquad containing the envelope <tt>searchEnv</tt>. Creates the subquad if it does not already exist.
		 * 
		 * @return the subquad containing the search envelope
		 */
		JtsQuadNode<T> getNode(Envelope searchEnv) {
			int subnodeIndex = getSubnodeIndex(searchEnv, centrex, centrey);
			// if subquadIndex is -1 searchEnv is not contained in a subquad
			if (subnodeIndex != -1) {
				return getSubnode(subnodeIndex).getNode(searchEnv);
			}
			return this;
		}

		JtsNodeBase<T> find(Envelope searchEnv) {
			int subnodeIndex = getSubnodeIndex(searchEnv, centrex, centrey);
			if (subnodeIndex == -1)
				return this;
			if (subnode[subnodeIndex] != null) {
				// query lies in subquad, so search it
				JtsQuadNode<T> node = subnode[subnodeIndex];
				return node.find(searchEnv);
			}
			// no existing subquad, so return this one anyway
			return this;
		}

		void insertNode(JtsQuadNode<T> node) {
			assert env == null || env.contains(node.env);
			int index = getSubnodeIndex(node.env, centrex, centrey);
			if (node.env.getWidth() == 0.5 * env.getWidth()) {
				subnode[index] = node;
			} else {
				// the quad is not a direct child, so make a new child quad to contain it
				// and recursively insert the quad
				JtsQuadNode<T> childNode = createSubnode(index);
				childNode.insertNode(node);
				subnode[index] = childNode;
			}
		}

		/**
		 * If the subquad doesn't exist, create it
		 */
		private JtsQuadNode<T> getSubnode(int index) {
			if (subnode[index] == null) {
				subnode[index] = createSubnode(index);
			}
			return subnode[index];
		}

		private JtsQuadNode<T> createSubnode(int index) {
			return new JtsQuadNode<T>(createSubEnvelope(index));
		}

		private Envelope createSubEnvelope(int index) {
			switch (index) {
				case 0: return new Envelope(env.getMinX(), env.getMinY(), centrex, centrey);
				case 1: return new Envelope(centrex, env.getMinY(), env.getMaxX(), centrey);
				case 2: return new Envelope(env.getMinX(), centrey, centrex, env.getMaxY());
				case 3: return new Envelope(centrex, centrey,  env.getMaxX(), env.getMaxY());
				default: throw new IllegalArgumentException("index was "+index);
			}
		}

	}

	
	static class Root<E> extends JtsNodeBase<E> {
		private static final long serialVersionUID = 1L;
		public Root() {}
	
		/**
		 * Insert an item into the quadtree this is the root of.
		 */
		public void insert(Envelope itemEnv, E item) {
			int index = getSubnodeIndex(itemEnv, 0, 0);
			// if index is -1, itemEnv must cross the X or Y axis.
			if (index == -1) {
				add(item);
				return;
			}
			/**
			 * the item must be contained in one quadrant, so insert it into the tree for that quadrant (which may not yet
			 * exist)
			 */
			JtsQuadNode<E> node = subnode[index];
			/**
			 * If the subquad doesn't exist or this item is not contained in it, have to expand the tree upward to contain
			 * the item.
			 */
			if (node == null || !node.getEnvelope().contains(itemEnv)) {
				subnode[index] = createExpanded(node, itemEnv);
			}
			/**
			 * At this point we have a subquad which exists and must contain the env for the item. Insert the item
			 * into the tree.
			 */
			insertContained(subnode[index], itemEnv, item);
		}
	
		/**
		 * insert an item which is known to be contained in the tree rooted at the given QuadNode root. Lower levels of the
		 * tree will be created if necessary to hold the item.
		 */
		private void insertContained(JtsQuadNode<E> tree, Envelope itemEnv, E item) {
			assert tree.getEnvelope().contains(itemEnv);
			/**
			 * Do NOT create a new quad for zero-area envelopes - this would lead to infinite recursion. Instead, use a
			 * heuristic of simply returning the smallest existing quad containing the query
			 */
			boolean isZeroX = isZeroWidth(itemEnv.getMinX(), itemEnv.getMaxX());
			boolean isZeroY = isZeroWidth(itemEnv.getMinY(), itemEnv.getMaxY());
			JtsNodeBase<E> node;
			if (isZeroX || isZeroY) {
				node = tree.find(itemEnv);
			} else {
				node = tree.getNode(itemEnv);
			}
			node.add(item);
		}
	
		private static boolean isZeroWidth(double min, double max) {
			double width = max - min;
			if (width == 0.0) {
				return true;
			}
			//32 bits of precision is quite enough
			return Math.max(Math.abs(min/width), Math.abs(max/width)) > Integer.MAX_VALUE;
		}
	
		@Override
		protected boolean isSearchMatch(Envelope searchEnv) {
			return true;
		}
	}

	/**
	 * A Key is a unique identifier for a node in a quadtree. It contains a lower-left point and a level number. The
	 * level number is the power of two for the size of the node envelope
	 * 
	 * @version 1.7
	 */
	static class Key {
		public static double initialQuadLevel(Envelope env) {
			double dx = env.getWidth();
			double dy = env.getHeight();
			return 2*MathUtil.floorPow2(dx > dy ? dx : dy);
		}
	
		private EnvelopeBuilder env = null;
		
		public Key(Envelope itemEnv) {
			env = new EnvelopeBuilder(itemEnv.getCrsId());
			computeKey(itemEnv);
		}
	
		public Envelope getEnvelope() {
			return env.getEnvelope();
		}
	
		/**
		 * return a square envelope containing the argument envelope, whose extent is a power of two and which is based
		 * at a power of 2
		 */
		public void computeKey(Envelope itemEnv) {
			double quadSize = initialQuadLevel(itemEnv);
			env.clear();
			double itmX = itemEnv.getMinX();
			double itmY = itemEnv.getMinY();
			computeKeyForLevel(itmX, itmY, quadSize);
			//TODO: would be nice to have a non-iterative form of this algorithm
			while (!env.contains(itemEnv)) {
				quadSize *= 2;
				computeKeyForLevel(itmX, itmY, quadSize);
			}
		}
	
		private void computeKeyForLevel(double itemMinX, double itemMinY, double quadSize) {
			double x = MathUtil.floorToMultiple(itemMinX, quadSize);
			double y = MathUtil.floorToMultiple(itemMinY, quadSize);
			env.setMBR(x, y, x + quadSize, y + quadSize);
		}
	}

	private static final long serialVersionUID = -7461163625812743604L;

	/**
	 * Ensure that the envelope for the inserted item has non-zero extents. Use the current minExtent to pad the
	 * envelope, if necessary
	 */
	public Envelope ensureExtent(Envelope itemEnv) {
		//The names "ensureExtent" and "minExtent" are misleading -- sounds like
		//this method ensures that the extents are greater than minExtent.
		//Perhaps we should rename them to "ensurePositiveExtent" and "defaultExtent".
		//[Jon Aquino]
		double minx = itemEnv.getMinX();
		double maxx = itemEnv.getMaxX();
		double miny = itemEnv.getMinY();
		double maxy = itemEnv.getMaxY();
		// has a non-zero extent
		if (minx != maxx && miny != maxy) {
			return itemEnv;
		}

		// pad one or both extents
		if (minx == maxx) {
			minx = minx - minExtent / 2.0;
			maxx = minx + minExtent / 2.0;
		}
		if (miny == maxy) {
			miny = miny - minExtent / 2.0;
			maxy = miny + minExtent / 2.0;
		}
		
		EnvelopeBuilder builder = new EnvelopeBuilder(itemEnv.getCrsId());
		builder.setMBR(minx, miny, maxx, maxy);
		return builder.getEnvelope();
	}

	private Root<E> root = new Root<E>();
	/**
	 * minExtent is the minimum envelope extent of all items inserted into the tree so far. It is used as a heuristic
	 * value to construct non-zero envelopes for features with zero X and/or Y extent. Start with a non-zero extent, in
	 * case the first feature inserted has a zero extent in both directions. This value may be non-optimal, but only one
	 * feature will be inserted with this value.
	 **/
	private double minExtent = 1.0;

	/**
	 * Constructs a Quadtree with zero items.
	 */
	public JtsQuadtree() {}

	public void insert(Envelope itemEnv, E item) {
		collectStats(itemEnv);
		root.insert(ensureExtent(itemEnv), item);
	}

	/**
	 * @return <code>true</code> if the item was found (and thus removed)
	 */
	public boolean remove(Envelope itemEnv, Object item) {
		Envelope posEnv = ensureExtent(itemEnv);
		return root.remove(posEnv, item);
	}

	/**
	 * @return <code>true</code> if the item was found (and thus removed)
	 */
	public boolean remove(Object item) {
		return root.remove(item);
	}

	
	public boolean contains(Envelope itemEnv, Object item) {
		Envelope posEnv = ensureExtent(itemEnv);
		return root.contains(posEnv, item);
	}

	public List<E> query(Envelope searchEnv) {
		ListCollector<E> visitor = new ListCollector<E>();
		query(searchEnv, visitor);
		return visitor;
	}

	/**
	 * @param visitor a visitor object which is passed the visited items
	 */
	public void query(Envelope searchEnv, SearchItemReceiver<? super E> visitor) {
		root.visit(searchEnv, visitor);
	}

	private void collectStats(Envelope itemEnv) {
		double delX = itemEnv.getWidth();
		if (0 < delX && delX < minExtent)
			minExtent = delX;

		double delY = itemEnv.getHeight();
		if (0 < delY && delY < minExtent)
			minExtent = delY;
	}

	static <E> JtsQuadNode<E> createExpanded(JtsQuadNode<E> node, Envelope addEnv) {
		Envelope expandEnv = new Envelope(addEnv);
		if (node != null)
			expandEnv = expandEnv.union(node.env);
	
		JtsQuadNode<E> largerNode = createNode(expandEnv);
		if (node != null)
			largerNode.insertNode(node);
		return largerNode;
	}

	static <E> JtsQuadNode<E> createNode(Envelope env) {
		Key key = new Key(env);
		JtsQuadNode<E> node = new JtsQuadNode<E>(key.getEnvelope());
		return node;
	}

	/**
	 * Gets the index of the subquad that wholly contains the given envelope. If none does, returns -1.
	 * 
	 * @return the index of the subquad that wholly contains the given envelope or -1 if no subquad wholly contains the
	 *         envelope
	 */
	public static int getSubnodeIndex(Envelope env, double centrex, double centrey) {
		int subnodeIndex = -1;
		if (env.getMinX() >= centrex) {
			if (env.getMinY() >= centrey)
				subnodeIndex = 3;
			if (env.getMaxY() <= centrey)
				subnodeIndex = 1;
		}
		if (env.getMaxX() <= centrex) {
			if (env.getMinY() >= centrey)
				subnodeIndex = 2;
			if (env.getMaxY() <= centrey)
				subnodeIndex = 0;
		}
		return subnodeIndex;
	}
}
