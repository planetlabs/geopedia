package com.sinergise.gwt.ui.maingui.extwidgets;

import static com.google.gwt.user.client.ui.DockLayoutPanel.Direction.CENTER;
import static com.sinergise.gwt.ui.StyleConsts.DECORATED_SPLIT_PANEL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.google.gwt.dom.client.Element;
import com.google.gwt.layout.client.Layout.AnimationCallback;
import com.google.gwt.layout.client.Layout.Layer;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.DockLayoutPanel.Direction;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;
import com.sinergise.common.ui.controls.CanEnsureChildVisibility;
import com.sinergise.common.ui.controls.SourcesVisibilityChangeEvents;
import com.sinergise.common.ui.controls.VisibilityChangeListener;
import com.sinergise.common.util.state.gwt.PropertyChangeListener;
import com.sinergise.common.util.state.gwt.PropertyChangeListenerCollection;
import com.sinergise.common.util.state.gwt.SourcesPropertyChangeEvents;
import com.sinergise.common.util.string.StringUtil;
import com.sinergise.gwt.ui.controls.EnsureVisibilityUtil;
import com.sinergise.gwt.ui.maingui.gwtmod.SplitLayoutPanel;
import com.sinergise.gwt.util.UtilGWT;
import com.sinergise.gwt.util.history.HistoryHandler;
import com.sinergise.gwt.util.history.HistoryManager;

/**
 * Extends {@link SplitLayoutPanel} to add additional styles to all components for decoration purposes.
 * 
 * @author tcerovski
 */
public class SGSplitLayoutPanel extends SplitLayoutPanel implements CanEnsureChildVisibility, SourcesVisibilityChangeEvents {
	private static Widget tweakLayoutData(Widget target) {
		LayoutData ld = (LayoutData)target.getLayoutData();
		if (ld != null) {
			ld.originalSize = ld.size;
		}
		return target;
	}
	
	protected class SGHSplitter extends SGSplitter {
	    public SGHSplitter(Widget target, boolean reverse) {
	      super(target, reverse);
	      setStyleName("gwt-SplitLayoutPanel-HDragger");
	      addStyleName("HDragger");
	    }

	    @Override
	    protected int getAbsolutePosition() {
	      return getAbsoluteLeft();
	    }

	    @Override
	    protected int getEventPosition(Event event) {
	      return event.getClientX();
	    }

	    @Override
	    protected int getTargetPosition() {
	      return target.getAbsoluteLeft();
	    }

	    @Override
	    protected int getTargetSize() {
	      return target.getOffsetWidth();
	    }
	}
	
	protected class SGVSplitter extends SGSplitter {
	    public SGVSplitter(Widget target, boolean reverse) {
	      super(target, reverse);
	      setStyleName("gwt-SplitLayoutPanel-VDragger");
	      addStyleName("VDragger");
	    }

	    @Override
	    protected int getAbsolutePosition() {
	      return getAbsoluteTop();
	    }

	    @Override
	    protected int getEventPosition(Event event) {
	      return event.getClientY();
	    }

	    @Override
	    protected int getTargetPosition() {
	      return target.getAbsoluteTop();
	    }

	    @Override
	    protected int getTargetSize() {
	      return target.getOffsetHeight();
	    }
	}
	
	/**
	 * Extends default {@link Splitter} to implement visibility toggling on double click.  
	 */
	public abstract class SGSplitter extends Splitter implements SourcesPropertyChangeEvents<Object> {
		
		public static final String PROP_WIDGET_VISIBLE = "widgetVisible";
		
		private PropertyChangeListenerCollection<Object> listeners = new PropertyChangeListenerCollection<Object>();
		
		private long tDown;
		private int posDown;
		
		public SGSplitter(Widget target, boolean reverse) {
			super(tweakLayoutData(target), reverse);
			sinkEvents(clickEventsMask);
		}

		@Override
		public void onBrowserEvent(Event event) {
			if (splitterFixed) return;
			if ((event.getTypeInt() & clickEventsMask) != 0) {
				int dPos = getAssociatedWidgetSize() - posDown;
				if (dPos == 0 && System.currentTimeMillis() - tDown < 500) {
					toggleWidgetVisibility();
					Event.releaseCapture(getElement());
					event.preventDefault();
				}
			}
		    if (event.getTypeInt() == Event.ONMOUSEDOWN) {
		    	tDown = System.currentTimeMillis();
		    	posDown = getAssociatedWidgetSize();
		    }
			super.onBrowserEvent(event);
		}
		
		public void toggleWidgetVisibility() {
			setWidgetVisible(!isWidgetVisible());
		}
		
		public boolean isWidgetVisible() {
			return getAssociatedWidgetSize() > 0;
		}
		
		@Override
		public int getAssociatedWidgetSize() {
			return super.getAssociatedWidgetSize();
		}
		
		public void setWidgetVisible(boolean newVisible) {
			LayoutData tgtData = (LayoutData)target.getLayoutData();
			boolean oldVisible = tgtData.size > 0;
			if (newVisible == oldVisible) {
				return;
			}
			
			final int newSize;
			if (newVisible) {
				newSize = (int)Math.round(tgtData.originalSize);
			} else {
				newSize = 0;
				tgtData.originalSize = tgtData.size;
			}
			
			setAssociatedWidgetSize(newSize);
			listeners.fireChange(this, PROP_WIDGET_VISIBLE, Boolean.valueOf(oldVisible), Boolean.valueOf(newVisible));
		}
		
		@Override
		public void setAssociatedWidgetSize(int size) {
			super.setAssociatedWidgetSize(size);
			SGSplitLayoutPanel.this.onResize();
		}
		
		public void addPropertyChangeListener(PropertyChangeListener<Object> listener) {
			listeners.add(listener);
		}
		
		public void removePropertyChangeListener(PropertyChangeListener<Object> listener) {
			listeners.remove(listener);
		}
		
	}
	
	private boolean splittersOnTop = true;
	private int clickEventsMask = Event.ONDBLCLICK;
	private boolean splitterFixed = false;
	
    protected List<VisibilityChangeListener> visibilityListeners = new ArrayList<VisibilityChangeListener>();

	
    public SGSplitLayoutPanel() {
    	//for themebundles we will use actual splitter size (4). 
    	this(4);
    }
	public SGSplitLayoutPanel(int splitSize) {
		super(splitSize);
		addStyleName(DECORATED_SPLIT_PANEL);
	}

	public void setSplitterFixed(boolean fixed) {
		this.splitterFixed=fixed;
	}
	/**
	 * If other than {@link Event#ONDBLCLICK}, should be set before adding children.
	 * @param events
	 */
	public void setCollapseEventsMask(int events) {
		clickEventsMask = events;
	}
	
	public void setSplittersOnTop(boolean splittersOnTop) {
		this.splittersOnTop = splittersOnTop;
	}
	
	@Override
	public Splitter insertAndGetSplitter(Widget child, Direction direction, double size, Widget before) {
		Splitter splitter = super.insertAndGetSplitter(child, direction, size, before);
		// Set splitter style
		if (splitter != null) {
			UIObject.setStyleName(DOM.getParent(splitter.getElement()), DECORATED_SPLIT_PANEL+"-SPLIT-"+direction.name(), true);
		}
		
		// Set direction-dependent style
		UIObject.setStyleName(DOM.getParent(child.getElement()), DECORATED_SPLIT_PANEL+"-"+direction.name(), true);
	
		if (splittersOnTop) {
			// moving everything that's added publicly to the bottom; splitters will remain last
			Element childWrapper = child.getElement().getParentElement();
			Element topParent = childWrapper.getParentElement();
			if (topParent.getChildCount() > 2) {
				topParent.removeChild(childWrapper);
				topParent.insertFirst(childWrapper);
			}
		}
		return splitter;
	}
	
	@Override
	protected SGSplitter createVerticalSplitter(Widget target, boolean reverse) {
		return new SGVSplitter(target, reverse);
	}
	
	@Override
	protected SGSplitter createHorizontalSplitter(Widget target, boolean reverse) {
		return new SGHSplitter(target, reverse);
	}
	
	@Override
	public SGSplitter addWestAndGetSplitter(Widget widget, double size) {
		return (SGSplitter)super.addWestAndGetSplitter(widget, size);
	}
	
	@Override
	public SGSplitter addEastAndGetSplitter(Widget widget, double size) {
		return (SGSplitter)super.addEastAndGetSplitter(widget, size);
	}
	
	@Override
	public SGSplitter addNorthAndGetSplitter(Widget widget, double size) {
		return (SGSplitter)super.addNorthAndGetSplitter(widget, size);
	}
	
	@Override
	public SGSplitter addSouthAndGetSplitter(Widget widget, double size) {
		return (SGSplitter)super.addSouthAndGetSplitter(widget, size);
	}
	
	public static class SplitterWidgetVisibilityHistoryHandler implements HistoryHandler {
		
		public static void bind(String histParam, SGSplitter splitter) {
			HistoryManager.getInstance().registerHandler(new SplitterWidgetVisibilityHistoryHandler(histParam, splitter));
		}
		
		private final String histParam;
		private final SGSplitter splitter;
		
		private SplitterWidgetVisibilityHistoryHandler(final String histParam, SGSplitter splitter) {
			this.histParam = histParam;
			this.splitter = splitter;
			
			splitter.addPropertyChangeListener(new PropertyChangeListener<Object>() {
				public void propertyChange(Object sender, String propertyName, Object oldValue, Object newValue) {
					if (SGSplitter.PROP_WIDGET_VISIBLE.equals(propertyName) && newValue instanceof Boolean) {
						HistoryManager.getInstance().setHistoryParam(histParam, newValue.toString());
					}
				}
			});
		}
		
		public Collection<String> getHandledHistoryParams() {
			// variable used instead of constant as multiple sliders can be handled by this type of handler
			return Arrays.asList(histParam);
		}
		
		public void handleHistoryChange(HistoryManager manager) {
			if (!manager.hasHistoryParam(histParam)) return;
			
			boolean visible = StringUtil.isTruthy(manager.getHistoryParam(histParam), false);
			splitter.setWidgetVisible(visible);
		}
		
	}
	
	public boolean isChildVisible(Object child) {
		Widget myChild = UtilGWT.getDirectChildForDescendant(this, (Widget)child);
		if (myChild == null) {
			return false;
		}
		return myChild.isVisible();
	}
	
	public void ensureChildVisible(Object child) {
		Widget childW = UtilGWT.getDirectChildForDescendant(this, (Widget)child);
		if (childW == null) {
			return;
		}

		LayoutData ld = (LayoutData)childW.getLayoutData();
		if (ld.size == 0 && ld.direction != CENTER) {
			setWidgetSize(childW, Math.round(ld.originalSize));
			forceLayout(); //GWT bug #7188 http://code.google.com/p/google-web-toolkit/issues/detail?id=7188
		}
	}
	
	public boolean isDeepVisible() {
		return EnsureVisibilityUtil.isDeepVisible(this);
	}
	
	public void ensureVisible() {
		EnsureVisibilityUtil.ensureVisibility(this);
	}
	
	@Override
	protected void onUnload() {
		notifyVisibilityChange(false);
		super.onUnload();
	}

	protected void notifyVisibilityChange(boolean visible) {
		for (VisibilityChangeListener l : visibilityListeners) {
			l.visibilityChanged(visible);
		}
		
	}
	
	@Override
	public void addVisibilityChangeListener(VisibilityChangeListener listener) {
		visibilityListeners.add(listener);
	}
	
	@Override
	public void removeVisibilityChangeListener(VisibilityChangeListener listener) {
		visibilityListeners.remove(listener);
	}
	
	@Override
	public void setVisible(boolean visible) {
		boolean oldVisible = isVisible();
		super.setVisible(visible);
		if (oldVisible != visible) {
			notifyVisibilityChange(visible);
		}
	}
	
	@Override
	public void animate(int duration, final AnimationCallback callback) {
		super.animate(duration, new AnimationCallback() {
			@Override
			public void onLayout(Layer layer, double progress) {
				if (callback != null) {
					callback.onLayout(layer, progress);
				}
			}
			@Override
			public void onAnimationComplete() {
				if (callback != null) {
					callback.onAnimationComplete();
				}
				afterLayout();
			}
		});
	}
	
	@Override
	public void forceLayout() {
		super.forceLayout();
		afterLayout();
	}

	protected void afterLayout() {
		updateChildVisibility();
	}

	private void updateChildVisibility() {
		for (Widget child : this) {
			Object layoutData = child.getLayoutData();
			if (layoutData instanceof LayoutData) {
				LayoutData ld = (LayoutData)layoutData;
				child.setVisible(!ld.hidden && (ld.direction == CENTER || ld.size > 0));
			}
		}
	}
}
