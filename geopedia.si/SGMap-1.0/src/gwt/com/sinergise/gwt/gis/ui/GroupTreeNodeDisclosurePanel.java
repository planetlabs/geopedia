package com.sinergise.gwt.gis.ui;

import java.util.Iterator;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.util.collections.tree.ITreeNode;
import com.sinergise.gwt.gis.map.util.StyleConsts;
import com.sinergise.gwt.gis.ui.GroupTree.NodeRepWidget;
import com.sinergise.gwt.gis.ui.GroupTree.NodeWidget;
import com.sinergise.gwt.ui.DefaultDisclosureHeader;



/**
 * Custom disclosure panel that can accept children. Children are hidden/shown as
 * the panel is open or closed.
 */
@SuppressWarnings("rawtypes")
public class GroupTreeNodeDisclosurePanel<T extends ITreeNode> extends NodeWrapper<T> implements HasWidgets, NodeWidget<T> {
	private FlowPanel 		    	children=new FlowPanel();
	private DisclosurePanel 	    discP;
	private DefaultDisclosureHeader discHead;
	private Widget                  header;
	private ImageResource           ovOpened;
	private ImageResource           ovClosed;
	
	
	public GroupTreeNodeDisclosurePanel(T node) {
		super(node);
	}
	
	public GroupTreeNodeDisclosurePanel(T node, ImageResource ovOpened, ImageResource ovClosed) {
		super(node);
		this.ovOpened = ovOpened;
		this.ovClosed = ovClosed;
	}
	
	public GroupTreeNodeDisclosurePanel(T node, Widget header) {
		this(node);
		wrapNodeHeader(header);
	}
	
	public GroupTreeNodeDisclosurePanel(T node, Widget header, ImageResource ovOpened, ImageResource ovClosed) {
		this(node);
		this.ovOpened = ovOpened;
		this.ovClosed = ovClosed;
		wrapNodeHeader(header);
	}
	
	@Override
	protected void wrapNodeHeader(Widget wgt) {
		header=wgt;
		discHead=new DefaultDisclosureHeader(wgt);
		discHead.setEnabled(node.getChildCount()>0);
		discP = new DisclosurePanel();
		//try putting "div.gwt-TabPanelBottom > div > div { overflow: visible !important; }" to solve appearing of scrollbar
		discP.setAnimationEnabled(true);
		discP.setHeader(discHead);
		discP.setOpen(false);
		initWidget(discP);
		children.setStyleName(StyleConsts.GROUPTREE_CHILDREN);
		discP.setContent(children);
		
		if (ovOpened != null) {
			discHead.setOpenedImage(ovOpened);
		}
		if (ovClosed != null) {
			discHead.setClosedImage(ovClosed);
		}
	}
	
	public Widget getHeader() {
		return header;
	}
	
	public DefaultDisclosureHeader getDisclosurePanelHeader() {
		return (DefaultDisclosureHeader)discP.getHeader();
	}

	@Override
	public void setExpanded(boolean expanded, boolean applyToChildren) {
		discP.setOpen(expanded);
		if (applyToChildren) {
			for (Iterator<Widget> it = children.iterator(); it.hasNext();) {
				Widget wgt = it.next();
				if (wgt instanceof NodeWidget) {
					((NodeWidget)wgt).setExpanded(expanded, true);
				}
			}
		}
	}
	@Override
	public boolean isExpanded(boolean deep) {
		boolean ret=discP.isOpen();
		if (deep) {
			ret = ret && nodeWidgetParent.isExpanded(true);
		}
		return ret;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void add(Widget child) {
		children.add(child);
		if (child instanceof NodeRepWidget) {
			((NodeRepWidget<T>)child).setNodeWidgetParent(this);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void insert(Widget child, int beforeIndex) {
		children.insert(child, beforeIndex);
		if (child instanceof NodeRepWidget) {
			((NodeRepWidget<T>)child).setNodeWidgetParent(this);
		}
	}

	@Override
	public void clear() {
		children.clear();
	}
	@Override
	public Iterator<Widget> iterator() {
		return children.iterator();
	}
	@Override
	public boolean remove(Widget w) {
		return children.remove(w);
	}
}
