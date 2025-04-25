package com.sinergise.gwt.gis.ui;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.util.collections.tree.ITreeNode;
import com.sinergise.gwt.gis.ui.GroupTree.NodeRepWidget;
import com.sinergise.gwt.gis.ui.GroupTree.NodeWidget;


@SuppressWarnings("rawtypes")
public class NodeWrapper<T extends ITreeNode> extends Composite implements NodeRepWidget<T> {
	T node;
	public NodeWrapper(T node) {
		this.node=node;
	}
	
	@Override
	public void ensureVisible() {
		setVisible(true);
		nodeWidgetParent.setExpanded(true, false);
		nodeWidgetParent.ensureVisible();
	}

	@Override
	public T getNode() {
		return node;
	}

	protected NodeWidget<T> nodeWidgetParent;
	@Override
	public NodeWidget<T> getNodeWidgetParent() {
		return nodeWidgetParent;
	}

	@Override
	public void setNodeWidgetParent(NodeWidget<T> nodeRepParent) {
		this.nodeWidgetParent=nodeRepParent;
	}

	@SuppressWarnings("unchecked")
	protected void wrapNodeHeader(Widget wgt) {
		initWidget(wgt);
		if (wgt instanceof NodeRepWidget) {
			((NodeRepWidget)wgt).widgetWrapped(this);
		}
	}

	/**
	 * @return
	 */
	public Widget getWrappedWidget() {
		return getWidget();
	}
	
	@Override
	public void widgetWrapped(NodeWrapper<T> wrapper) {
		throw new IllegalStateException("Cannot wrap a wrapper");
	}
}
