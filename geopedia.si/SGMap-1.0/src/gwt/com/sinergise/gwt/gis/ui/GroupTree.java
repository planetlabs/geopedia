package com.sinergise.gwt.gis.ui;

import java.util.Iterator;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.util.collections.tree.AbstractTree;
import com.sinergise.common.util.collections.tree.ITreeNode;
import com.sinergise.common.util.collections.tree.TreeListenerAdapter;
import com.sinergise.common.util.collections.tree.TreeNodeFilter;
import com.sinergise.gwt.gis.map.util.StyleConsts;
import com.sinergise.gwt.ui.core.WidgetVisitor;


@SuppressWarnings("rawtypes")
public class GroupTree<T extends ITreeNode<T>> extends Composite {
	public abstract static class NodeWidgetFinder<S extends ITreeNode> implements WidgetVisitor {
		public NodeRepWidget<S> result;
		
		public NodeWidgetFinder() {}
		
		@Override
		@SuppressWarnings("unchecked")
		public boolean visit(Widget wgt) {
			if (wgt instanceof NodeRepWidget && accept((NodeRepWidget<S>) wgt)) {
				result = (NodeRepWidget<S>)wgt;
				return false; 
			}
			return true;
		}
		
		public abstract boolean accept(NodeRepWidget<S> wgt);
	}
	
	public abstract static class NodeFinder<S extends ITreeNode> implements WidgetVisitor, TreeNodeFilter<S> {
		public NodeRepWidget<S> result;
		
		public NodeFinder() {}
		
		@Override
		@SuppressWarnings("unchecked")
		public boolean visit(Widget wgt) {
			System.out.println("NodeFinder: "+wgt);
			if (wgt instanceof NodeRepWidget && accept((S)((NodeRepWidget)wgt).getNode())) {
				System.out.println("Accepted: "+wgt);
				result = (NodeRepWidget)wgt;
				return false;
			}
			return true;
		}
	}
	
	public static class RootChildrenWidget<S extends ITreeNode> extends FlowPanel implements NodeWidget<S> {
		final S root;
		
		public RootChildrenWidget(S root) {
			this.root = root;
		}
		
		@Override
		public S getNode() {
			return root;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public void add(Widget w) {
			super.add(w);
			if (w instanceof NodeRepWidget) {
				((NodeRepWidget<S>) w).setNodeWidgetParent(this);
			}
		}
		@Override
		public NodeWidget<S> getNodeWidgetParent() {
			return null;
		}

		@Override
		public void setNodeWidgetParent(NodeWidget<S> arg0) {
			throw new UnsupportedOperationException("Don't set parent on root");
		}
		
		@Override
		public void ensureVisible() {
			return;
		}
		
		@Override
		public boolean isExpanded(boolean arg0) {
			return true;
		}
		
		@Override
		public void setExpanded(boolean expanded, boolean applyToChildren) {
			if (!applyToChildren) return;
			for (Iterator<?> it = iterator(); it.hasNext();) {
				Widget wgt = (Widget) it.next();
				if (wgt instanceof NodeWidget) {
					((NodeWidget) wgt).setExpanded(expanded, applyToChildren);
				}
			}
		}
		
		@Override
		public void widgetWrapped(NodeWrapper<S> wrapper) {
		}
	}
	
	public static interface NodeRepWidget<S extends ITreeNode> {
		S getNode();
		
		void setNodeWidgetParent(NodeWidget<S> nodeRepParent);
		
		NodeWidget<S> getNodeWidgetParent();
		
		void ensureVisible();
		
		void widgetWrapped(NodeWrapper<S> wrapper);
	}
	
	public static interface NodeWidget<S extends ITreeNode> extends NodeRepWidget<S> {
		boolean isExpanded(boolean deep);
		
		void setExpanded(boolean expanded, boolean applyToChildren);
	}
	
	protected boolean showRoot = false;
	
	protected RootChildrenWidget<T> wrappedWidget;
	
	private final class Rebuilder extends Timer {
		
		private boolean scheduled = false;
		
		public void schedule() {
			if (scheduled) cancel();
			scheduled = true;
			schedule(100);
		}
		
		@Override
		public void run() {
			rebuildTree();
			scheduled = false;
		}
	}
	
	protected final AbstractTree<T> treeModel;
	protected final Rebuilder rebuilder = new Rebuilder();
	
	public GroupTree(final AbstractTree<T> tree) {
		this(tree, true);
	}
	
	protected GroupTree(final AbstractTree<T> tree, boolean constructGui) {
		this.treeModel = tree;
		wrappedWidget = new RootChildrenWidget<T>(tree.getRoot());
		if (constructGui) constructGUI(tree, wrappedWidget);
		initWidget(wrappedWidget);
		tree.addTreeListener(new TreeListenerAdapter<T>() {
			@Override
			public void nodeAdded(T parent, T added, int newIndex) {
				rebuilder.schedule();
			}
			@Override
			public void nodeRemoved(T parent, T removed, int oldIndex) {
				rebuilder.schedule();
			}
		});
	}
	
	protected void rebuildTree() {
		wrappedWidget.clear();
		constructGUI(treeModel, wrappedWidget);
	}
	
	public boolean ensureVisible(T node) {
		NodeRepWidget nrw = findWidgetFor(node);
		if (nrw == null) return false;
		nrw.ensureVisible();
		return true;
	}
	
	public NodeRepWidget findWidgetFor(final T node) {
		NodeWidgetFinder nf = new NodeWidgetFinder() {
			@Override
			public boolean accept(NodeRepWidget wgt) {
				return node.equals(wgt.getNode());
			}
		};
		traverseWidgets(nf);
		return nf.result;
	}
	
	public NodeRepWidget<T> findWidgetFor(final TreeNodeFilter<? super T> filter) {
		NodeFinder<T> nf = new NodeFinder<T>() {
			@Override
			public boolean accept(T node) {
				return filter.accept(node);
			}
		};
		traverseWidgets(nf);
		return nf.result;
	}
	
	protected void constructGUI(AbstractTree<T> tree, HasWidgets parent) {
		if (showRoot) {
			parent.add((Widget)constructGUI(tree, tree.getRoot(), null));
			return;
		}
		T root = tree.getRoot();
		for (int i = 0; i < root.getChildCount(); i++) {
			constructGUI(tree, root.getChild(i), parent);
		}
	}
	
	private NodeRepWidget<T> constructGUI(AbstractTree<T> tree, T node, HasWidgets parent) {
		NodeRepWidget<T> ret;
		if (getVisibleChildCount(node) == 0) {
			ret = constructLeafWidget(tree, node);
		}  else {
			ret = constructGroupWidget(tree, node);
		}
		if (parent != null) parent.add((Widget)ret);
		return ret;
	}

	protected NodeWrapper<T> constructLeafWidget(AbstractTree<T> tree, T node) {
		NodeWrapper<T> nw = new NodeWrapper<T>(node);
		Widget wgt = createHeaderWidgetFor(tree, nw);
		nw.wrapNodeHeader(wgt);
		return nw;
	}

	protected GroupTreeNodeDisclosurePanel<T> constructGroupWidget(AbstractTree<T> tree, T node) {
		GroupTreeNodeDisclosurePanel<T> groupTreeNodeDisplay = new GroupTreeNodeDisclosurePanel<T>(node, getOpenedIcon(), getClosedIcon());
		Widget wgt = createHeaderWidgetFor(tree, groupTreeNodeDisplay);
		groupTreeNodeDisplay.wrapNodeHeader(wgt);
		groupTreeNodeDisplay.getDisclosurePanelHeader().setStylePrimaryName(getGroupHeaderStyle(tree, node));
		groupTreeNodeDisplay.setStylePrimaryName(getGroupStyle(tree, node));
		groupTreeNodeDisplay.setWidth("100%");
		for (int i = 0; i < node.getChildCount(); i++) {
			constructGUI(tree, node.getChild(i), groupTreeNodeDisplay);
		}
		return groupTreeNodeDisplay;
	}
	
	protected int getVisibleChildCount(T node) {
		return node.getChildCount();
	}
	
	/**
	 * Override this to provide a custom icon in the opened state
	 * @return image prototype
	 */
	protected ImageResource getOpenedIcon() {
		return null;
	}
	
	/**
	 * Override this to provide a custom icon in the closed state
	 * @return image prototype
	 */
	protected ImageResource getClosedIcon() {
		return null;
	}
	
	/**
	 * Override this to provide custom widgets for node label
	 * 
	 * @param tree  
	 */
	protected Widget createHeaderWidgetFor(AbstractTree<T> tree, NodeWrapper<T> wrapper) {
		return new Label(wrapper.getNode().toString());
	}
	
	/**
	 * Override this to provide custom styling for the node's container widget
	 * 
	 * @param tree The tree that the node is rendered in
	 * @param node The node that should be styled
	 */
	protected String getGroupStyle(AbstractTree<T> tree, T node) {
		return StyleConsts.GROUPTREE_DISCLOSURE_PANEL;
	}
	
	/**
	 * Override this to provide custom styling for the node's header label widget
	 * 	 * 
	 * @param tree The tree that the node is rendered in
	 * @param node The node that should be styled
	 */
	protected String getGroupHeaderStyle(AbstractTree<T> tree, T node) {
		return "header";
	}
	
	public NodeRepWidget getRootWidget() {
		return (NodeRepWidget) getWidget();
	}
	
	public boolean traverseWidgets(WidgetVisitor visitor) {
		NodeRepWidget rootW = getRootWidget();
		if (rootW instanceof GroupTreeNodeDisclosurePanel) {
			return traverseNode(visitor, (Widget) rootW);
		} else if (rootW instanceof HasWidgets) {
			HasWidgets hw = (HasWidgets) rootW;
			for (Iterator<Widget> it = hw.iterator(); it.hasNext();) {
				Widget wgt = it.next();
				if (!traverseNode(visitor, wgt)) return false;
			}
			return true;
		}
		throw new IllegalStateException("The tree should either be a node or a widget collection.");
	}
	
	@SuppressWarnings("unchecked")
	public boolean traverseNode(WidgetVisitor visitor, Widget node) {
		if (node instanceof GroupTreeNodeDisclosurePanel) {
			GroupTreeNodeDisclosurePanel gtndp = (GroupTreeNodeDisclosurePanel) node;
			if (!visitor.visit(gtndp.getHeader())) return false;
			for (Iterator<Widget> it = gtndp.iterator(); it.hasNext();) {
				Widget child = it.next();
				if (child instanceof NodeRepWidget && !traverseNode(visitor, child)) return false;
			}
			return true;
		} else if (node instanceof NodeWrapper) {
			return visitor.visit(((NodeWrapper) node).getWrappedWidget());
		} else {
			return visitor.visit(node);
		}
	}
}
