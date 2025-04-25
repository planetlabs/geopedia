package com.sinergise.gwt.ui.maingui.extwidgets;

import java.util.EnumSet;
import java.util.HashMap;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasMouseOutHandlers;
import com.google.gwt.event.dom.client.HasMouseOverHandlers;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.DockLayoutPanel.Direction;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ProvidesResize;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.gwt.ui.ImageAnchor;
import com.sinergise.gwt.ui.maingui.gwtmod.SplitLayoutPanel;
import com.sinergise.gwt.ui.resources.dock.DockLayoutResources;

public class SGHidableDockLayoutPanel extends SplitLayoutPanel {

	private class PanelHolder extends FlowPanel implements HasMouseOverHandlers, HasMouseOutHandlers, RequiresResize, ProvidesResize {
	
		@Override
		public void onResize() {
			if (content != null && content instanceof RequiresResize) {
				((RequiresResize)content).onResize();
			}
		}
	
		private Splitter splitter = null;
		private Direction direction;
	
		FlowPanel minimizePanel = new FlowPanel();
		ImageAnchor btnMinimize;
		boolean minimized = false;
		boolean hovering = false;
		double lastOpenedSize = 0;
	
		private ImageResource getMinimizerImage(Direction direction, boolean minim) {
			switch (getResolvedDirection(direction)) {
				case SOUTH:
					return minim ? resources.minimizeUp() : resources.minimizeDown();
				case NORTH:
					return minim ? resources.minimizeDown() : resources.minimizeUp();
				case EAST:
					return minim ? resources.minimizeLeft() : resources.minimizeRight();
				case WEST:
					return minim ? resources.minimizeRight() : resources.minimizeLeft();
				default:
					return null;
			}
		}
	
		public Double getOpenedSize() {
			if (content == null) {
				if (lastOpenedSize > 0)
					return Double.valueOf(lastOpenedSize);
				return null;
			}
			if (minimized) {
				if (lastOpenedSize > 0)
					return Double.valueOf(lastOpenedSize);
				return null;
			}
			LayoutData holderLayout = (LayoutData)getLayoutData();
			if (holderLayout.size > 0)
				return Double.valueOf(holderLayout.size);
			return null;
		}
	
		public PanelHolder(Direction direction1) {
			this.direction = direction1;
			setStyleName(resources.dockLayout().contentHolder() + " " + direction.name());
			if (direction == Direction.CENTER)
				return;
	
			minimizePanel.setStyleName(resources.dockLayout().minimizer());
			btnMinimize = new ImageAnchor(getMinimizerImage(direction, minimized));
			minimizePanel.add(btnMinimize);
			btnMinimize.addClickHandler(new ClickHandler() {
	
				@Override
				public void onClick(ClickEvent event) {
					LayoutData holderLayout = (LayoutData)getLayoutData();
					LayoutData splitterLayout = (LayoutData)splitter.getLayoutData();
					if (!minimized) {
						if (content == null)
							return; // can't minimize!!
						splitter.setVisible(false);
						splitterLayout.size = 0;
						lastOpenedSize = getWidgetSize(PanelHolder.this);
						holderLayout.size = minimizerSize;
						hovering = false;
						minimized = true;
						btnMinimize.setImageRes(getMinimizerImage(direction, minimized));
						forceLayout();
					} else {
						if (content == null)
							return; // can't maximize!						
						holderLayout.size = lastOpenedSize;
						holderLayout.popout = false;
						splitter.setVisible(true);
						splitterLayout.size = splitterSize;
						minimized = false;
						hovering = false;
						btnMinimize.setImageRes(getMinimizerImage(direction, minimized));
						forceLayout();
					}
	
				}
			});
	
			addMouseOverHandler(new MouseOverHandler() {
	
				@Override
				public void onMouseOver(MouseOverEvent event) {
					if (minimized && !hovering) {
						hovering = true;
						slidePanel(PanelHolder.this, lastOpenedSize, false);
					}
	
				}
			});
			addMouseOutHandler(new MouseOutHandler() {
	
				@Override
				public void onMouseOut(MouseOutEvent event) {
					if (minimized && hovering) {
						hovering = false;
						slidePanel(PanelHolder.this, minimizerSize, true);
					}
				}
			});
		}
	
		public void setSplitter(Splitter splitter1) {
			if (direction == Direction.CENTER)
				return;
			assert (this.splitter == null) : "Splitter may not be modified!";
			this.splitter = splitter1;
			removeContent();
		}
	
		Widget content = null;
	
		public void setContent(Widget w, double size) {
			if (w == null)
				return;
			if (content != null) {
				content.removeFromParent();
				clear();
			}
			content = w;
			content.addStyleName(resources.dockLayout().content());
			add(content);
	
			if (direction != Direction.CENTER) {
				add(minimizePanel);
	
				LayoutData holderLayout = (LayoutData)getLayoutData();
				LayoutData splitterLayout = (LayoutData)splitter.getLayoutData();
				splitterLayout.size = splitterSize;
				holderLayout.size = size;
				holderLayout.popout = false;
				minimized = false;
				hovering = false;
				splitter.setVisible(true);
				forceLayout();
				btnMinimize.setImageRes(getMinimizerImage(direction, minimized));
	
	
				switch (direction) {
					case SOUTH:
						content.getElement().getStyle().setPropertyPx("bottom", minimizerSize);
						minimizePanel.getElement().getStyle().setPropertyPx("height", minimizerSize);
						break;
					case NORTH:
						content.getElement().getStyle().setPropertyPx("top", minimizerSize);
						minimizePanel.getElement().getStyle().setPropertyPx("height", minimizerSize);
						break;
					case EAST:
						content.getElement().getStyle().setPropertyPx("right", minimizerSize);
						minimizePanel.getElement().getStyle().setPropertyPx("width", minimizerSize);
						break;
					case WEST:
						content.getElement().getStyle().setPropertyPx("left", minimizerSize);
						minimizePanel.getElement().getStyle().setPropertyPx("width", minimizerSize);
						break;
				}
	
			}
	
		}
	
		public void removeContent() {
			if (content != null) {
				content.removeFromParent();
				clear();
				content = null;
			}
			if (splitter != null) {
				LayoutData holderLayout = (LayoutData)getLayoutData();
				LayoutData splitterLayout = (LayoutData)splitter.getLayoutData();
				splitterLayout.size = 0;
				holderLayout.size = 0;
				holderLayout.popout = false;
				minimized = false;
				hovering = false;
				splitter.setVisible(false);
				forceLayout();
			}
	
		}
	
		@Override
		public HandlerRegistration addMouseOverHandler(MouseOverHandler handler) {
			return addDomHandler(handler, MouseOverEvent.getType());
		}
	
		@Override
		public HandlerRegistration addMouseOutHandler(MouseOutHandler handler) {
			return addDomHandler(handler, MouseOutEvent.getType());
		}
	
		public boolean hasContent(Widget widget) {
			return widget == content;
		}
	
	}

	private final DockLayoutResources resources;

	private int minimizerSize = 15;

	HashMap<Direction, PanelHolder> panelHoldersMap = new HashMap<Direction, PanelHolder>();

	public SGHidableDockLayoutPanel(EnumSet<Direction> enabledPanels) {
		this(enabledPanels, DockLayoutResources.INSTANCE);
	}

	public SGHidableDockLayoutPanel(EnumSet<Direction> enabledPanels, DockLayoutResources resources) {
		this.resources = resources;
		resources.dockLayout().ensureInjected();

		if (enabledPanels.contains(Direction.NORTH)) {
			processDirection(Direction.NORTH);
		}
		if (enabledPanels.contains(Direction.SOUTH)) {
			processDirection(Direction.SOUTH);
		}
		if (enabledPanels.contains(Direction.EAST)) {
			processDirection(Direction.EAST);
		}
		if (enabledPanels.contains(Direction.WEST)) {
			processDirection(Direction.WEST);
		}
		if (enabledPanels.contains(Direction.CENTER)) {
			processDirection(Direction.CENTER);
		}
	}

	private void slidePanel(PanelHolder panel, double size, boolean in) {
		LayoutData data = (LayoutData)panel.getLayoutData();
		if (!in) {
			data.oldSize = data.size;
			data.popout = true;
			data.size = size;
			panel.content.getElement().getStyle().setProperty("zIndex", "100");
			forceLayout();
		} else {
			data.size = size;
			data.oldSize = 0;
			data.popout = false;
			panel.content.getElement().getStyle().clearProperty("zIndex");
			forceLayout();
		}
	
	}

	public double getWidgetSize(Widget widget) {
		LayoutData data = (LayoutData)widget.getLayoutData();
		assert data.direction != Direction.CENTER : "The size of the center widget can not be updated.";
		return data.size;
	}

	private void processDirection(Direction direction) {
		PanelHolder holder = new PanelHolder(direction);
		panelHoldersMap.put(direction, holder);
		insert(holder, direction, 0, null);
	}

	@Override
	public Splitter insertAndGetSplitter(Widget child, Direction direction, double size, Widget before) {
		assert (child instanceof PanelHolder) : "Only PanelHolder children may be inserted!";
		PanelHolder pnl = (PanelHolder)child;
		Splitter splitter = super.insertAndGetSplitter(child, direction, size, before);
		pnl.setSplitter(splitter);
		if (splitter != null) {
			UIObject.setStyleName(DOM.getParent(splitter.getElement()), resources.dockLayout().splitter() + " "
				+ direction.name(), true);
		}
		UIObject.setStyleName(DOM.getParent(child.getElement()),
			resources.dockLayout().splitPanel() + " " + direction.name(), true);
		return splitter;
	}

	public void setPanel(Widget widget, Direction direction) {
		setPanel(widget, direction, 0);
	}

	public void setPanel(Widget widget, Direction direction, int size) {
		setPanel(widget, direction, (double)size);
	}

	public void setPanel(Widget widget, Direction direction, double size) {
		PanelHolder holder = panelHoldersMap.get(direction);
		if (holder == null)
			return;
		Double lastOpenedSize = holder.getOpenedSize();
		if (lastOpenedSize == null || lastOpenedSize.doubleValue() <= 0) {
			if (size < 0)
				size = 0d;
		} else {
			size = lastOpenedSize.doubleValue();
		}
		holder.setContent(widget, size);
	}

	public void removePanel(Direction direction) {
		assert !(direction == Direction.CENTER) : "Center widget can not be removed!";
		PanelHolder holder = panelHoldersMap.get(direction);
		if (holder == null)
			return;
		holder.removeContent();
	}

	public boolean hasPanel(Widget widget, Direction direction) {
		if (widget == null)
			return false;
		PanelHolder holder = panelHoldersMap.get(direction);
		if (holder == null)
			return false;
		return holder.hasContent(widget);

	}

}
