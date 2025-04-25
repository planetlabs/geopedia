package com.sinergise.gwt.ui.maingui.gwtmod;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.DockLayoutPanel.Direction;
import com.google.gwt.user.client.ui.Widget;

/**
 * A panel that adds user-positioned splitters between each of its child
 * widgets.
 * 
 * <p>
 * This panel is used in the same way as {@link DockLayoutPanel}, except that
 * its children's sizes are always specified in {@link Unit#PX} units, and each
 * pair of child widgets has a splitter between them that the user can drag.
 * </p>
 * 
 * <p>
 * This widget will <em>only</em> work in standards mode, which requires that
 * the HTML page in which it is run have an explicit &lt;!DOCTYPE&gt;
 * declaration.
 * </p>
 * 
 * <h3>CSS Style Rules</h3>
 * <ul class='css'>
 * <li>.gwt-SplitLayoutPanel { the panel itself }</li>
 * <li>.gwt-SplitLayoutPanel .gwt-SplitLayoutPanel-HDragger { horizontal dragger
 * }</li>
 * <li>.gwt-SplitLayoutPanel .gwt-SplitLayoutPanel-VDragger { vertical dragger }
 * </li>
 * </ul>
 * 
 * <p>
 * <h3>Example</h3>
 * {@example com.google.gwt.examples.SplitLayoutPanelExample}
 * </p>
 * 
 * <p><b>Sinergise GWT Mod that returns created splitter Widget uppon inserting new widgets.</b></p>
 * 
 * @see com.google.gwt.user.client.ui.SplitLayoutPanel
 */
public class SplitLayoutPanel extends DockLayoutPanel {

  protected class HSplitter extends Splitter {
    public HSplitter(Widget target, boolean reverse) {
      super(target, reverse);
      getElement().getStyle().setPropertyPx("width", splitterSize);
      setStyleName("gwt-SplitLayoutPanel-HDragger");
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

  protected abstract class Splitter extends Widget {
    protected final Widget target;

    private int offset;
    private boolean mouseDown;
    private ScheduledCommand layoutCommand;

    private final boolean reverse;
    private int minSize;

    public Splitter(Widget target, boolean reverse) {
      this.target = target;
      this.reverse = reverse;

      setElement(Document.get().createDivElement());
      sinkEvents(Event.ONMOUSEDOWN | Event.ONMOUSEUP | Event.ONMOUSEMOVE
          | Event.ONDBLCLICK);
    }

    @Override
    public void onBrowserEvent(Event event) {
      switch (event.getTypeInt()) {
        case Event.ONMOUSEDOWN:
          mouseDown = true;
          offset = getEventPosition(event) - getAbsolutePosition();
          Event.setCapture(getElement());
          event.preventDefault();
          break;

        case Event.ONMOUSEUP:
          mouseDown = false;
          Event.releaseCapture(getElement());
          event.preventDefault();
          break;

        case Event.ONMOUSEMOVE:
          if (mouseDown) {
            int size;
            if (reverse) {
              size = getTargetPosition() + getTargetSize()
                  - getEventPosition(event) - offset;
            } else {
              size = getEventPosition(event) - getTargetPosition() - offset;
            }
            setAssociatedWidgetSize(size);
            event.preventDefault();
          }
          break;
      }
    }

    public void setMinSize(int minSize) {
      this.minSize = minSize;
      LayoutData layout = (LayoutData) target.getLayoutData();

      // Try resetting the associated widget's size, which will enforce the new
      // minSize value.
      setAssociatedWidgetSize((int) layout.size);
    }

    protected abstract int getAbsolutePosition();

    protected abstract int getEventPosition(Event event);

    protected abstract int getTargetPosition();

    protected abstract int getTargetSize();
    
    protected int getAssociatedWidgetSize() {
    	return (int)(((LayoutData) target.getLayoutData())).size;
    }

    public void setAssociatedWidgetSize(int size) {
      if (size < minSize) {
        size = minSize;
      }

      LayoutData layout = (LayoutData) target.getLayoutData();
      if (size == layout.size) {
        return;
      }

      layout.size = size;

      // Defer actually updating the layout, so that if we receive many
      // mouse events before layout/paint occurs, we'll only update once.
      if (layoutCommand == null) {
        layoutCommand = new Command() {
          public void execute() {
            layoutCommand = null;
            forceLayout();
          }
        };
        Scheduler.get().scheduleDeferred(layoutCommand);
      }
    }
  }

  protected class VSplitter extends Splitter {
    public VSplitter(Widget target, boolean reverse) {
      super(target, reverse);
      getElement().getStyle().setPropertyPx("height", splitterSize);
      setStyleName("gwt-SplitLayoutPanel-VDragger");
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

  private static final int DEFAULT_SPLITTER_SIZE = 8;

  protected final int splitterSize; 

  /**
   * Construct a new {@link SplitLayoutPanel} with the default splitter size of
   * 8px.
   */
  public SplitLayoutPanel() {
    this(DEFAULT_SPLITTER_SIZE);
  }

  /**
   * Construct a new {@link SplitLayoutPanel} with the specified splitter size
   * in pixels.
   * 
   * @param splitterSize the size of the splitter in pixels
   */
  public SplitLayoutPanel(int splitterSize) {
    super(Unit.PX);
    this.splitterSize = splitterSize;
    setStyleName("gwt-SplitLayoutPanel");
  }

  /**
   * Return the size of the splitter in pixels.
   * 
   * @return the splitter size
   */
  public int getSplitterSize() {
    return splitterSize;
  }

  @Override
  public void insert(Widget child, Direction direction, double size, Widget before) {
    insertAndGetSplitter(child, direction, size, before);
  }
  
  public Splitter insertAndGetSplitter(Widget child, Direction direction, double size, Widget before) {
    super.insert(child, direction, size, before);
    if (direction != Direction.CENTER) {
      return insertSplitter(child, before);
    }
    return null;
  }

  @Override
  public boolean remove(Widget child) {
    assert !(child instanceof Splitter) : "Splitters may not be directly removed";

    int idx = getWidgetIndex(child);
    if (super.remove(child)) {
      // Remove the associated splitter, if any.
      // Now that the widget is removed, idx is the index of the splitter.
      if (idx < getWidgetCount()) {
        // Call super.remove(), or we'll end up recursing.
        super.remove(getWidget(idx));
      }
      return true;
    }
    return false;
  }

  /**
   * Sets the minimum allowable size for the given widget.
   * 
   * <p>
   * Its associated splitter cannot be dragged to a position that would make it
   * smaller than this size. This method has no effect for the
   * {@link DockLayoutPanel.Direction#CENTER} widget.
   * </p>
   * 
   * @param child the child whose minimum size will be set
   * @param minSize the minimum size for this widget
   */
  public void setWidgetMinSize(Widget child, int minSize) {
    _assertIsChild(child);
    Splitter splitter = getAssociatedSplitter(child);
    // The splitter is null for the center element. 
    if (splitter != null) {
      splitter.setMinSize(minSize);
    }
  }
  
  private void _assertIsChild(Widget widget) {
    assert (widget == null) || (widget.getParent() == this) : "The specified widget is not a child of this panel";
  }

  private Splitter getAssociatedSplitter(Widget child) {
    // If a widget has a next sibling, it must be a splitter, because the only
    // widget that *isn't* followed by a splitter must be the CENTER, which has
    // no associated splitter.
    int idx = getWidgetIndex(child);
    if (idx > -1 && idx < getWidgetCount() - 1) {
      Widget splitter = getWidget(idx + 1);
      assert splitter instanceof Splitter : "Expected child widget to be splitter";
      return (Splitter) splitter;
    }
    return null;
  }

  private Splitter insertSplitter(Widget widget, Widget before) {
    assert getChildren().size() > 0 : "Can't add a splitter before any children";

    LayoutData layout = (LayoutData) widget.getLayoutData();
    Splitter splitter = null;
    switch (getResolvedDirection(layout.direction)) {
      case WEST:
        splitter = createHorizontalSplitter(widget, false);
        break;
      case EAST:
        splitter = createHorizontalSplitter(widget, true);
        break;
      case NORTH:
        splitter = createVerticalSplitter(widget, false);
        break;
      case SOUTH:
        splitter = createVerticalSplitter(widget, true);
        break;
      default:
        assert false : "Unexpected direction";
    }

    super.insert(splitter, layout.direction, splitterSize, before);
    return splitter;
  }
  
  protected Splitter createVerticalSplitter(Widget target, boolean reverse) {
	  return new VSplitter(target, reverse);
  }
  
  protected Splitter createHorizontalSplitter(Widget target, boolean reverse) {
	  return new HSplitter(target, reverse);
  }
  
	public Splitter addWestAndGetSplitter(Widget widget, double size) {
		return insertAndGetSplitter(widget, Direction.WEST, size, null);
	}
  	
	public Splitter addEastAndGetSplitter(Widget widget, double size) {
		return insertAndGetSplitter(widget, Direction.EAST, size, null);
	}
  	
	public Splitter addNorthAndGetSplitter(Widget widget, double size) {
		return insertAndGetSplitter(widget, Direction.NORTH, size, null);
	}
  	
	public Splitter addSouthAndGetSplitter(Widget widget, double size) {
		return insertAndGetSplitter(widget, Direction.SOUTH, size, null);
	}
  
}
