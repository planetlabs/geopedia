package com.sinergise.gwt.ui.maingui.gwtmod;


import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.layout.client.Layout;
import com.google.gwt.layout.client.Layout.Layer;
import com.google.gwt.user.client.ui.AnimatedLayout;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.DockLayoutPanel.Direction;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.LayoutCommand;
import com.google.gwt.user.client.ui.ProvidesResize;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.WidgetCollection;
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
public class DockLayoutPanel extends ComplexPanel implements AnimatedLayout,
    RequiresResize, ProvidesResize {


  /**
   * Layout data associated with each widget.
   */
  protected static class LayoutData {
    public Direction direction;
    public double oldSize, size;
    public double originalSize;
    public boolean hidden;
    public boolean popout = false;
    public Layer layer;

    public LayoutData(Direction direction, double size, Layer layer) {
      this.direction = direction;
      this.size = size;
      this.layer = layer;
    }
  }

  private class DockAnimateCommand extends LayoutCommand {
    public DockAnimateCommand(Layout layout) {
      super(layout);
    }

    @Override
    protected void doBeforeLayout() {
      doLayout();
    }
  }

  private final Unit unit;
  private Widget center;
  private final Layout layout;
  private final LayoutCommand layoutCmd;
  private double filledWidth, filledHeight;

  /**
   * Creates an empty dock panel.
   *
   * @param unit the unit to be used for layout
   */
  public DockLayoutPanel(Unit unit) {
    this.unit = unit;

    setElement(Document.get().createDivElement());
    layout = new Layout(getElement());
    layoutCmd = new DockAnimateCommand(layout);
  }

  /**
   * Adds a widget at the center of the dock. No further widgets may be added
   * after this one.
   *
   * @param widget the widget to be added
   */
  @Override
  public void add(Widget widget) {
    insert(widget, Direction.CENTER, 0, null);
  }

  /**
   * Adds a widget to the east edge of the dock.
   *
   * @param widget the widget to be added
   * @param size the child widget's size
   */
  public void addEast(Widget widget, double size) {
    insert(widget, Direction.EAST, size, null);
  }

  /**
   * Overloaded version for IsWidget.
   * 
   * @see #addEast(Widget,double)
   */
  public void addEast(IsWidget widget, double size) {
    this.addEast(widget.asWidget(), size);
  }
  
  /**
   * Adds a widget to the end of the line. In LTR mode, the widget is added to
   * the east. In RTL mode, the widget is added to the west.
   *
   * @param widget the widget to be added
   * @param size the child widget's size
   */
  public void addLineEnd(Widget widget, double size) {
    insert(widget, Direction.LINE_END, size, null);
  }

  /**
   * Adds a widget to the start of the line. In LTR mode, the widget is added to
   * the west. In RTL mode, the widget is added to the east.
   *
   * @param widget the widget to be added
   * @param size the child widget's size
   */
  public void addLineStart(Widget widget, double size) {
    insert(widget, Direction.LINE_START, size, null);
  }

  /**
   * Adds a widget to the north edge of the dock.
   *
   * @param widget the widget to be added
   * @param size the child widget's size
   */
  public void addNorth(Widget widget, double size) {
    insert(widget, Direction.NORTH, size, null);
  }
  
  /**
   * Overloaded version for IsWidget.
   * 
   * @see #addNorth(Widget,double)
   */
  public void addNorth(IsWidget widget, double size) {
    this.addNorth(widget.asWidget(), size);
  }

  /**
   * Adds a widget to the south edge of the dock.
   *
   * @param widget the widget to be added
   * @param size the child widget's size
   */
  public void addSouth(Widget widget, double size) {
    insert(widget, Direction.SOUTH, size, null);
  }
  
  /**
   * Overloaded version for IsWidget.
   * 
   * @see #addSouth(Widget,double)
   */
  public void addSouth(IsWidget widget, double size) {
    this.addSouth(widget.asWidget(), size);
  }

  /**
   * Adds a widget to the west edge of the dock.
   *
   * @param widget the widget to be added
   * @param size the child widget's size
   */
  public void addWest(Widget widget, double size) {
    insert(widget, Direction.WEST, size, null);
  }
  
  /**
   * Overloaded version for IsWidget.
   * 
   * @see #addWest(Widget,double)
   */
  public void addWest(IsWidget widget, double size) {
    this.addWest(widget.asWidget(), size);
  }

  public void animate(int duration) {
    animate(duration, null);
  }

  public void animate(int duration, final Layout.AnimationCallback callback) {
    layoutCmd.schedule(duration, callback);
  }

  public void forceLayout() {
    layoutCmd.cancel();
    doLayout();
    layout.layout();
    onResize();
  }

  /**
   * Gets the container element wrapping the given child widget.
   *
   * @param child
   * @return the widget's container element
   */
  public Element getWidgetContainerElement(Widget child) {
    assertIsChild(child);
    return ((LayoutData) child.getLayoutData()).layer.getContainerElement();
  }

  /**
   * Gets the layout direction of the given child widget.
   *
   * @param child the widget to be queried
   * @return the widget's layout direction, or <code>null</code> if it is not a
   *         child of this panel
   */
  public Direction getWidgetDirection(Widget child) {
    assertIsChild(child);
    if (child.getParent() != this) {
      return null;
    }
    return ((LayoutData) child.getLayoutData()).direction;
  }

  /**
   * Adds a widget to the east edge of the dock, inserting it before an existing
   * widget.
   *
   * @param widget the widget to be added
   * @param size the child widget's size
   * @param before the widget before which to insert the new child, or
   *          <code>null</code> to append
   */
  public void insertEast(Widget widget, double size, Widget before) {
    insert(widget, Direction.EAST, size, before);
  }

  /**
   * Adds a widget to the start of the line, inserting it before an existing
   * widget. In LTR mode, the widget is added to the east. In RTL mode, the
   * widget is added to the west.
   *
   * @param widget the widget to be added
   * @param size the child widget's size
   * @param before the widget before which to insert the new child, or
   *          <code>null</code> to append
   */
  public void insertLineEnd(Widget widget, double size, Widget before) {
    insert(widget, Direction.LINE_END, size, before);
  }

  /**
   * Adds a widget to the end of the line, inserting it before an existing
   * widget. In LTR mode, the widget is added to the west. In RTL mode, the
   * widget is added to the east.
   *
   * @param widget the widget to be added
   * @param size the child widget's size
   * @param before the widget before which to insert the new child, or
   *          <code>null</code> to append
   */
  public void insertLineStart(Widget widget, double size, Widget before) {
    insert(widget, Direction.LINE_START, size, before);
  }

  /**
   * Adds a widget to the north edge of the dock, inserting it before an
   * existing widget.
   *
   * @param widget the widget to be added
   * @param size the child widget's size
   * @param before the widget before which to insert the new child, or
   *          <code>null</code> to append
   */
  public void insertNorth(Widget widget, double size, Widget before) {
    insert(widget, Direction.NORTH, size, before);
  }

  /**
   * Adds a widget to the south edge of the dock, inserting it before an
   * existing widget.
   *
   * @param widget the widget to be added
   * @param size the child widget's size
   * @param before the widget before which to insert the new child, or
   *          <code>null</code> to append
   */
  public void insertSouth(Widget widget, double size, Widget before) {
    insert(widget, Direction.SOUTH, size, before);
  }

  /**
   * Adds a widget to the west edge of the dock, inserting it before an existing
   * widget.
   *
   * @param widget the widget to be added
   * @param size the child widget's size
   * @param before the widget before which to insert the new child, or
   *          <code>null</code> to append
   */
  public void insertWest(Widget widget, double size, Widget before) {
    insert(widget, Direction.WEST, size, before);
  }

  public void onResize() {
    for (Widget child : getChildren()) {
      if (child instanceof RequiresResize) {
        ((RequiresResize) child).onResize();
      }
    }
  }

  @Override
  public boolean remove(Widget w) {
    boolean removed = super.remove(w);
    if (removed) {
      // Clear the center widget.
      if (w == center) {
        center = null;
      }

      LayoutData data = (LayoutData) w.getLayoutData();
      layout.removeChild(data.layer);
    }

    return removed;
  }

  /**
   * Updates the size of the widget passed in as long as it is not the center
   * widget and updates the layout of the dock.
   *
   * @param widget the widget that needs to update its size
   * @param size the size to update the widget to
   */
  public void setWidgetSize(Widget widget, double size) {
    assertIsChild(widget);
    LayoutData data = (LayoutData) widget.getLayoutData();

    assert data.direction != Direction.CENTER :
        "The size of the center widget can not be updated.";

    data.size = size;

    // Update the layout.
    animate(0);
  }

  protected Widget getCenter() {
    return center;
  }

  protected double getCenterHeight() {
    return getElement().getClientHeight() / layout.getUnitSize(unit, true) -
              filledHeight;
  }

  protected double getCenterWidth() {
    return getElement().getClientWidth() / layout.getUnitSize(unit, false) -
              filledWidth;
  }

  /**
   * Resolve the specified direction based on the current locale. If the
   * direction is {@link Direction#LINE_START} or {@link Direction#LINE_END},
   * the return value will be one of {@link Direction#EAST} or
   * {@link Direction#WEST} depending on the RTL mode of the locale. For all
   * other directions, the specified value is returned.
   *
   * @param direction the specified direction
   * @return the locale
   */
  protected Direction getResolvedDirection(Direction direction) {
    if (direction == Direction.LINE_START) {
      return LocaleInfo.getCurrentLocale().isRTL()
          ? Direction.EAST : Direction.WEST;
    } else if (direction == Direction.LINE_END) {
      return LocaleInfo.getCurrentLocale().isRTL()
          ? Direction.WEST : Direction.EAST;
    }
    return direction;
  }

  protected Unit getUnit() {
    return unit;
  }

  /**
   * Adds a widget to the specified edge of the dock. If the widget is already a
   * child of this panel, this method behaves as though {@link #remove(Widget)}
   * had already been called.
   *
   * @param widget the widget to be added
   * @param direction the widget's direction in the dock
   * @param before the widget before which to insert the new child, or
   *          <code>null</code> to append
   */
  protected void insert(Widget widget, Direction direction, double size,
      Widget before) {
    assertIsChild(before);

    // Validation.
    if (before == null) {
      assert center == null : "No widget may be added after the CENTER widget";
    } else {
      assert direction != Direction.CENTER : "A CENTER widget must always be added last";
    }

    // Detach new child.
    widget.removeFromParent();

    // Logical attach.
    WidgetCollection children = getChildren();
    if (before == null) {
      children.add(widget);
    } else {
      int index = children.indexOf(before);
      children.insert(widget, index);
    }

    if (direction == Direction.CENTER) {
      center = widget;
    }

    // Physical attach.
    Layer layer = layout.attachChild(widget.getElement(),
        (before != null) ? before.getElement() : null, widget);
    LayoutData data = new LayoutData(direction, size, layer);
    widget.setLayoutData(data);

    // Adopt.
    adopt(widget);

    // Update the layout.
    animate(0);
  }

  @Override
  protected void onLoad() {
    layout.onAttach();
  }

  @Override
  protected void onUnload() {
    layout.onDetach();
  }

  void assertIsChild(Widget widget) {
    assert (widget == null) || (widget.getParent() == this) : "The specified widget is not a child of this panel";
  }

  private void doLayout() {
    double left = 0;
    double top = 0;
    double right = 0;
    double bottom = 0;

    for (Widget child : getChildren()) {
      LayoutData data = (LayoutData) child.getLayoutData();
      Layer layer = data.layer;

      switch (getResolvedDirection(data.direction)) {
        case NORTH:
          layer.setLeftRight(left, unit, right, unit);
          layer.setTopHeight(top, unit, data.size, unit);
          if (!data.popout) {
        	  top += data.size;
          } else {
        	  top += data.oldSize;
          }  
          break;

        case SOUTH:
          layer.setLeftRight(left, unit, right, unit);
          layer.setBottomHeight(bottom, unit, data.size, unit);
          if (!data.popout) {
        	  bottom += data.size;
          } else {
        	  bottom += data.oldSize;
          }
          break;

        case WEST:
          layer.setTopBottom(top, unit, bottom, unit);
          layer.setLeftWidth(left, unit, data.size, unit);
          if (!data.popout) {
        	  left += data.size;
          } else {
        	  left += data.oldSize;
          }

          break;

        case EAST:
          layer.setTopBottom(top, unit, bottom, unit);
          layer.setRightWidth(right, unit, data.size, unit);
          if (!data.popout) {
        	  right += data.size;
          } else {
        	  right += data.oldSize;
          }
          break;

        case CENTER:
          layer.setLeftRight(left, unit, right, unit);
          layer.setTopBottom(top, unit, bottom, unit);
          break;
      }
    }

    filledWidth = left + right;
    filledHeight = top + bottom;
  }
}

